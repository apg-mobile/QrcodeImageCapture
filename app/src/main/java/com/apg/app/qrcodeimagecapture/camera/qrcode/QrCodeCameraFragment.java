package com.apg.app.qrcodeimagecapture.camera.qrcode;

import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.apg.app.qrcodeimagecapture.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import me.grantland.widget.AutofitTextView;

/**
 * Created by X-tivity on 1/10/2017 AD.
 */
@EFragment(R.layout.fragment_qr_camera)
public class QrCodeCameraFragment extends Fragment implements
        QrCodeCameraContract.View,
        QrCodeCameraContract.Remote {

    private QrCodeCameraContract.Communicator comm;

    @Bean
    protected QrCodeCameraPresenter presenter;

    @ViewById
    protected EditText edtQrCode;
    @ViewById
    protected Button btnSend;
    @ViewById
    protected LinearLayout llRecentQr;
    @ViewById
    protected AutofitTextView atvRecentQr;

    @AfterViews
    protected void init() {
        presenter.setView(this);
        presenter.init();
        edtQrCode.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && keyCode != KeyEvent.KEYCODE_BACK) {

                presenter.onSendQrCode();
            }

            return false;
        });
    }

    @Click
    protected void btnSend() {
        presenter.onSendQrCode();
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

    @Override
    public void onDetach() {
        presenter.onDetach();
        super.onDetach();
    }

    @Override
    public void setCommunicator(QrCodeCameraContract.Communicator comm) {
        this.comm = comm;
    }

    @Override
    public QrCodeCameraContract.Communicator getCommunicator() {
        return comm;
    }

    @Override
    public QrCodeCameraContract.Remote getRemote() {
        return this;
    }

    @Override
    public String getQrCode() {
        return edtQrCode.getText().toString();
    }

    @Override
    public void clearText() {
        edtQrCode.getText().clear();
    }

    @Override
    public void inputTextRequestFocus() {
        edtQrCode.requestFocus();
    }

    @Override
    public void showRecentQrCode(String s) {
        llRecentQr.setVisibility(View.VISIBLE);
        atvRecentQr.setText(s);
    }

    @Override
    public void hideRecentQrCode(String s) {
        llRecentQr.setVisibility(View.GONE);
    }

    @Override
    public void setRecentQrCode(String s) {
        presenter.OnRemoteSetRecentQrCode(s);
    }

    @Override
    public void setToast() {
        Toast.makeText(getActivity(),"Photo taken", Toast.LENGTH_SHORT).show();
    }
}
