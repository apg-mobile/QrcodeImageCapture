package com.apg.app.qrcodeimagecapture.camera.photo;

import android.text.TextUtils;

import org.androidannotations.annotations.EBean;

/**
 * Created by X-tivity on 1/16/2017 AD.
 */
@EBean
public class PhotoCameraPresenter implements PhotoCameraContract.Presenter {

    private PhotoCameraContract.View mView;

    @Override
    public void setView(PhotoCameraContract.View view) {
        this.mView = view;
    }

    @Override
    public void init() {
        if (mView.getCommunicator() != null) {
            mView.getCommunicator().onPhotoRemoteReady(mView.getRemote());
        }
    }

    @Override
    public void onBtnCaptureClick() {
        if (mView.getCommunicator() != null) {
            mView.getCommunicator().onPhotoButtonClick();
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onRemoteSetGuideForQrCode(String qrCode) {
        if (!TextUtils.isEmpty(qrCode)) {
            mView.showPhotoGuide(qrCode);
        } else {
            mView.hidePhotoGuide(qrCode);
        }
    }
}
