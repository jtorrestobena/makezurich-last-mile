package ch.makezurich.conqueringlastmile.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ch.makezurich.conqueringlastmile.R;
import ch.makezurich.ttnandroidapi.datastorage.api.Device;

/*
 * Copyright 2018 Jose Antonio Torres Tobena / bytecoders
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SendPayloadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SendPayloadFragment extends BaseFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private List<Device> devices;
    private EditText payloadText;
    private String selectedDevice = "";
    private List<String> deviceNames = new ArrayList<>();
    private ProgressBar sendProgress;
    private OnSendPayloadRequest mListener;

    public SendPayloadFragment() {
        // Required empty public constructor
    }

    public static SendPayloadFragment newInstance() {
        return new SendPayloadFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_send_payload, container, false);
        payloadText = view.findViewById(R.id.payload_hex_input);
        sendProgress = view.findViewById(R.id.sendProgress);

        for (Device d : this.devices) {
            deviceNames.add(d.getName());
        }
        Spinner spinner = view.findViewById(R.id.spinnerSelectDevice);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item,deviceNames);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        FloatingActionButton fab = view.findViewById(R.id.fab_send_payload);
        fab.setOnClickListener(this);

        return view;
    }

    public SendPayloadFragment setDevices(List<Device> devices) {
        this.devices = devices;
        return this;
    }

    public interface OnSendPayloadRequest {
        void onPayloadRequest(@NonNull String device, @NonNull final String payloadHex, @NonNull final SendPayloadFragment sendPayloadFragment);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSendPayloadRequest) {
            mListener = (OnSendPayloadRequest) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSendPayloadRequest");
        }
    }

    @Override
    public void onClick(View view) {
        // Clicked on the fab. Check if we can send payload
        checkPayload();
        final String payloadHex = payloadText.getText().toString();
        if (selectedDevice == null || selectedDevice.isEmpty()) {
            Toast.makeText(getContext(), R.string.no_device_selected, Toast.LENGTH_LONG).show();
            return;
        }
        if (!payloadHex.isEmpty()) {
            mListener.onPayloadRequest(selectedDevice, payloadHex, this);
        } else {
            Toast.makeText(getContext(), R.string.payload_empty_msg, Toast.LENGTH_LONG).show();
        }
    }

    private void checkPayload() {
        final String payload = payloadText.getText().toString();
        if((payload.length()%2)!=0) {
            // Add leading 0 to the last byte in case the input length is even
            final String payloadCorrected = new StringBuilder(payload).insert(payload.length() - 1, "0").toString();
            payloadText.setText(payloadCorrected);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selectedDevice = deviceNames.get(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        selectedDevice = "";
    }

    public void setSendProgress(boolean progress) {
        sendProgress.setVisibility(progress ? View.VISIBLE : View.INVISIBLE);
    }
}
