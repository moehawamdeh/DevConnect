package org.ieeemadc.devconnect.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class AddressResultReceiver extends ResultReceiver {
    public void setListener(OnReceiveListener listener) {
        mListener = listener;
    }

    private OnReceiveListener mListener;
    public String getAddressOutput() {
        return mAddressOutput;
    }

    private String mAddressOutput;
    public AddressResultReceiver(Handler handler) {
        super(handler);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        if (resultData == null) {
            return;
        }

        // Display the address string
        // or an error message sent from the intent service.
        mAddressOutput = resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY);
        if (mAddressOutput == null) {
            mAddressOutput = "";
        }
        //displayAddressOutput();

        // Show a toast message if an address was found.
        if (resultCode == FetchAddressIntentService.SUCCESS_RESULT){}
            mListener.onReceive(mAddressOutput);
        }


    public interface OnReceiveListener {
        void onReceive(String location);
    }

}