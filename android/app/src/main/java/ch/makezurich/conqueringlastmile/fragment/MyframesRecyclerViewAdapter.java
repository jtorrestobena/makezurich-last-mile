package ch.makezurich.conqueringlastmile.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ch.makezurich.conqueringlastmile.R;
import ch.makezurich.conqueringlastmile.fragment.FrameFragment.OnFrameListFragmentInteractionListener;
import ch.makezurich.ttnandroidapi.datastorage.api.Frame;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Frame} and makes a call to the
 * specified {@link OnFrameListFragmentInteractionListener}.
 */
public class MyframesRecyclerViewAdapter extends RecyclerView.Adapter<MyframesRecyclerViewAdapter.ViewHolder> {

    private final List<Frame> mValues;
    private final FrameFragment.OnFrameListFragmentInteractionListener mListener;

    public MyframesRecyclerViewAdapter(List<Frame> items, OnFrameListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_frame, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        final Frame frame = mValues.get(position);
        holder.mDeviceIdView.setText("Device: " + frame.getDeviceId());
        holder.mRawDataView.setText("Payload B64: " + frame.getRaw());
        holder.mHexPayloadView.setText("Payload HEX: " + frame.getHexString());
        holder.mTimeStampView.setText("Timestamp: " + frame.getTimeStamp());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mDeviceIdView;
        public final TextView mRawDataView;
        public final TextView mHexPayloadView;
        public final TextView mTimeStampView;
        public Frame mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDeviceIdView = (TextView) view.findViewById(R.id.device_id);
            mRawDataView = (TextView) view.findViewById(R.id.raw_data);
            mHexPayloadView = (TextView) view.findViewById(R.id.hex_data);
            mTimeStampView = (TextView) view.findViewById(R.id.frame_timestamp);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mRawDataView.getText() + "'";
        }
    }
}
