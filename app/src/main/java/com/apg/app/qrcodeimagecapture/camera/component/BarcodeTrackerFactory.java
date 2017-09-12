package com.apg.app.qrcodeimagecapture.camera.component;

import android.util.Log;

import com.apg.app.qrcodeimagecapture.camera.component.view.GraphicOverlay;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

/**
 * Created by X-tivity on 1/9/2017 AD.
 */
public class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {

    private OnDetectorUpdateListener<Barcode> mListener;
    private GraphicOverlay mGraphicOverlay;
    private boolean isNotify = true;

    public BarcodeTrackerFactory(GraphicOverlay graphicOverlay,
                                 OnDetectorUpdateListener<Barcode> listener) {
        mGraphicOverlay = graphicOverlay;
        mListener = listener;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        Log.e("CREATE BARCODE", barcode.displayValue);
        if (isNotify && mListener != null) {
            mListener.onDetectorNew(barcode);
            BarcodeGraphic graphic = new BarcodeGraphic(mGraphicOverlay);
            return new GraphicTracker<>(mGraphicOverlay, graphic);
        } else {
            return null;
        }
    }

    public boolean isNotify() {
        return isNotify;
    }

    public void setNotify(boolean notify) {
        isNotify = notify;
    }

    public interface OnDetectorUpdateListener<T> {
        void onDetectorNew(T item);
    }
}

