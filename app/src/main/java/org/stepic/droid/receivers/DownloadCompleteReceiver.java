package org.stepic.droid.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.squareup.otto.Bus;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.video.VideoCachedOnDiskEvent;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.ICancelSniffer;
import org.stepic.droid.store.IStoreStateManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.RWLocks;

import java.io.File;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

public class DownloadCompleteReceiver extends BroadcastReceiver {

    @Inject
    UserPreferences mUserPrefs;
    @Inject
    DatabaseFacade mDatabaseFacade;
    @Inject
    IStoreStateManager mStoreStateManager;
    @Inject
    Bus bus;

    @Inject
    ICancelSniffer mCancelSniffer;

    @Inject
    ExecutorService mThreadSingleThreadExecutor;

    @Inject
    DownloadManager downloadManager;

    public DownloadCompleteReceiver() {
        MainApplication.component().inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

        mThreadSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                blockForInBackground(referenceId);
            }
        });
    }

    private void blockForInBackground(final long referenceId) {
        try {
            RWLocks.DownloadLock.writeLock().lock();

            DownloadEntity downloadEntity = mDatabaseFacade.getDownloadEntityIfExist(referenceId);
            if (downloadEntity != null) {
                long video_id = downloadEntity.getVideoId();
                final long step_id = downloadEntity.getStepId();
                mDatabaseFacade.deleteDownloadEntityByDownloadId(referenceId);

                File downloadFolderAndFile = new File(mUserPrefs.getUserDownloadFolder(), video_id + "");
                String path = Uri.fromFile(downloadFolderAndFile).getPath();

                if (mCancelSniffer.isStepIdCanceled(step_id)) {
                    downloadManager.remove(referenceId);//remove notification (is it really work and need?)
                    mCancelSniffer.removeStepIdCancel(step_id);
                    Step step = mDatabaseFacade.getStepById(step_id);
                    if (step != null) {
                        Lesson lesson = mDatabaseFacade.getLessonById(step.getLesson());
                        if (lesson != null) {
                            Unit unit = mDatabaseFacade.getUnitByLessonId(lesson.getId());
                            if (unit != null && mCancelSniffer.isUnitIdIsCanceled(unit.getId())) {
                                mStoreStateManager.updateUnitLessonAfterDeleting(lesson.getId());//automatically update section
                                mCancelSniffer.removeUnitIdCancel(unit.getId());
                                if (mCancelSniffer.isSectionIdIsCanceled(unit.getSection())) {
                                    mCancelSniffer.removeSectionIdCancel(unit.getSection());
                                }
                            }
                        }
                    }
                } else {
                    //is not canceled
                    final CachedVideo cachedVideo = new CachedVideo(step_id, video_id, path, downloadEntity.getThumbnail());
                    cachedVideo.setQuality(downloadEntity.getQuality());
                    mDatabaseFacade.addVideo(cachedVideo);

                    final Step step = mDatabaseFacade.getStepById(step_id);
                    step.set_cached(true);
                    step.set_loading(false);
                    mDatabaseFacade.updateOnlyCachedLoadingStep(step);
                    mStoreStateManager.updateUnitLessonState(step.getLesson());
                    final Lesson lesson = mDatabaseFacade.getLessonById(step.getLesson());
                    Handler mainHandler = new Handler(MainApplication.getAppContext().getMainLooper());
                    //Say to ui that ui is cached now
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (lesson != null)
                                bus.post(new VideoCachedOnDiskEvent(step_id, lesson, cachedVideo));
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            } else {
                downloadManager.remove(referenceId);//remove notification (is it really work and need?)
            }
        } finally

        {
            RWLocks.DownloadLock.writeLock().unlock();
        }
    }

}