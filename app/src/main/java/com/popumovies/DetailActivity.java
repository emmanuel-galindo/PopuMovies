package com.popumovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.popumovies.adapter.CursorPagerAdapter;
import com.popumovies.data.MovieContract;
import com.popumovies.utils.PrefUtil;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

/**
 * Detail Activity
 * The activity comes from an onClick event on the MovieAdapter, with the position on the list
 * As we use a ViewPager, with its CursorPagerAdapter implementation, the same list of movies
 * from MainActivity list is obtained, and then the position used by argument is used to
 * locate the correct page within the cursor.
 * To the fragment, a list of all the fields from the cursor query is sent so no additional
 * query is necessary to show the Movie information.
 */
public class DetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final int ACTIVITY_DETAIL_LOADER = 1;
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private CursorPagerAdapter mPagerAdapter;
    private int mPosition;
//    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG,"onCreate");
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
//        Uri movieUri = intent.getData();
        mPosition = intent.getIntExtra("pos", 0);
        long movieId = intent.getLongExtra("movieId",0);

        //if on tablet, it rotates, catch it here and send it to mainactivity
        //TODO: evaluate if it is better to redirect this when saving the state or in any other
        // step from the lifecycle
        //TODO: when the app is open in landscape, and then rotated, a new movie is selected, and then rotated again: should it create a new instance? it is going to mainactivity with savedinstance null
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (tabletSize && getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE ) {
            // un par de tiros
            getSupportFragmentManager().beginTransaction().commitAllowingStateLoss();
            getSupportFragmentManager().executePendingTransactions();
            
            finish();
//            Intent listIntent = new Intent(this, MainActivity.class)
//                    .putExtra("pos",mPosition)
//                    .putExtra("movieId", movieId);
//            startActivity(listIntent);
            return;
        }


        setContentView(R.layout.activity_detail);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);

        mPagerAdapter = new CursorPagerAdapter<>(getSupportFragmentManager(),
                DetailActivityFragment.class, new String[]{}, null);
        mPager.setAdapter(mPagerAdapter);
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        LoaderManager.LoaderCallbacks<Cursor> callbacks = this;
        getSupportLoaderManager().initLoader(ACTIVITY_DETAIL_LOADER, null, callbacks);
    }

//    @Override
//    public void onBackPressed() {
//        if (mPager.getCurrentItem() == 0) {
//            // If the user is currently looking at the first step, allow the system to handle the
//            // Back button. This calls finish() on this activity and pops the back stack.
//            super.onBackPressed();
//        } else {
//            // Otherwise, select the previous step.
//            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
//        }
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri movieUri;
        int idFilter = PrefUtil.getInt(this, getString(R.string.pref_filter_label));
//        if (idFilter == R.id.action_sort_popularity)
//            movieUri = MovieContract.MovieEntry.buildPopularMovieUri();
//        else if (idFilter == R.id.action_favorites)
        if (idFilter == R.id.action_favorites)
            movieUri = MovieContract.MovieEntry.buildFavoriteMovieUri();
        else if (idFilter == R.id.action_sort_votes)
            movieUri = MovieContract.MovieEntry.buildHighRatedMovieUri();
        else
            movieUri = MovieContract.MovieEntry.buildPopularMovieUri();

        return new CursorLoader(this, movieUri,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPagerAdapter.swapCursor(data, mPager);
        mPager.setCurrentItem(mPosition, false);

        // Show a little help about the swipe on the first time
        boolean showSwipeHelp = PrefUtil.getBoolean(this, getString(R.string.pref_swipehelp_label));
        if (!showSwipeHelp) {
            PrefUtil.setBoolean(this, getString(R.string.pref_swipehelp_label), true);
            Intent intent = new Intent(this, SwipeHelpActivity.class);
            startActivity(intent);
        }
    }

    /*
    Quick fix to get to the previous (pixel precision) scroll position when the UP button
    is clicked
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

}
