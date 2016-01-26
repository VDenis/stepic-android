package org.stepic.droid.core;

import android.os.Handler;

import com.squareup.otto.Bus;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.units.UnitProgressUpdateEvent;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.store.operations.DatabaseManager;

import java.util.List;

import javax.inject.Inject;

public class LocalProgressOfUnitManager implements ILocalProgressManager {
    private DatabaseManager mDatabaseManager;
    private Bus mBus;

    @Inject
    public LocalProgressOfUnitManager(DatabaseManager databaseManager, Bus bus) {
        mDatabaseManager = databaseManager;
        mBus = bus;
    }

    @Override
    public void checkUnitAsPassed(final long stepId) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Step step = mDatabaseManager.getStepById(stepId);
                if (step == null) return;
                List<Step> stepList = mDatabaseManager.getStepsOfLesson(step.getLesson());
                for (Step stepItem : stepList) {
                    if (!stepItem.is_custom_passed()) return;
                }

                Unit unit = mDatabaseManager.getUnitByLessonId(step.getLesson());
                if (unit == null) return;

                unit.setIs_viewed_custom(true);
                mDatabaseManager.addUnit(unit);

                final long unitId = unit.getId();
                Handler mainHandler = new Handler(MainApplication.getAppContext().getMainLooper());
                //Say to ui that ui is cached now
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mBus.post(new UnitProgressUpdateEvent(unitId));
                    }
                };
                mainHandler.post(myRunnable);

            }
        });
    }
}
