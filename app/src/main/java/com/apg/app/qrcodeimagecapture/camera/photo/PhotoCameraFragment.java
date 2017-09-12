package com.apg.app.qrcodeimagecapture.camera.photo;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apg.app.qrcodeimagecapture.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

/**
 * Created by X-tivity on 1/16/2017 AD.
 */
@EFragment(R.layout.fragment_photo_camera)
public class PhotoCameraFragment extends Fragment implements
        PhotoCameraContract.View,
        PhotoCameraContract.Remote {

    private PhotoCameraContract.Communicator comm;

    @Bean
    protected PhotoCameraPresenter presenter;

    @ViewById
    protected ImageButton btnCapture;
    @ViewById
    protected LinearLayout llGuide;
    @ViewById
    protected TextView tvQrCode;

    @InstanceState
    protected String mQrCode;

    @AfterViews
    protected void init() {
        presenter.setView(this);
        presenter.init();
    }

    private void setupGuide() {
        if (TextUtils.isEmpty(mQrCode)) {
            llGuide.setVisibility(View.GONE);
        } else {
            tvQrCode.setText(mQrCode);
            llGuide.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    public void onPause() {
        presenter.onPause();
        super.onPause();
    }

    @Click
    protected void btnCapture() {
        presenter.onBtnCaptureClick();
    }

    @Override
    public void setCommunicator(PhotoCameraContract.Communicator comm) {
        this.comm = comm;
    }

    @Override
    public PhotoCameraContract.Communicator getCommunicator() {
        return comm;
    }

    @Override
    public String getQrCode() {
        return mQrCode;
    }

    @Override
    public PhotoCameraContract.Remote getRemote() {
        return this;
    }

    @Override
    public void showPhotoGuide(String qrCode) {
        llGuide.setVisibility(View.VISIBLE);
        tvQrCode.setText(qrCode);
    }

    @Override
    public void hidePhotoGuide(String qrCode) {
        llGuide.setVisibility(View.GONE);
    }

    @Override
    public void setGuideForQrCode(String qrCode) {
        mQrCode = qrCode;
        presenter.onRemoteSetGuideForQrCode(qrCode);
    }

}
