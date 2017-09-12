package com.apg.app.qrcodeimagecapture.camera.vision;

/**
 * Created by X-tivity on 1/10/2017 AD.
 */

public interface VisionCameraRemote {

    void takePhoto();

    void startDetectBarcode();

    void stopDetectBarcode();

    boolean isDetectBarcode();

    void pauseCamera();

    void resumeCamera();
}
