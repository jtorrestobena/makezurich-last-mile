package ch.makezurich.conqueringlastmile.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import ch.makezurich.conqueringlastmile.R;
import ch.makezurich.conqueringlastmile.TTNApplication;
import ch.makezurich.conqueringlastmile.util.ConnectionSettings;

public class ToolsFragment extends BaseFragment {

    private TTNApplication ttnApp;

    public ToolsFragment() {
        // Required empty public constructor
    }

    public static ToolsFragment newInstance() {
        return new ToolsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ttnApp = (TTNApplication) getContext().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tools, container, false);

        final ConnectionSettings connSettings = ttnApp.getConnectionSettings();
        if (connSettings.isTlsEnabled()) {
            v.findViewById(R.id.connection_status_secure).setVisibility(View.VISIBLE);
        } else {
            v.findViewById(R.id.connection_status_insecure).setVisibility(View.VISIBLE);
        }

        final TextView protocolTv = v.findViewById(R.id.tv_proto);
        protocolTv.setText(connSettings.getProtocol());

        final ExpandableListView certList = v.findViewById(R.id.expandableListViewCert);
        certList.setAdapter(new CertificateExpandableListViewAdapter(getContext(), connSettings.getClientCertificates()));

        return v;
    }
}
