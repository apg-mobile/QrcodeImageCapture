package com.apg.app.qrcodeimagecapture.camera.instruction;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apg.app.qrcodeimagecapture.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import static com.apg.app.qrcodeimagecapture.camera.instruction.InstructionCameraFragment_.INSTRUCTION_ARG;

/**
 * Created by X-tivity on 1/16/2017 AD.
 */
@EFragment(R.layout.fragment_instruction_camera)
public class InstructionCameraFragment extends Fragment implements
        InstructionCameraContract.View,
        InstructionCameraContract.Remote {

    private InstructionCameraContract.Communicator mComm;

    @Bean
    protected InstructionCameraPresenter presenter;

    @FragmentArg
    protected String instruction;

    @ViewById
    protected LinearLayout llGuide;
    @ViewById
    protected TextView tvInstruction;

    @AfterViews
    protected void init() {
        setupGuide();
        presenter.setView(this);
        presenter.init();
    }

    private void setupGuide() {
        if (TextUtils.isEmpty(instruction)) {
            llGuide.setVisibility(View.GONE);
        } else {
            tvInstruction.setText(instruction);
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

    @Override
    public InstructionCameraContract.Communicator getCommunicator() {
        return mComm;
    }

    @Override
    public void setCommunicator(InstructionCameraContract.Communicator comm) {
        this.mComm = comm;
    }

    @Override
    public InstructionCameraContract.Remote getRemote() {
        return this;
    }

    @Override
    public void setInstruction(String instruction) {
        getArguments().putString(INSTRUCTION_ARG, instruction);
        this.instruction = instruction;
        setupGuide();
    }
}
