package com.apg.app.qrcodeimagecapture;

import android.app.Activity;
import android.app.ProgressDialog;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

/**
 * Created by nantaphop on 30-Jul-14.
 */
@EBean
public class LoadingDialog {

    @RootContext
    Activity activity;
    private ProgressDialog progressDialog;

    @UiThread
    public void show() {
        if(progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = ProgressDialog.show(activity,"",activity.getString(R.string.loading));
            progressDialog.show();
        }
    }

    @UiThread
    public void show(String msg) {
        if(progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = ProgressDialog.show(activity,"", msg);
            progressDialog.show();
        }
    }

    @UiThread
    public void dismiss() {
        if (progressDialog != null) {
            if(progressDialog.isShowing()){
                try {
                    progressDialog.dismiss();
                } catch (IllegalArgumentException e ){
                    e.printStackTrace();
                }
            }
        }
    }
}
