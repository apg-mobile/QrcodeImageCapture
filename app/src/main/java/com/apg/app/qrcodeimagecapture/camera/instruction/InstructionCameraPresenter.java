package com.apg.app.qrcodeimagecapture.camera.instruction;

import org.androidannotations.annotations.EBean;

/**
 * Created by X-tivity on 1/16/2017 AD.
 */
@EBean
public class InstructionCameraPresenter implements InstructionCameraContract.Presenter {

    private InstructionCameraContract.View mView;

    @Override
    public void setView(InstructionCameraContract.View view) {
        this.mView = view;
    }

    @Override
    public void init() {
        if (mView.getCommunicator() != null) {
            mView.getCommunicator().onInstructionRemoteReady(mView.getRemote());
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }
}
