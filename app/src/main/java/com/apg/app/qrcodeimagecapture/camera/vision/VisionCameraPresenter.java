package com.apg.app.qrcodeimagecapture.camera.vision;

import org.androidannotations.annotations.EBean;

/**
 * Created by X-tivity on 1/10/2017 AD.
 */
@EBean
public class VisionCameraPresenter implements VisionCameraContract.Presenter {

    private VisionCameraContract.View mView;

    @Override
    public void setView(VisionCameraContract.View view) {
        mView = view;
        mView.setPresenter(this);
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
}
