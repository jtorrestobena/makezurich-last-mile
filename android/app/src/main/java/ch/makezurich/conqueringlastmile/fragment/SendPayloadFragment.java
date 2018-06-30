package ch.makezurich.conqueringlastmile.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import ch.makezurich.conqueringlastmile.R;
import ch.makezurich.ttnandroidapi.datastorage.api.Device;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SendPayloadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SendPayloadFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private List<Device> devices;
    private EditText payloadText;
    private String selectedDevice = "";
    private List<String> deviceNames = new ArrayList<>();
    private ProgressBar sendProgress;

    public SendPayloadFragment() {
        // Required empty public constructor
    }

    public static SendPayloadFragment newInstance() {
        SendPayloadFragment fragment = new SendPayloadFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        Spinner spinner = (Spinner) view.findViewById(R.id.spinnerSelectDevice);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,deviceNames);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        return view;
    }

    public SendPayloadFragment setDevices(List<Device> devices) {
        this.devices = devices;
        return this;
    }

    public String getPayloadHex() {
        checkPayload();
        return payloadText.getText().toString();
    }

    private void checkPayload() {
        final String payload = payloadText.getText().toString();
        if((payload.length()%2)!=0) {
            // Add leading 0 to the last byte in case the input length is even
            final String payloadCorrected = new StringBuilder(payload).insert(payload.length() - 1, "0").toString();
            payloadText.setText(payloadCorrected);
        }
    }

    public String getSelectedDevice() {
        return selectedDevice;
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
