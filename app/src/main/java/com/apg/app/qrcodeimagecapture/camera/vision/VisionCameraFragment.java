package com.apg.app.qrcodeimagecapture.camera.vision;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apg.app.qrcodeimagecapture.R;
import com.apg.app.qrcodeimagecapture.camera.component.BarcodeGraphic;
import com.apg.app.qrcodeimagecapture.camera.component.BarcodeTrackerFactory;
import com.apg.app.qrcodeimagecapture.camera.component.view.CameraSource;
import com.apg.app.qrcodeimagecapture.camera.component.view.CameraSourcePreview;
import com.apg.app.qrcodeimagecapture.camera.component.view.GraphicOverlay;
import com.apg.library.corehelper.Apg;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.jz.rp.library.RP;
import com.jz.rp.library.RPResult;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;

/**
 * Created by X-tivity on 1/10/2017 AD.
 */
@EFragment(R.layout.fragment_vision_camera)
public class VisionCameraFragment extends Fragment implements
        VisionCameraContract.View,
        VisionCameraRemote {

    private static final String TAG = VisionCameraFragment.class.getName();
    // intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private boolean isCanTakePhoto = true;
    private VisionCameraContract.Presenter mPresenter;
    private VisionCameraContract.Communicator comm;
    private BarcodeTrackerFactory barcodeFactory;
    private BarcodeDetector barcodeDetector;
    private CameraSource mCameraSource;
    private GestureDetector gestureDetector;
    private UsbReceiver usbReceiver;
    private BarcodeTrackerFactory.OnDetectorUpdateListener<Barcode> detectorListener =
            new BarcodeTrackerFactory.OnDetectorUpdateListener<Barcode>() {
                @Override
                public void onDetectorNew(final Barcode item) {
                    Activity activity = getActivity();
                    if (activity != null && isNotify) {
                        activity.runOnUiThread(() -> {
                            if (comm != null) comm.onDetectBarcode(item.displayValue);
                        });
                    }
                }
            };

    @FragmentArg
    protected boolean isUseFlash;
    @FragmentArg
    protected boolean isAutoFocus;

    @InstanceState
    protected boolean isNotify = true;

    @ViewById(R.id.preview)
    protected CameraSourcePreview mPreview;
    @ViewById(R.id.overlay)
    protected GraphicOverlay<BarcodeGraphic> mGraphicOverlay;
    @ViewById(R.id.llPause)
    protected LinearLayout llPause;


    @AfterViews
    protected void init() {

        gestureDetector = new GestureDetector(getActivity(), new CaptureGestureListener());

        if (getView() != null) {
            getView().setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        }

        RP.requestPermission(this, Manifest.permission.CAMERA, 0);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (RP.isPermissionGranted(getActivity(), Manifest.permission.CAMERA)) {
            startCameraSource();
        }

        if (comm != null) {
            comm.onCameraReady(this);
            comm.onUsbAttached(Apg.usb().isHardwareKeyboardAvailable(getContext()), this);
        }

        registerOnUsbListener();
        mPresenter.onResume();
    }

    @Override
    public void onPause() {
        getContext().unregisterReceiver(usbReceiver);
        releaseCamera();
        mPresenter.onPause();
        super.onPause();
    }

    @Override
    public void onDetach() {
        mPresenter.onDetach();
        super.onDetach();
    }

    public void createCameraSource(boolean autoFocus, boolean useFlash) {

        Context context = getContext().getApplicationContext();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        int format = Barcode.CODE_39 | Barcode.CODE_93 | Barcode.CODE_128 | Barcode.QR_CODE;
        barcodeDetector = new BarcodeDetector.Builder(getContext()).setBarcodeFormats(format).build();
        barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay, detectorListener);
        barcodeFactory.setNotify(isNotify);
        barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeFactory).build());

        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = context.registerReceiver(null, lowStorageFilter) != null;


            if (hasLowStorage) {
                Log.w(TAG, getString(R.string.low_storage_error));
                Toast.makeText(getContext(), R.string.low_storage_error, Toast.LENGTH_LONG).show();
            } else {
                Log.w(TAG, "Detector Fail");
                Toast.makeText(getContext(), "Detector Fail", Toast.LENGTH_LONG).show();
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        CameraSource.Builder builder = new CameraSource.Builder(getContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK);
//                .setRequestedPreviewSize(1600, 1024)
//                .setRequestedFps(30.0f);

        // make sure that auto focus is an available option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = builder.setFocusMode(
                    autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);
        }

        mCameraSource = builder
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .build();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    public void startCameraSource() {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());

        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance()
                    .getErrorDialog(getActivity(), code, RC_HANDLE_GMS);
            dlg.show();
        }

        try {

            if (mCameraSource == null) {
                createCameraSource(isAutoFocus, isUseFlash);
            }

            mPreview.start(mCameraSource, mGraphicOverlay);
        } catch (IOException e) {
            mCameraSource.release();
            mCameraSource = null;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void releaseCamera() {
        if (mPreview != null) {
            mPreview.stop();
        }
        if (mCameraSource != null) {
            mCameraSource.release(); //release the resources
            mCameraSource = null;
        }
    }

    public void registerOnUsbListener() {
        IntentFilter i = new IntentFilter();
        i.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        i.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        usbReceiver = new UsbReceiver();
        getContext().registerReceiver(usbReceiver, i);
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        RPResult rpResult = RP.validateOnRequestPermissionsResult(
                getActivity(), permissions, grantResults);

        if (rpResult.isSuccess()) {
            // do nothing, continue to onResume()..
        } else {

            if (rpResult.isSomePermissionDisabled()) {
                View.OnClickListener listener = view -> {
                    RP.openAppDetailsActivity(getActivity());
                    if (comm != null) comm.onPermissionDenied();
                };

                Snackbar snackbar = Snackbar.make(
                        mGraphicOverlay,
                        R.string.permission_camera_rationale,
                        Snackbar.LENGTH_INDEFINITE);

                View view = snackbar.getView();
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);

                snackbar
                        .setActionTextColor(Color.WHITE)
                        .setAction(R.string.ok, listener)
                        .show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Camera Permission")
                        .setMessage(R.string.no_camera_permission)
                        .setPositiveButton(R.string.ok, (dialog, id) -> {
                            RP.requestPermission(
                                    VisionCameraFragment.this,
                                    Manifest.permission.CAMERA,
                                    0);
                        })
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                            if (comm != null) {
                                comm.onPermissionDenied();
                            }
                        })
                        .show();
            }
        }
    }

    @Override
    public void takePhoto() {
        if (isVisible() && mCameraSource != null && isCanTakePhoto) {
            isCanTakePhoto = false;
            mCameraSource.takePicture(
                    () -> {
                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(() -> {
                                if (comm != null) comm.onShutter();
                            });
                        }
                    },
                    (bytes) -> {
                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(() -> {
                                if (comm != null) comm.onPictureTaken(bytes);
                                isCanTakePhoto = true;
                            });
                        }
                    });
        }
    }

    @Override
    public void startDetectBarcode() {
        if (isVisible() && barcodeDetector != null) {
            isNotify = true;
            barcodeFactory.setNotify(true);
        }
    }

    @Override
    public void stopDetectBarcode() {
        if (isVisible() && barcodeDetector != null) {
            isNotify = false;
            barcodeFactory.setNotify(false);
        }
    }

    @Override
    public boolean isDetectBarcode() {
        return isVisible() && barcodeDetector != null && barcodeFactory.isNotify();
    }

    @Override
    public void pauseCamera() {
        llPause.setVisibility(View.VISIBLE);
        releaseCamera();
    }

    @Override
    public void resumeCamera() {
        if (RP.isPermissionGranted(getActivity(), Manifest.permission.CAMERA)) {
            llPause.setVisibility(View.GONE);
            startCameraSource();
        }
    }

    /**
     * onTap returns the tapped barcode result to the calling Activity.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the activity is ending.
     */
    private boolean onTap(float rawX, float rawY) {
        // Find tap point in preview frame coordinates.
        int[] location = new int[2];
        mGraphicOverlay.getLocationOnScreen(location);
        float x = (rawX - location[0]) / mGraphicOverlay.getWidthScaleFactor();
        float y = (rawY - location[1]) / mGraphicOverlay.getHeightScaleFactor();

        // Find the barcode whose center is closest to the tapped point.
        Barcode best = null;
        float bestDistance = Float.MAX_VALUE;
        for (BarcodeGraphic graphic : mGraphicOverlay.getGraphics()) {
            Barcode barcode = graphic.getBarcode();
            if (barcode.getBoundingBox().contains((int) x, (int) y)) {
                // Exact hit, no need to keep looking.
                best = barcode;
                break;
            }
            float dx = x - barcode.getBoundingBox().centerX();
            float dy = y - barcode.getBoundingBox().centerY();
            float distance = (dx * dx) + (dy * dy);  // actually squared distance
            if (distance < bestDistance) {
                best = barcode;
                bestDistance = distance;
            }
        }

        if (best != null) {
            if (comm != null) comm.onDetectBarcode(best.rawValue);
            return true;
        }
        return false;
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    @Override
    public void setCommunicator(VisionCameraContract.Communicator comm) {
        this.comm = comm;
    }

    @Override
    public void setPresenter(VisionCameraContract.Presenter presenter) {
        mPresenter = presenter;
    }

    public class UsbReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                if (comm != null) comm.onUsbAttached(true, VisionCameraFragment.this);
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                if (comm != null) comm.onUsbAttached(false, VisionCameraFragment.this);
            }
        }
    }
}
