package com.apg.app.qrcodeimagecapture;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.apg.app.qrcodeimagecapture.camera.qrcode.QrCodeCameraContract;
import com.apg.app.qrcodeimagecapture.camera.qrcode.QrCodeCameraFragment;
import com.apg.app.qrcodeimagecapture.camera.qrcode.QrCodeCameraFragment_;
import com.apg.app.qrcodeimagecapture.camera.vision.VisionCameraContract;
import com.apg.app.qrcodeimagecapture.camera.vision.VisionCameraFragment;
import com.apg.app.qrcodeimagecapture.camera.vision.VisionCameraFragment_;
import com.apg.app.qrcodeimagecapture.camera.vision.VisionCameraPresenter;
import com.apg.app.qrcodeimagecapture.camera.vision.VisionCameraRemote;
import com.apg.app.qrcodeimagecapture.LoadingDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Vijaya.Karri on 29/8/2017.
 */
public class ActivityCameraTakePicture extends AppCompatActivity {

    private QrCodeCameraContract.Remote QRRemote;
    private VisionCameraRemote mVisionRemote;

    public static final String EXTRA_SCAN_GENERATE_QR = "data_generate_qr";

    private String generatedQr = "";

    private int counter = 0;

    public String currentPhotoPath;

    protected VisionCameraPresenter visionPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_sdo_qip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setupToolbar(toolbar);
        setupCameraForQrCode();
    }


    private void setupToolbar(Toolbar toolbar) {
        if (toolbar != null) {
            toolbar.setTitle("Camera Scan QR");
        }
    }

    private void setupCameraForQrCode() {
        visionPresenter = new VisionCameraPresenter();

        VisionCameraFragment visionFragment = (VisionCameraFragment)
                getSupportFragmentManager().findFragmentByTag("vision");
        QrCodeCameraFragment qrFragment = (QrCodeCameraFragment)
                getSupportFragmentManager().findFragmentByTag("qr");

        if (visionFragment == null || qrFragment == null) {

            visionFragment = VisionCameraFragment_.builder()
                    .isAutoFocus(true)
                    .isUseFlash(false)
                    .build();

            qrFragment = QrCodeCameraFragment_.builder().build();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contentContainer, visionFragment, "vision")
                    .add(R.id.contentContainer, qrFragment, "qr")
                    .commit();
        }

        visionPresenter.setView(visionFragment);
        visionFragment.setCommunicator(visionComm);
        qrFragment.setCommunicator(qrComm);
        getSupportFragmentManager().executePendingTransactions();
        enableQrCodeDetection();
    }

private QrCodeCameraContract.Communicator qrComm = new QrCodeCameraContract.Communicator() {
        @Override
        public void onRemoteReady(QrCodeCameraContract.Remote remote) {
            QRRemote = remote;
        }

        @Override
        public void onInputQrCodeManually(String s) {
            if (QRRemote != null) {
                QRRemote.setRecentQrCode(s);
            }
        }
    };


    private VisionCameraContract.Communicator visionComm = new VisionCameraContract.Communicator() {

        @Override
        public void onCameraReady(VisionCameraRemote remote) {
            mVisionRemote = remote;
        }

        @Override
        public void onShutter() {
            // do nothing.
        }

        @Override
        public void onDetectBarcode(String barcode) {
            if(counter == 0) { // first time, when barcode is detected, taking photo.
                disableQrCodeDetection();
                takePhoto();
            }
            else {
                disableQrCodeDetection();
                if (QRRemote != null) {
                    QRRemote.setRecentQrCode(barcode);
                    generatedQr = barcode;
                    onBackPressed();
                }
            }
        }

        @Override
        public void onPictureTaken(byte[] bytes) {
            counter ++;
            if(counter <= 3) { // picture will be taken 3 times. Later (counter > 3) qr code willbe detected.
                TakePhotoInBackGround photoInBackGround = new TakePhotoInBackGround();
                photoInBackGround.execute(bytes);
            }else{
                enableQrCodeDetection();
            }

        }

        @Override
        public void onPermissionDenied() {
            finish();
        }

        @Override
        public void onUsbAttached(boolean hardwareKeyboardAvailable, VisionCameraRemote remote) {

        }
    };


    private void takePhoto(){
        if(mVisionRemote != null) {
            mVisionRemote.takePhoto();
        }
    }


    private void enableQrCodeDetection() {
        // start receive result
        // remote to vision to start detect qr-code
        if (mVisionRemote != null) {
            mVisionRemote.startDetectBarcode();
        }
    }


    private void disableQrCodeDetection() {
        // start receive result
        // remote to vision to start detect qr-code
        if (mVisionRemote != null) {
            mVisionRemote.stopDetectBarcode();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        intent.putExtra(EXTRA_SCAN_GENERATE_QR, generatedQr);
        super.onBackPressed();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat(getString(R.string.create_image_fil_timestamp), Locale.US).format(new Date());
        String imageFileName = getString(R.string.create_image_fil_name) + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                getString(R.string.create_image_fil_jpg),         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = getString(R.string.create_image_photo_path) + imageFile.getAbsolutePath();
        return imageFile;
    } // End of createImageFile()

    private class TakePhotoInBackGround extends AsyncTask<byte[],String, Bitmap> {
        @Override
        protected void onPreExecute() {
            //loadingDialog.show();
        }

        @Override
        protected Bitmap doInBackground(byte[]... params) {
            publishProgress("Taking Photo..."+counter);
            byte[] bytes = params[0];

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 700, 700);
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

           // Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            FileOutputStream out = null;
            try {
                if(photoFile != null) {
                    out = new FileOutputStream(photoFile);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                }
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return bmp;
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }


        @Override
        protected void onPostExecute(Bitmap qrCodeBitmap) {
            //loadingDialog.dismiss();
            if (QRRemote != null) {
                QRRemote.setToast();
            }
            takePhoto();
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // The new size we need to scale to
        final int REQUIRED_SIZE = 400;

        // Find the correct scale value. It should be the power of 2.
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE &&
                options.outHeight / scale / 2 >= REQUIRED_SIZE) {
            scale *= 2;
        }

        return scale;

    } // End of calculateInSampleSize()
}
