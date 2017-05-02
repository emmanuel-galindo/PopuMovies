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

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.popumovies.adapter.MovieAdapter;
import com.popumovies.data.MovieContract.MovieEntry;
import com.popumovies.sync.MovieSyncAdapter;
import com.popumovies.utils.PrefUtil;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";
    private static final int MOVIE_LOADER = 0;

    private MovieAdapter mMovieAdapter;
    private RecyclerView mRecyclerView;
    private Menu mMenu;
    private int mPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(int pos, long movieId);
    }

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG,"onCreate");
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG,"onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        mMovieAdapter = new MovieAdapter(getActivity(), null);
        mRecyclerView.setAdapter(mMovieAdapter);
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        int item = PrefUtil.getInt(getActivity(), getString(R.string.pref_filter_label),
                R.id.action_sort_popularity);

        MenuItem menuItem = menu.findItem(item);
        if (menuItem == null)
            menuItem = menu.findItem(R.id.action_sort_popularity);

        menuItem.setChecked(true);
        mMenu = menu;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovie();
            return true;
        }
        else if (
                (id == R.id.action_sort_popularity) ||
                        (id == R.id.action_sort_votes) ||
                        (id == R.id.action_favorites))  {

            PrefUtil.setInt(getActivity(), getString(R.string.pref_filter_label), id);
            mMenu.findItem(R.id.action_sort_popularity).setChecked(false);
            mMenu.findItem(R.id.action_sort_votes).setChecked(false);
            mMenu.findItem(R.id.action_favorites).setChecked(false);
            mMenu.findItem(id).setChecked(true);
            restartLoader();
        }
        else if (id == android.R.id.home) {
            getActivity().onBackPressed();
        }


        return super.onOptionsItemSelected(item);
    }


    private void updateMovie() {
        Log.d(LOG_TAG, "in updateMovie");
        MovieSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("scrollpos")) {
                int scrollToPos = savedInstanceState
                        .getInt("scrollpos");
                setPosition(scrollToPos);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // - Make use of the LayoutManager capabilities to help saving the scroll position
        // When rotating, it will be only useful when returning to the orientation in where it was set.
        if (mRecyclerView != null) {
            outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mRecyclerView.getLayoutManager().onSaveInstanceState());
            int scrollpos = ((GridLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
            outState.putInt("scrollpos", scrollpos);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri movieUri;
        int idFilter = PrefUtil.getInt(getActivity(),getString(R.string.pref_filter_label));
        if (idFilter == R.id.action_sort_popularity)
            movieUri = MovieEntry.buildPopularMovieUri();
        else if (idFilter == R.id.action_favorites)
            movieUri = MovieEntry.buildFavoriteMovieUri();
        else if (idFilter == R.id.action_sort_votes)
            movieUri = MovieEntry.buildHighRatedMovieUri();
        else
            movieUri = MovieEntry.buildPopularMovieUri();

        return new CursorLoader(getActivity(), movieUri,
                null, null, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int idFilter = PrefUtil.getInt(getActivity(),getString(R.string.pref_filter_label));
        // if there's no data (as in initial load), refresh
        if (data.getCount() > 0 || idFilter == R.id.action_favorites) {
            mMovieAdapter.swapCursor(data);
            if (mPosition != ListView.INVALID_POSITION) {
                Log.d(LOG_TAG,"onLoadFinished - scrollToPosition " + mPosition);
                mRecyclerView.scrollToPosition(mPosition);
            }
        } else {
            updateMovie();
        }

    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mMovieAdapter.swapCursor(null);
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }


    public void setPositionInList(int pos) {
        if (mRecyclerView.getLayoutManager() != null)
            mRecyclerView.getLayoutManager().scrollToPosition(pos);
    }

    public void setPosition(int position) {
        mPosition = position;
    }
}


