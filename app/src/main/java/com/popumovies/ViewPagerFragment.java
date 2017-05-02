/*
 * Copyright (C) 2017 Emmanuel Galindo (https://emmanuel-galindo.github.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.popumovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.popumovies.adapter.CursorPagerAdapter;
import com.popumovies.data.MovieContract;
import com.popumovies.utils.PrefUtil;

/**
 * Nested Fragment solution
 * Based on http://stackoverflow.com/questions/13379194/how-to-add-a-fragment-inside-a-viewpager-using-nested-fragment-android-4-2
 * As on the main activity we want to have just one framelayout, when needed that is replaced
 * by the resource for this fragment, that has the view pager xml resource.
 * Since 4.2 it is allowed to do nested fragments:
 * https://developer.android.com/about/versions/android-4.2.html#NestedFragments
 */

public class ViewPagerFragment extends Fragment
        implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = ViewPagerFragment.class.getSimpleName();
    private static final int ACTIVITY_DETAIL_LOADER = 1;

    private CursorPagerAdapter mPagerAdapter;
    private ViewPager mPager;
    private int mPosition;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void setMovieId(long movieId);
        void setPosition(int position);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG,"onCreateView");
        return inflater.inflate(R.layout.fragment_view_pager, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(LOG_TAG,"onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mPosition = arguments.getInt(getString(R.string.arg_list_position_key));
        }
        ActionBar sab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (sab != null) {
            sab.setDisplayShowTitleEnabled(false);
//            sab.setDisplayHomeAsUpEnabled(true);
        }

        mPager = (ViewPager) view.findViewById(R.id.viewPager);
        // getChildFragmentManager is the key for nested fragments
        mPagerAdapter = new CursorPagerAdapter<>(getChildFragmentManager(),
                DetailActivityFragment.class, new String[]{}, null);
        mPager.setAdapter(mPagerAdapter);
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        LoaderManager.LoaderCallbacks<Cursor> mCallbacks = this;
        getLoaderManager().initLoader(ACTIVITY_DETAIL_LOADER, null, mCallbacks);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onPause() {
        Log.d(LOG_TAG,"onPause()");

        // Remove the back (up) from the menu
        if (((AppCompatActivity)getActivity()).getSupportActionBar() != null)
            ((AppCompatActivity)getActivity()).getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(false);

//         For the case when the device is rotated, if two pane with this info
//         it will show the detail in the right pane, and it will position the left list
        DetailActivityFragment df = (DetailActivityFragment) mPagerAdapter
                .instantiateItem(mPager, mPager.getCurrentItem());
        ((ViewPagerFragment.Callback) getActivity()).setMovieId(df.getMovieId());
        Log.d(LOG_TAG,"current item =>"+mPager.getCurrentItem());
        ((ViewPagerFragment.Callback) getActivity()).setPosition(mPager.getCurrentItem());
        super.onPause();


    }

    @Override
    public void onResume() {
        super.onResume();

//        if (((AppCompatActivity)getActivity()).getSupportActionBar() != null)
//            ((AppCompatActivity)getActivity()).getSupportActionBar()
//                    .setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri movieUri;
        int idFilter = PrefUtil.getInt(getActivity(), getString(R.string.pref_filter_label));
        if (idFilter == R.id.action_favorites)
            movieUri = MovieContract.MovieEntry.buildFavoriteMovieUri();
        else if (idFilter == R.id.action_sort_votes)
            movieUri = MovieContract.MovieEntry.buildHighRatedMovieUri();
        else
            movieUri = MovieContract.MovieEntry.buildPopularMovieUri();

        return new CursorLoader(getActivity(), movieUri,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPagerAdapter.swapCursor(data, mPager);
        mPager.setCurrentItem(mPosition, false);

        // Show a little help about the swipe on the first time
        boolean showSwipeHelp = PrefUtil.getBoolean(getActivity(), getString(R.string.pref_swipehelp_label));
        if (!showSwipeHelp) {
            PrefUtil.setBoolean(getActivity(), getString(R.string.pref_swipehelp_label), true);
            Intent intent = new Intent(getActivity(), SwipeHelpActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public int getCurrentPosition() {
        return mPager.getCurrentItem();

    }

    public void setMovieId(long movieId) {
        DetailActivityFragment df = (DetailActivityFragment) mPagerAdapter
                .instantiateItem(mPager, mPager.getCurrentItem());
        df.setMovieId(movieId);
    }

}
