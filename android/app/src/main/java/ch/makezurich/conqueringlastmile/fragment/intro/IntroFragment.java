package ch.makezurich.conqueringlastmile.fragment.intro;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ch.makezurich.conqueringlastmile.R;
import ch.makezurich.conqueringlastmile.activity.SettingsActivity;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link IntroFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IntroFragment extends Fragment {

    private IntroSlideAdapter adapter;
    private ViewPager viewPager;

    public IntroFragment() {
        setRetainInstance(true);
    }

    public static IntroFragment newInstance() {
        return new IntroFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new IntroSlideAdapter(getChildFragmentManager());
        adapter.addFrag(new IntroSlideFragment().withContent(R.string.hi_there, R.string.new_slide_right, R.raw.swipe_menu));
        adapter.addFrag(new IntroSlideFragment().withContent(R.string.welcome_ttn_account_question, R.string.ttn_account_explanation, R.raw.funky_chicken));
        adapter.addFrag(new IntroSlideFragment()
                .withContent(R.string.title_activity_settings, R.string.ttn_settings_description, R.raw.settings_blue)
                .withBottomPicture(R.drawable.ttn)
                .withAnimationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().startActivity(new Intent(getContext(), SettingsActivity.class));
                    }
                }));
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_intro, container, false);
        viewPager = v.findViewById(R.id.intro_slide_viewpager);
        viewPager.setAdapter(adapter);
        return v;
    }

    public void setConnectionSuccessful() {
        adapter.addFrag(new IntroSlideFragment().withContent(R.string.congrats, R.string.app_ready, R.raw.trophy)
                .withAnimationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                }));
        adapter.notifyDataSetChanged();
        viewPager.setCurrentItem(adapter.getCount() - 1);
    }

    private static class IntroSlideAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        public IntroSlideAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment) {
            mFragmentList.add(fragment);
        }
    }

}
