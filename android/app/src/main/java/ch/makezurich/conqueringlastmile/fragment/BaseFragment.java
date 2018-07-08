package ch.makezurich.conqueringlastmile.fragment;

import android.support.v4.app.Fragment;

import ch.makezurich.conqueringlastmile.MainActivity;

public abstract class BaseFragment extends Fragment {
    protected String fragmentTitle;
    protected int navItem;

    public BaseFragment withIdTitle(String fragmentTitle, int navItem) {
        this.fragmentTitle = fragmentTitle;
        this.navItem = navItem;

        return this;
    }

    @Override
    public void onResume() {
        super.onResume();
        final MainActivity activity = (MainActivity) getActivity();
        activity.setTitle(fragmentTitle);
        activity.setSelectedItem(navItem);
    }
}
