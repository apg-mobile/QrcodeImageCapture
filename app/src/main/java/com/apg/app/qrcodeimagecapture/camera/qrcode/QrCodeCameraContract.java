package com.apg.app.qrcodeimagecapture.camera.qrcode;

/**
 * Created by X-tivity on 1/10/2017 AD.
 */

public interface QrCodeCameraContract {

    interface View {
        void setCommunicator(Communicator comm);

        Communicator getCommunicator();

        Remote getRemote();

        String getQrCode();

        void clearText();

        void inputTextRequestFocus();

        void showRecentQrCode(String s);

        void hideRecentQrCode(String s);
    }

    interface Presenter {
        void setView(View view);

        void init();

        void onResume();

        void onPause();

        void onDetach();

        void onSendQrCode();

        void OnRemoteSetRecentQrCode(String s);
    }

    interface Communicator {
        void onRemoteReady(Remote remote);

        void onInputQrCodeManually(String s);
    }

    interface Remote {
        void setRecentQrCode(String s);
        void setToast();
    }
}
