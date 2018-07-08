package ch.makezurich.conqueringlastmile.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.makezurich.conqueringlastmile.R;
import ch.makezurich.conqueringlastmile.util.Util;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnDashboardSelectionListener} interface
 * to handle interaction events.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends BaseFragment {
    private static final String ARG_NUMDEV = "numdevices";
    private static final String ARG_NUMFRAME = "numframes";

    private int numDevices;
    private int numFrames;

    private OnDashboardSelectionListener mListener;
    private View.OnClickListener mViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onItemPressed(view.getId());
        }
    };

    public DashboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DashboardFragment.
     */
    public static DashboardFragment newInstance(int numDevices, int numFrames) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_NUMDEV, numDevices);
        args.putInt(ARG_NUMFRAME, numFrames);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            numDevices = getArguments().getInt(ARG_NUMDEV);
            numFrames = getArguments().getInt(ARG_NUMFRAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT ?
                R.layout.fragment_dashboard : R.layout.fragment_dashboard_land, container, false);
        setupView(view);
        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // We handle orientation change and layout here, otherwise we have to set it for the entire MainActivity
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup rootView = (ViewGroup) getView();
        View newView = inflater.inflate(newConfig.orientation == ORIENTATION_PORTRAIT ?
                R.layout.fragment_dashboard : R.layout.fragment_dashboard_land, rootView, false);
        // Remove all the existing views from the root view.
        // This is also a good place to recycle any resources you won't need anymore
        rootView.removeAllViews();
        rootView.addView(newView);
        setupView(newView);
    }

    private void setupView(View rootView) {
        for (View navEntry : Util.getViewsByTag((ViewGroup) rootView, "nav_entry")) {
            navEntry.setOnClickListener(mViewClickListener);
        }

        if (numDevices > 0) {
            final TextView devicesTextView = rootView.findViewById(R.id.tv_device_data_sum);
            devicesTextView.setText(String.format(getString(R.string.devices_dash_sum_plural), numDevices));
        }

        if (numFrames > 0) {
            final TextView framesTextView = rootView.findViewById(R.id.tv_device_frames_sum);
            framesTextView.setText(String.format(getString(R.string.frames_dash_sum_plural), numFrames));
        }
    }

    public void onItemPressed(int menuId) {
        if (mListener != null) {
            mListener.onDashboardSelection(menuId);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDashboardSelectionListener) {
            mListener = (OnDashboardSelectionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDashboardSelectionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnDashboardSelectionListener {
        void onDashboardSelection(int navItem);
    }
}
