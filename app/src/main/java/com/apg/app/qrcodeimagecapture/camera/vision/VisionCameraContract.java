package com.apg.app.qrcodeimagecapture.camera.vision;

/**
 * Created by X-tivity on 1/10/2017 AD.
 */

public interface VisionCameraContract {

    interface View {
        void setCommunicator(Communicator comm);

        void setPresenter(VisionCameraContract.Presenter presenter);
    }

    interface Presenter {
        void setView(View view);

        void onResume();

        void onPause();

        void onDetach();
    }

    interface Communicator {
        void onCameraReady(VisionCameraRemote remote);

        void onShutter();

        void onDetectBarcode(String barcode);

        void onPictureTaken(byte[] bytes);

        void onPermissionDenied();

        void onUsbAttached(boolean hardwareKeyboardAvailable, VisionCameraRemote remote);
    }
}
