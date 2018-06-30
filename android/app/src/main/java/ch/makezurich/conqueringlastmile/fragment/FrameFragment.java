package ch.makezurich.conqueringlastmile.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ch.makezurich.conqueringlastmile.MainActivity;
import ch.makezurich.conqueringlastmile.R;
import ch.makezurich.ttnandroidapi.datastorage.api.Frame;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFrameListFragmentInteractionListener}
 * interface.
 */
public class FrameFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private OnFrameListFragmentInteractionListener mListener;
    private List<Frame> frames = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FrameFragment() {
    }

    @SuppressWarnings("unused")
    public static FrameFragment newInstance() {
        return new FrameFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_frame_list, container, false);
        if (v instanceof SwipeRefreshLayout) {
            swipeRefreshLayout = (SwipeRefreshLayout) v;
            swipeRefreshLayout.setOnRefreshListener(this);
        }

        View listView = v.findViewById(R.id.list);

        // Set the adapter
        if (listView instanceof RecyclerView) {
            Context context = listView.getContext();
            recyclerView = (RecyclerView) listView;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new MyframesRecyclerViewAdapter(frames, mListener));
        }
        return v;
    }

    public FrameFragment setFrames(List<Frame> frames) {
        this.frames = frames;
        return this;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFrameListFragmentInteractionListener) {
            mListener = (OnFrameListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFrameListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRefresh() {
        new Thread() {
            @Override
            public void run() {
                frames = ((MainActivity) getActivity()).getNewFrames();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setAdapter(new MyframesRecyclerViewAdapter(frames, mListener));
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }.start();
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
    public interface OnFrameListFragmentInteractionListener {
        void onListFragmentInteraction(Frame item);
    }
/*
    public FrameFragment setRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        swipeRefreshLayout.setOnRefreshListener(listener);
        return this;
    }

    public void updateFrames(List<Frame> newFrameList) {
        swipeRefreshLayout.setRefreshing(false);
        frames = newFrameList;
        recyclerView.setAdapter(new MyframesRecyclerViewAdapter(frames, mListener));
    }
    */
}
