package ch.makezurich.conqueringlastmile.fragment;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ch.makezurich.conqueringlastmile.R;
import ch.makezurich.conqueringlastmile.datastorage.DeviceProfile;
import ch.makezurich.conqueringlastmile.fragment.DevicesFragment.OnListFragmentInteractionListener;
import ch.makezurich.ttnandroidapi.datastorage.api.Device;
import de.hdodenhof.circleimageview.CircleImageView;
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
 * {@link RecyclerView.Adapter} that can display a {@link Device} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MydevicesRecyclerViewAdapter extends RecyclerView.Adapter<MydevicesRecyclerViewAdapter.ViewHolder> {

    private final List<DeviceProfile> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MydevicesRecyclerViewAdapter(List<DeviceProfile> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_devices, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mDeviceNameView.setText(holder.mItem.getFriendlyName());

        final Bitmap picture = holder.mItem.getPicture();
        if (picture != null) {
            holder.mDevicePicImageview.setImageBitmap(picture);
        }

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
        final TextView mDeviceNameView;
        final CircleImageView mDevicePicImageview;
        DeviceProfile mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mDeviceNameView = view.findViewById(R.id.device_name);
            mDevicePicImageview = view.findViewById(R.id.device_picture);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDeviceNameView.getText() + "'";
        }
    }
}
