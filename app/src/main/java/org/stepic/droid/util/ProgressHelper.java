package org.stepic.droid.util;

import android.app.ProgressDialog;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ProgressBar;

import com.yandex.metrica.YandexMetrica;

public class ProgressHelper {
    public static void dismiss(ProgressBar mProgressLogin) {

        if (mProgressLogin != null && mProgressLogin.getVisibility() != View.GONE) {
            mProgressLogin.setVisibility(View.GONE);
        }
    }

    public static void activate(ProgressBar progressBar) {
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
    }

    public static void dismiss(SwipeRefreshLayout swipeRefreshLayout) {
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    public static void activate(SwipeRefreshLayout swipeRefreshLayout) {
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(true);
    }

    public static void activate(ProgressDialog progressDialog) {
        if (progressDialog != null)
            progressDialog.show();
    }

    public static void dismiss(ProgressDialog progressDialog) {
        if (progressDialog != null && progressDialog.isShowing()) {
            try {
                progressDialog.dismiss();
            } catch (Exception ex) {
                YandexMetrica.reportError("progressDialog", ex);
            }
        }
    }
}
