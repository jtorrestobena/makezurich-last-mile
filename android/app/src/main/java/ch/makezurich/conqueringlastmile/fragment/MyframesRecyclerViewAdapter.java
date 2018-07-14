package ch.makezurich.conqueringlastmile.fragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ch.makezurich.conqueringlastmile.R;
import ch.makezurich.conqueringlastmile.fragment.FrameFragment.OnFrameListFragmentInteractionListener;
import ch.makezurich.ttnandroidapi.datastorage.api.Frame;

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
 * {@link RecyclerView.Adapter} that can display a {@link Frame} and makes a call to the
 * specified {@link OnFrameListFragmentInteractionListener}.
 */
public class MyframesRecyclerViewAdapter extends RecyclerView.Adapter<MyframesRecyclerViewAdapter.ViewHolder> {

    private final List<Frame> mValues;
    private final FrameFragment.OnFrameListFragmentInteractionListener mListener;

    public MyframesRecyclerViewAdapter(Context context, List<Frame> items, OnFrameListFragmentInteractionListener listener) {
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
        holder.mTimeStampView.setText("Timestamp: " + frame.getTimestampString());

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
        final View mView;
        final TextView mDeviceIdView;
        final TextView mRawDataView;
        final TextView mHexPayloadView;
        final TextView mTimeStampView;
        public Frame mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mDeviceIdView = view.findViewById(R.id.device_id);
            mRawDataView = view.findViewById(R.id.raw_data);
            mHexPayloadView = view.findViewById(R.id.hex_data);
            mTimeStampView = view.findViewById(R.id.frame_timestamp);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mRawDataView.getText() + "'";
        }
    }
}
