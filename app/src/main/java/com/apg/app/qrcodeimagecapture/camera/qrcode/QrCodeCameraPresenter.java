package com.apg.app.qrcodeimagecapture.camera.qrcode;

import org.androidannotations.annotations.EBean;

/**
 * Created by X-tivity on 1/10/2017 AD.
 */
@EBean
public class QrCodeCameraPresenter implements QrCodeCameraContract.Presenter {

    private QrCodeCameraContract.View mView;

    @Override
    public void setView(QrCodeCameraContract.View view) {
        this.mView = view;
    }

    @Override
    public void init() {
        if (mView.getCommunicator() != null) {
            mView.getCommunicator().onRemoteReady(mView.getRemote());
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDetach() {

    }

    @Override
    public void onSendQrCode() {
        if (mView.getCommunicator() != null) {
            String tempQrCode = mView.getQrCode();
            mView.clearText();
            mView.inputTextRequestFocus();
            mView.getCommunicator().onInputQrCodeManually(tempQrCode);
        }
    }

    @Override
    public void OnRemoteSetRecentQrCode(String s) {
        if (s == null || s.isEmpty()) {
            mView.hideRecentQrCode(s);
        } else {
            mView.showRecentQrCode(s);
        }
    }


}
