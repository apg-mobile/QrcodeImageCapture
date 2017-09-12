package com.apg.app.qrcodeimagecapture.camera.photo;

/**
 * Created by X-tivity on 1/16/2017 AD.
 */

public interface PhotoCameraContract {

    interface View {
        void setCommunicator(Communicator comm);

        Communicator getCommunicator();

        String getQrCode();

        Remote getRemote();

        void showPhotoGuide(String qrCode);

        void hidePhotoGuide(String qrCode);
    }

    interface Presenter {
        void setView(View view);

        void init();

        void onBtnCaptureClick();

        void onResume();

        void onPause();

        void onRemoteSetGuideForQrCode(String qrCode);
    }

    interface Communicator {
        void onPhotoRemoteReady(Remote remote);
        void onPhotoButtonClick();
    }

    interface Remote {
        void setGuideForQrCode(String qrCode);
    }
}
