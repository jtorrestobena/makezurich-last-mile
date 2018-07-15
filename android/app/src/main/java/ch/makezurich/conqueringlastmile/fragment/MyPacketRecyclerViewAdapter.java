package ch.makezurich.conqueringlastmile.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ch.makezurich.conqueringlastmile.R;
import ch.makezurich.conqueringlastmile.fragment.PacketFragment.OnPacketFragmentSelectionListener;
import ch.makezurich.ttnandroidapi.mqtt.api.data.Packet;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Packet} and makes a call to the
 * specified {@link OnPacketFragmentSelectionListener}.
 */
public class MyPacketRecyclerViewAdapter extends RecyclerView.Adapter<MyPacketRecyclerViewAdapter.ViewHolder> {

    private final List<Packet> mValues;
    private final OnPacketFragmentSelectionListener mListener;

    public MyPacketRecyclerViewAdapter(List<Packet> items, OnPacketFragmentSelectionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_packet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mDeviceIdView.setText(holder.mItem.getDevId());
        holder.mDeviceEUIView.setText(holder.mItem.getDevEUI());
        holder.mPortView.setText(String.valueOf(holder.mItem.getPort()));
        holder.mCounterView.setText(String.valueOf(holder.mItem.getCounter()));
        holder.mPayloadView.setText(holder.mItem.getPayload());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onPacketSelected(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mDeviceIdView;
        final TextView mDeviceEUIView;
        final TextView mPortView;
        final TextView mCounterView;
        final TextView mPayloadView;
        public Packet mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDeviceIdView = view.findViewById(R.id.dev_id);
            mDeviceEUIView = view.findViewById(R.id.dev_eui);
            mPortView  = view.findViewById(R.id.port);
            mCounterView = view.findViewById(R.id.counter);
            mPayloadView = view.findViewById(R.id.payload);
        }

        @Override
        public String toString() {
            return super.toString() + " devid '" + mDeviceIdView.getText() +
                    "' dev eui '"+ mDeviceEUIView.getText() + "'";
        }
    }
}
