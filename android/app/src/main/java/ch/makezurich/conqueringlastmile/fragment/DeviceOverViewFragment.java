package ch.makezurich.conqueringlastmile.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.makezurich.conqueringlastmile.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DeviceOverViewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DeviceOverViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeviceOverViewFragment extends Fragment {

    private static final String ARG_FRAME_COUNT = "ARG_FRAME_COUNT";
    private static final String ARG_LAST_SEEN = "ARG_LAST_SEEN";

    private int mNumDevices;
    private String mLastSeenDate;

    public DeviceOverViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param packetNumber Number of packets received
     * @param lastSeen A string indicating a date where the device sent last frame
     * @return A new instance of fragment DeviceOverViewFragment.
     */
    public static DeviceOverViewFragment newInstance(int packetNumber, String lastSeen) {
        DeviceOverViewFragment fragment = new DeviceOverViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FRAME_COUNT, packetNumber);
        args.putString(ARG_LAST_SEEN, lastSeen);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNumDevices = getArguments().getInt(ARG_FRAME_COUNT);
            mLastSeenDate = getArguments().getString(ARG_LAST_SEEN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_device_over_view, container, false);

        final TextView frameNumberTextView = v.findViewById(R.id.tv_frameNum);
        frameNumberTextView.setText(String.valueOf(mNumDevices));

        final TextView lastSeenTextView = v.findViewById(R.id.tv_lastSeen);
        lastSeenTextView.setText(mLastSeenDate);

        return v;
    }
}
