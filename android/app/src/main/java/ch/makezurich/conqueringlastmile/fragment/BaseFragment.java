package ch.makezurich.conqueringlastmile.fragment;

import android.support.v4.app.Fragment;

import ch.makezurich.conqueringlastmile.MainActivity;
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
