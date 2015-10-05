package org.stepic.droid.concurrency;

import com.squareup.otto.Bus;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IShell;
import org.stepic.droid.events.courses.FinishingSaveCoursesToDbEvent;
import org.stepic.droid.events.courses.StartingSaveCoursesToDbEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DbOperationsCourses;

import java.util.List;

import javax.inject.Inject;

public class ToDbCoursesTask extends StepicTask<Void, Void, Void> {

    @Inject
    IShell mShell;
    @Inject
    Bus bus;

    private List<Course> mCourses;
    private DbOperationsCourses.Table mCourseType;
    private int mPage;

    public ToDbCoursesTask(List<Course> courses, DbOperationsCourses.Table type, int page) {
        super(MainApplication.getAppContext());
        MainApplication.component().inject(this);

        //courses now is not thread safe
        mPage = page;
        mCourseType = type;
        mCourses = courses;
    }

    @Override
    protected Void doInBackgroundBody(Void... params) throws Exception {

        DbOperationsCourses dbOperationsCourses = mShell.getDbOperationsCourses(mCourseType);
        dbOperationsCourses.open();
        try {
            if (mPage == 1)
                dbOperationsCourses.clearCache();
            for (Course courseItem : mCourses) {
                if (!dbOperationsCourses.isCourseInDB(courseItem))
                    dbOperationsCourses.addCourse(courseItem);
            }
        } finally {
            dbOperationsCourses.close();
        }
        return null;

    }

    @Override
    protected void onPreExecute() {
        bus.post(new StartingSaveCoursesToDbEvent(mCourseType));
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(AsyncResultWrapper<Void> voidAsyncResultWrapper) {
        super.onPostExecute(voidAsyncResultWrapper);
        bus.post(new FinishingSaveCoursesToDbEvent(mCourseType));
    }
}
