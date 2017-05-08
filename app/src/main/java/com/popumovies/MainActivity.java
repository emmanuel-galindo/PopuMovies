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

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import static com.popumovies.R.id.main_container;
import static com.popumovies.R.id.movie_detail_container;

//import static com.popumovies.R.id.fragment_main;

public class MainActivity
        extends AppCompatActivity
        implements
            MainActivityFragment.Callback, ViewPagerFragment.Callback {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static final String VIEWPAGERFRAGMENT_TAG = "VFTAG";
    public static final String MAINFRAGMENT_TAG = "MFTAG";
    public static final String MOVIE_SAVE_KEY="MOVIE_ID";
    public static final String POSITION_SAVE_KEY = "POSITION";

    private boolean mTwoPane;
    private long mMovieId = 0;
    private MainActivityFragment mf;
    /* position will be set onsaveinstance, onitemselected, when viewpager change (callback)
    position will also propagate to the list fragment when showing detail pane on two, as cols
    are resized. And onBackPressed when on detail @ single pane, to position where the viewpager
    currenly is positioned
     */
    private int mPosition = ListView.INVALID_POSITION;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTwoPane = getResources().getBoolean(R.bool.has_two_panes);


        mf = (MainActivityFragment) getSupportFragmentManager()
                .findFragmentByTag(MAINFRAGMENT_TAG);
        if (mf == null)
            mf = new MainActivityFragment();
        getSupportFragmentManager().beginTransaction()
                    .replace(main_container, mf, MAINFRAGMENT_TAG)
                    .commit();

        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(POSITION_SAVE_KEY);
            mMovieId = savedInstanceState.getLong(MOVIE_SAVE_KEY);
            //TODO: duplicated code!!
            // Here we handle the rotation when a movie is selected
            if (mPosition >= 0 && mMovieId > 0) {
                Bundle args = new Bundle();
                if (mTwoPane) {
                    if (getSupportActionBar() != null)
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

                    // in Two pane, we show the movie in the right pane
                    DetailActivityFragment df = new DetailActivityFragment();
                    args.putString(DetailActivityFragment.DETAIL_MOVIEID, Long.toString(mMovieId));
                    df.setArguments(args);

                    findViewById(movie_detail_container).setVisibility(View.VISIBLE);

                    getSupportFragmentManager().beginTransaction()
                            .replace(movie_detail_container, df, DETAILFRAGMENT_TAG)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .addToBackStack(null)
                            .commit();

                    mf.setPosition(mPosition);
                }
                else {
                    // Here we handle the rotation from lands to port when a movie is selected,
                    // therefore we show the ViewPager and set the correct position for the
                    // Detail

                    // Including the ViewPager holder (for allow nested fragments)
                    ViewPagerFragment vp = new ViewPagerFragment();
                    vp.setArguments(args);
                    args.putInt(getString(R.string.arg_list_position_key), mPosition);

                    if (getSupportActionBar() != null)
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                    getSupportFragmentManager().beginTransaction()
                            .replace(main_container, vp, VIEWPAGERFRAGMENT_TAG)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .addToBackStack(null)
                            .commit();

                }
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setElevation(0f);
        }

    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG,"onResume");
        super.onResume();
    }

    // This is meant to be executed when a user press the back button on the detail view
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(LOG_TAG,"onRestart");
        MainActivityFragment mf = (MainActivityFragment)getSupportFragmentManager().
                findFragmentByTag(MAINFRAGMENT_TAG);
        if (mf != null) {
            // if data changes in detail activity, as favorites, we want it reflected on the
            // saved instance
            mf.restartLoader();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(LOG_TAG,"onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // Callback when a item is clicked.
    // If it is a single pane, it will use the pos, so Pager (for swipe between movies) work
    // If it is two pane, it replaces the fragment with a new one w/correct arguments
    @Override
    public void onItemSelected(int position, long movieId) {
        Log.d(LOG_TAG,"onItemSelected");
        mMovieId = movieId;
        mPosition = position;
        Bundle args = new Bundle();

        if (mTwoPane) {
            DetailActivityFragment df = new DetailActivityFragment();
            args.putString(DetailActivityFragment.DETAIL_MOVIEID, Long.toString(movieId));
            df.setArguments(args);

            findViewById(movie_detail_container).setVisibility(View.VISIBLE);

            getSupportFragmentManager().beginTransaction()
                    .replace(movie_detail_container, df, DETAILFRAGMENT_TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();

            // as layouts just changed, main list got narrow so
            // here we call the recyclerview scroll to recalculate position
            // setposition only won't work as MF keeps the same
            mf.setPositionInList(mPosition);
        }
        else {
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);


            // Including the ViewPager holder (for allow nested fragments)
            ViewPagerFragment vp = new ViewPagerFragment();
            vp.setArguments(args);
            args.putInt(getString(R.string.arg_list_position_key), mPosition);

            getSupportFragmentManager().beginTransaction()
                    .replace(main_container, vp, VIEWPAGERFRAGMENT_TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();

        }
    }

    @Override
    public void onBackPressed() {

        Log.d(LOG_TAG, "onBackPressed()");
        // If in twopane, and it is showing a detail, we want to close the detail. And also
        // to remove the flags that a movie is selected
        if (mTwoPane &&
                findViewById(movie_detail_container).getVisibility() == View.VISIBLE) {
            mMovieId = 0;
            getSupportFragmentManager().beginTransaction()
                    .remove(
                    getSupportFragmentManager()
                            .findFragmentByTag(DETAILFRAGMENT_TAG)
                    ).commit();
            findViewById(movie_detail_container).setVisibility(View.GONE);
        }

        // If we are not in two pane, and we are seeing the detail when back is pressed,
        // we want to remove the indication that a movie is selected, and to reposition the list
        // to the same movie the user last saw
        ViewPagerFragment vp = (ViewPagerFragment) getSupportFragmentManager()
                .findFragmentByTag(VIEWPAGERFRAGMENT_TAG);
        // We are checking if vp is null because in twoPane it returns null
        if (!mTwoPane && vp != null && vp.isVisible()) {
            // Here we remove the selected movie flag.
            // In this case, we need to setMovieId in the DetailFragment through the VPFragment,
            // as after this the onPause of VPFragment is executed by the lifecycle
            // and, for the rotation process, saves the movieId from the DFragment.
            vp.setMovieId(0);

            // Also, we want to keep the position from within the VPFragment navigation, so
            // the list repositions correctly
            mf.setPosition(vp.getCurrentPosition());
        }
        
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG,"onSaveInstanceState");
        outState.putLong(MOVIE_SAVE_KEY, mMovieId);
        outState.putInt(POSITION_SAVE_KEY, mPosition);

    }

    public void setMovieId(long movieId) {
        mMovieId = movieId;
    }
    public void setPosition(int position) {
        mPosition = position;
    }
}
