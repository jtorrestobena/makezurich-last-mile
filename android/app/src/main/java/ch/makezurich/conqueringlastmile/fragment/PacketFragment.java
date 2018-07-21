package ch.makezurich.conqueringlastmile.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ch.makezurich.conqueringlastmile.R;
import ch.makezurich.conqueringlastmile.TTNApplication;
import ch.makezurich.ttnandroidapi.mqtt.api.data.Packet;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnPacketFragmentSelectionListener}
 * interface.
 */
public class PacketFragment extends BaseFragment implements TTNApplication.TTNSessionListener {

    private OnPacketFragmentSelectionListener mListener;
    private TTNApplication ttnApp;

    private RecyclerView recyclerView;
    private List<Packet> sessionPackets;
    private MyPacketRecyclerViewAdapter packetRecyclerViewAdapter;
    private View emptyView;
    private FloatingActionButton saveSessionButton;

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            ttnApp.getSessionPackets().remove(((MyPacketRecyclerViewAdapter.ViewHolder) viewHolder).mItem);
            refreshFrames();
        }
    };

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

        saveSessionButton = view.findViewById(R.id.fab_save_session);
        saveSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSession();
            }
        });

        // Set the adapter
        recyclerView = view.findViewById(R.id.list);
        sessionPackets = ttnApp.getSessionPackets();
        packetRecyclerViewAdapter = new MyPacketRecyclerViewAdapter(sessionPackets, mListener);
        recyclerView.setAdapter(packetRecyclerViewAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        emptyView = view.findViewById(R.id.empty_view);
        if (sessionPackets == null || sessionPackets.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            saveSessionButton.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!sessionPackets.isEmpty()) {
            refreshFrames();
        }
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
                refreshFrames();
            }
        });
    }

    private void refreshFrames() {
        packetRecyclerViewAdapter.notifyDataSetChanged();
        if (!sessionPackets.isEmpty()) {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            saveSessionButton.setVisibility(View.VISIBLE);
            recyclerView.smoothScrollToPosition(sessionPackets.size() - 1);
        } else {
            recyclerView.setVisibility(View.GONE);
            saveSessionButton.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void saveSession() {
        final EditText sessionName = new EditText(getContext());
        sessionName.setText(String.format("Session_%s",
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.save_session_dialog_title)
                .setIcon(R.drawable.ic_baseline_save_24px)
                .setMessage(R.string.save_session_dialog_message)
                .setView(sessionName)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final String sSessionName = sessionName.getText().toString();
                        if (!sSessionName.isEmpty()) {
                            boolean wasSaved = ttnApp.saveSession(sSessionName, false);
                            if (!wasSaved) {
                                // Check if user wants to overwrite
                                AlertDialog.Builder overwiteOKCancel = new AlertDialog.Builder(getContext());
                                overwiteOKCancel.setTitle(R.string.session_exist_title)
                                        .setIcon(R.drawable.ic_baseline_save_24px)
                                        .setMessage(String.format(getString(R.string.session_exist_message), sSessionName))
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                ttnApp.saveSession(sSessionName, true);
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null);
                                overwiteOKCancel.create().show();
                            }
                        } else {
                            Toast.makeText(getContext(), R.string.please_enter_session_name, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    @Override
    public void onSessionRefresh(List<Packet> sessionPackets) {
        this.sessionPackets = sessionPackets;
        packetRecyclerViewAdapter = new MyPacketRecyclerViewAdapter(this.sessionPackets, mListener);
        recyclerView.setAdapter(packetRecyclerViewAdapter);
        refreshFrames();
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
