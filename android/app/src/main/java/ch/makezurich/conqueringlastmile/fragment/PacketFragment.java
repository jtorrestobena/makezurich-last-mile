package ch.makezurich.conqueringlastmile.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ch.makezurich.conqueringlastmile.R;
import ch.makezurich.conqueringlastmile.TTNApplication;
import ch.makezurich.ttnandroidapi.mqtt.api.AndroidTTNListener;
import ch.makezurich.ttnandroidapi.mqtt.api.data.Packet;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnPacketFragmentSelectionListener}
 * interface.
 */
public class PacketFragment extends BaseFragment implements AndroidTTNListener {

    private OnPacketFragmentSelectionListener mListener;
    private TTNApplication ttnApp;

    private RecyclerView recyclerView;
    private List<Packet> sessionPackets;
    private MyPacketRecyclerViewAdapter packetRecyclerViewAdapter;
    private View emptyView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PacketFragment() {
    }

    @SuppressWarnings("unused")
    public static PacketFragment newInstance() {
        PacketFragment fragment = new PacketFragment();
        return new PacketFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ttnApp = (TTNApplication) getContext().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_packet_list, container, false);

        // Set the adapter
        recyclerView = view.findViewById(R.id.list);
        sessionPackets = ttnApp.getSessionPackets();
        packetRecyclerViewAdapter = new MyPacketRecyclerViewAdapter(sessionPackets, mListener);
        recyclerView.setAdapter(packetRecyclerViewAdapter);

        if (sessionPackets == null || sessionPackets.isEmpty()){
            emptyView = view.findViewById(R.id.empty_view);
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ttnApp.addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ttnApp.removeListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPacketFragmentSelectionListener) {
            mListener = (OnPacketFragmentSelectionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPacketFragmentSelectionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onError(Throwable _error) {

    }

    @Override
    public void onTLSError(Throwable _error) {

    }

    @Override
    public void onConnected(boolean _reconnect) {

    }

    @Override
    public void onPacket(Packet _message) {
        // Refresh packets
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (emptyView != null) {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                packetRecyclerViewAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(sessionPackets.size() - 1);
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnPacketFragmentSelectionListener {
        void onPacketSelected(Packet packet);
    }
}
