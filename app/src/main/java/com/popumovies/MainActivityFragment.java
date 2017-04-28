package com.popumovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
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

    private MovieAdapter mMovieAdapter;
    private RecyclerView mRecyclerView;
    // --Commented out by Inspection (4/28/17 11:41 AM):private RecyclerView.Adapter mAdapter;
    private Menu mMenu;

    // when the list is layed out, if mposition is > 0 it will be used to scroll to position
//    private int mPosition;
    private int mPosition = ListView.INVALID_POSITION;
//    private boolean mTwoPane;


    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    // --Commented out by Inspection (4/28/17 11:41 AM):private static final String SELECTED_KEY = "selected_position";
    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";
    private static final int MOVIE_LOADER = 0;
    // --Commented out by Inspection (4/28/17 11:41 AM):private int mScrollToPos;
    // --Commented out by Inspection (4/28/17 11:41 AM):private SwipeRefreshLayout mSwipeRefreshLayout;

    public void setPositionInList(int pos) {
        if (mRecyclerView.getLayoutManager() != null)
            mRecyclerView.getLayoutManager().scrollToPosition(pos);
    }

    public void setPosition(int position) {
        mPosition = position;
    }


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
//        void initMoviePane(long movieId);
//        int getPosition();

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
        RecyclerView.LayoutManager mLayoutManager = mRecyclerView.getLayoutManager();

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

        //TODO: Add a refresh button in the action bar or implement the swipe down for refresh

        //TODO: this is a bug? the integer value of the layout items seems to change
        //TODO: therefore the best seem to keep the key, not the int value
        int item = PrefUtil.getInt(getActivity(), getString(R.string.pref_filter_label),
                R.id.action_sort_popularity);

        MenuItem menuItem = (MenuItem) menu.findItem(item);
        // FIXME: 4/5/16
        if (menuItem == null)
            menuItem = (MenuItem) menu.findItem(R.id.action_sort_popularity);

        menuItem.setChecked(true);
        mMenu = menu;

    }


    //TODO: when a new sort order is selected, it should go back to the top of the list
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // TODO: Prevent the user to press refresh when there's another refresh going on
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
            //updateMovie();
//            getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
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
//        FetchMoviesTask moviesTask = new FetchMoviesTask(getActivity(),
//                getActivity().getString(R.string.sort_popularity));
//        moviesTask.execute();
//        moviesTask = new FetchMoviesTask(getActivity(),
//                getActivity().getString(R.string.sort_votes));
//        moviesTask.execute();

    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
//                    String sortBy = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by popularity.

//        String sortOrder;
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
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        //todo: only do this when it is not favorite listing
        int idFilter = PrefUtil.getInt(getActivity(),getString(R.string.pref_filter_label));
        // if there's no data (as in initial load), refresh
        if (data.getCount() > 0 || idFilter == R.id.action_favorites) {
            mMovieAdapter.swapCursor(data);
            if (mPosition != ListView.INVALID_POSITION) {
                mRecyclerView.scrollToPosition(mPosition);
            }
        } else {
            updateMovie();
        }

//        getActivity().findViewById(R.id.content).setVisibility(View.VISIBLE);

        // if in twopane, we will init the fragment with the first item from list
//        if (mTwoPane) {
        //TODO: fucking normalize the use of twoPane. This option is the GO for me (now)
//        boolean twoPane = getResources().getBoolean(R.bool.has_two_panes);
//        if (twoPane && data.moveToFirst()) {
//            long id = data.getLong(data.getColumnIndexOrThrow("_id"));
//            ((MainActivityFragment.Callback) getActivity()).initMoviePane(id);
//            //TODO: in twopane, if no records, show a gray screen in movie detail fragment
//        }
//        }

        // As data is already loaded, restore using the layoutmanager inner saved state
//        if (mLayoutManagerSavedState != null)
//            mLayoutManager.onRestoreInstanceState(mLayoutManagerSavedState);
//        if (mScrollToPos > 0) {
//            mRecyclerView.scrollToPosition(mScrollToPos);
//        }

        // SmoothScrollToPosition does not work as expected. If the selected position in land
        // with 4 columns is 8, when rotate to portrait with 2 cols it will show 6/8 of the
        // 6&7 positions. The desired is to show 8-12 positions.

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


//    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//
//        if(savedInstanceState != null)
//        {
//            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
//            mRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
//        }
//    }

//    @Override
//    protected void onRestoreInstanceState(Parcelable state) {
//        if (state instanceof Bundle) {
//            Parcelable savedRecyclerLayoutState = ((Bundle) state).getParcelable(BUNDLE_RECYCLER_LAYOUT);
//        }
//        super.onRestoreInstanceState(state);
//    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState instanceof Bundle) {
            // When restoring, as data is still not loaded, we just save the reference for
            // Loader.onLoadFinished
            Parcelable mLayoutManagerSavedState = ((Bundle) savedInstanceState)
                    .getParcelable(BUNDLE_RECYCLER_LAYOUT);
            if (savedInstanceState.containsKey("scrollpos")) {
                int scrollToPos = ((Bundle) savedInstanceState)
                        .getInt("scrollpos");
                setPosition(scrollToPos);
            }
        }
    }


//    @Override
//    public void onResume() {
//        super.onResume();
//
//        if (mLayoutManagerSavedState != null) {
//            mLayoutManager.onRestoreInstanceState(mLayoutManagerSavedState);
//        }
//    }

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
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
//        if (mPosition != ListView.INVALID_POSITION) {
//            outState.putInt(SELECTED_KEY, mPosition);
//        }
    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        int pos = mLayoutManager.getPosition(mRecyclerView.getChildAt(0));
//        int scrollpos = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
//            .findFirstCompletelyVisibleItemPosition();
//        Log.d(LOG_TAG, "pos="+pos+";scr="+scrollpos);
//     }



}


