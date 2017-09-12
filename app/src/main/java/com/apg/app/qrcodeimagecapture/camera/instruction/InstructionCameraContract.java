package com.apg.app.qrcodeimagecapture.camera.instruction;

/**
 * Created by X-tivity on 1/16/2017 AD.
 */

public interface InstructionCameraContract {

    interface View {

        Communicator getCommunicator();

        void setCommunicator(Communicator comm);

        Remote getRemote();
    }

    interface Presenter {
        void setView(View view);

        void init();

        void onResume();

        void onPause();
    }

    interface Communicator {
        void onInstructionRemoteReady(Remote remote);
    }

    interface Remote {
        void setInstruction(String instruction);
    }
}
