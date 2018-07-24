package ch.makezurich.conqueringlastmile.fragment.intro;


import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import ch.makezurich.conqueringlastmile.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class IntroSlideFragment extends Fragment {

    private @StringRes int titleRes;
    private @StringRes int subTitleRes;
    private @RawRes int animationRes;
    private View.OnClickListener animationClickListener;
    private @DrawableRes int bottomPicture;

    public IntroSlideFragment() {
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_intro_slide, container, false);

        final TextView title = view.findViewById(R.id.intro_title);
        final TextView subTitle = view.findViewById(R.id.intro_subtitle);
        final LottieAnimationView animationView = view.findViewById(R.id.intro_animation);

        title.setText(titleRes);
        subTitle.setText(subTitleRes);
        subTitle.setMovementMethod(new LinkMovementMethod());
        animationView.setAnimation(animationRes);

        if (animationClickListener != null) {
            animationView.setClickable(true);
            animationView.setFocusable(true);
            animationView.setOnClickListener(animationClickListener);
        }

        if (bottomPicture != 0) {
            ImageView bottomImageView = view.findViewById(R.id.bottom_picture);
            bottomImageView.setVisibility(View.VISIBLE);
            bottomImageView.setImageResource(bottomPicture);
        }

        return view;
    }

    public IntroSlideFragment withContent(@StringRes int titleRes, @StringRes int subTitleRes, @RawRes int animationRes) {
        this.titleRes = titleRes;
        this.subTitleRes = subTitleRes;
        this.animationRes = animationRes;
        return this;
    }

    public IntroSlideFragment withAnimationClickListener(View.OnClickListener animationClickListener) {
        this.animationClickListener = animationClickListener;
        return this;
    }

    public IntroSlideFragment withBottomPicture(@DrawableRes int bottomPicture) {
        this.bottomPicture = bottomPicture;
        return this;
    }
}
