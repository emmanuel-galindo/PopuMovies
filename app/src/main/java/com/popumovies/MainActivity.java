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

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    public static final String MAINFRAGMENT_TAG = "MFTAG";
    private static final String VIEWPAGERFRAGMENT_TAG = "VFTAG";

    private static final String LOG_TAG = MainActivity.class.getSimpleName();


    private boolean mTwoPane;
    private long mMovieId = 0;
    private MainActivityFragment mf;
    private int mPosition = ListView.INVALID_POSITION;

// behavior
//1 - init port
//    show list
//2 - init lands
//    show list
//3 - lands rot port
//    show keeping pos list
//4 - lands after selecting any movie rot port
//    show selected movie
//5 - port list rot lands
//    show list keeping position and first item
//6 - port detail rot lands
//    show detail in detail pane, and list w/pos
//7 - click list lands
//      show detail in detail pane.. back button should close the detail pane


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TODO: normalize how to evaluate if in twopane
        mTwoPane = getResources().getBoolean(R.bool.has_two_panes);

//        if (mTwoPane) {

            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp-land). If this view is present, then the activity should be
            // in two-pane mode.
            //if (findViewById(movie_detail_container) != null) {
            //    mTwoPane = true;

            // First of all, why to do this? because it wont allow a new fragment to be created when the
            // loader finishes in the mainactivityfragment
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            // tengo dos casos
            // 1-sis == null: no carga la primer peli al init de twopane after rotate
            //      por que al rotar, sis ya esta, y por lo tanto no genera el fragment vacio para que despues se llene en onloadfinish
            // 2-!sis == null: selectitem en single pane, rotate, select otro peli, rotate, rotate, vuelve la primera
            //      por que selectitem no esta cargando la movie id
//            if (savedInstanceState == null ||
//                    (savedInstanceState != null && mMovieId == null)) {

            // init
            // rotate
            //    con nuevo movieid
            //    con viejo movieid
//            if (savedInstanceState == null) {
//                // init
//                getSupportFragmentManager().beginTransaction()
//                        .replace(movie_detail_container, new DetailActivityFragment(),
//                                DETAILFRAGMENT_TAG)
//                        .commit();
//            }
//            else {
//                mMovieId = savedInstanceState.getLong("MOVIE_ID",0);
//                if (getSupportFragmentManager()
//                        .findFragmentByTag(DETAILFRAGMENT_TAG) == null) {
//                    if (mMovieId > 0) {
//                        // init en portrait/list/item, rotate a landscape = empty shell
//                        DetailActivityFragment df = new DetailActivityFragment();
//                        Bundle args = new Bundle();
//                        args.putString(DetailActivityFragment.DETAIL_MOVIEID, Long.toString(mMovieId));
//                        args.putBoolean(DetailActivityFragment.DETAIL_TWOPANE, mTwoPane);
//
//                        df.setArguments(args);
//
//                        getSupportFragmentManager().beginTransaction()
//                                .replace(movie_detail_container, df,
//                                        DETAILFRAGMENT_TAG)
//                                .commit();
//                    } else {
//                        // init en portrait/list, rotate a landscape = empty shell
//                        getSupportFragmentManager().beginTransaction()
//                                .replace(movie_detail_container, new DetailActivityFragment(),
//                                        DETAILFRAGMENT_TAG)
//                                .commit();
//                    }
//                } else {
//                    if (mMovieId > 0) {
//                        // viene de portrait/list/item rotate
////                        DetailActivityFragment df = (DetailActivityFragment) getSupportFragmentManager()
////                                .findFragmentByTag(DETAILFRAGMENT_TAG);
////                        DetailActivityFragment df = new DetailActivityFragment();
////                        Bundle args = new Bundle();
////                        args.putString(DetailActivityFragment.DETAIL_MOVIEID, Long.toString(mMovieId));
////                        args.putBoolean(DetailActivityFragment.DETAIL_TWOPANE, mTwoPane);
////
////                        df.setArguments(args);
////
////                        getSupportFragmentManager().beginTransaction()
////                                .replace(R.id.movie_detail_container, df,
////                                        DETAILFRAGMENT_TAG)
////                                .commit();
//                    }
//                }
//            }


//            if (savedInstanceState == null || (
//                        (
//                                savedInstanceState != null &&
//                                        getSupportFragmentManager()
//                                                .findFragmentByTag(DETAILFRAGMENT_TAG) == null
//                        )
//                    )
//            ){
//
//                Intent intent = getIntent();
//    //        Uri movieUri = intent.getData();
//            //TODO: Evaluate and get rid of position(pager) when not necesarry
//                long position = intent.getIntExtra("pos", 0);
//                long movieId = intent.getLongExtra("movieId",0);
//
//                DetailActivityFragment frag = new DetailActivityFragment();
//                // if movieId has something in this stage, is because this is the redirect from
//                // the detail fragment
////                if (movieId > 0) {
////                    Bundle args = new Bundle();
////                    args.putString(DetailActivityFragment.DETAIL_MOVIEID, Long.toString(movieId));
////                    args.putBoolean(DetailActivityFragment.DETAIL_TWOPANE, mTwoPane);
////                    frag.setArguments(args);
////                }
//
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.movie_detail_container, frag,
//                                DETAILFRAGMENT_TAG)
//                        .commit();
//            }
//        } else {
//            if (savedInstanceState == null) {
//                mf = new MainActivityFragment();
//                getSupportFragmentManager().beginTransaction()
//                        .add(main_container, mf, MAINFRAGMENT_TAG)
//                        .commit();
////            } else {
////                mf = (MainActivityFragment) getSupportFragmentManager()
////                        .findFragmentById(fragment_main);
////                getSupportFragmentManager().beginTransaction()
////                        .replace(main_container, mf, MAINFRAGMENT_TAG)
////                        .commit();
//
//            }
//
//            getSupportActionBar().setElevation(0f);
////            }
//        }

        mf = (MainActivityFragment) getSupportFragmentManager()
                .findFragmentByTag(MAINFRAGMENT_TAG);
        if (mf == null)
            mf = new MainActivityFragment();
//            mf.setRetainInstance(true);
            // replace or add???
        getSupportFragmentManager().beginTransaction()
                    .replace(main_container, mf, MAINFRAGMENT_TAG)
                    .commit();
//        } else {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(main_container, mf, MAINFRAGMENT_TAG)
//                    .commit();
//        }

        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt("POSITION");
            mMovieId = savedInstanceState.getLong("MOVIE_ID");
            //TODO: duplicated code!!
            // Here we handle the rotation when a movie is selected
            if (mPosition >= 0 && mMovieId > 0) {
                Bundle args = new Bundle();
                if (mTwoPane) {
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

                    Log.d(LOG_TAG,"should call set position");
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

    // This is meant to be executed when a user press the back button on the detail view
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(LOG_TAG,"onRestart");
        // TODO: esto no lo deberia sacar???
        MainActivityFragment mf = (MainActivityFragment)getSupportFragmentManager().
                findFragmentByTag(MAINFRAGMENT_TAG);
        if (mf != null) {
            // if data changes in detail activity, as favorites, we want it reflected on the
            // saved instance
            mf.restartLoader();
        }
    }

//TODO: test onResume (see sunshine, onlocationchanged)
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.d("MainActivity","onResume");
//    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(LOG_TAG,"onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

//    @Override
//    public void loadDetailFragment(Uri contentUri) {
//        if (mTwoPane) {
//            Bundle args = new Bundle();
//            args.putParcelable(DetailActivityFragment.DETAIL_URI, contentUri);
//
//            DetailActivityFragment fragment = new DetailActivityFragment();
//
//            fragment.setArguments(args);
//
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
//                    .commit();
//        }
//    }

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
        mMovieId = movieId;
        mPosition = position;
        Bundle args = new Bundle();

        if (mTwoPane) {
            DetailActivityFragment df = new DetailActivityFragment();
            args.putString(DetailActivityFragment.DETAIL_MOVIEID, Long.toString(movieId));
//            args.putBoolean(DetailActivityFragment.DETAIL_TWOPANE, mTwoPane);
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



        // In two-pane mode, show the detail view in this activity by
        // adding or replacing the detail fragment using a
        // fragment transaction.


//        } else {
//            Intent intent = new Intent(this, DetailActivity.class)
//                    .putExtra("pos",pos)
//                    .putExtra("movieId", movieId);~~
//            startActivity(intent);
//        }
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed()");
        // If in twopane, and it is showing a detail, we want to close the detail. And also
        // to remove the flags that a movie is selected
        if (mTwoPane &&
                findViewById(movie_detail_container).getVisibility() == View.VISIBLE) {
            mMovieId = 0;
//            mPosition = -1;
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
//            mPosition = -1;
//            mf.setPosition(getPosition());


            // Here we remove the selected movie flag.
            // In this case, we need to setMovieId in the DetailFragment through the VPFragment,
            // as after this the onPause of VPFragment is executed by the lifecycle
            // and, for the rotation process, saves the movieId from the DFragment.
            vp.setMovieId(0);

            // Also, we want to keep the position from within the VPFragment navigation, so
            // the list repositions correctly
            mf.setPosition(vp.getCurrentPosition());

//            if (getSupportActionBar() != null)
//                getSupportActionBar().show();


        }

        super.onBackPressed();
    }

    // First of all, why to do this? because it wont allow a new fragment to be created when the
    // loader finishes in the mainactivityfragment
    // In two panel, it looks for the recentely created fragment and continue the load of data
    // In summary, at onCreate it init an empty fragment, and after the list of movies is
    // queries from db, it picks the first element and call initMoviePane to continue loading
//    @Override
//    public void initMoviePane(long movieId) {
//        if (mTwoPane && mMovieId == 0) {
//            DetailActivityFragment df = (DetailActivityFragment) getSupportFragmentManager()
//                    .findFragmentByTag(DETAILFRAGMENT_TAG);
//            if ( df != null ) {
//                df.onTwoPaneMovieSelected(movieId);
//            }
//        }
//    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG,"onSaveInstanceState");
        //TODO: change these hardcoded keys
        outState.putLong("MOVIE_ID", mMovieId);
        outState.putInt("POSITION", mPosition);

    }

    public void setMovieId(long movieId) {
        mMovieId = movieId;
    }
    public void setPosition(int position) {
        mPosition = position;
    }
    public int getPosition() {
        return mPosition;
    }

//    public void refreshMovieList() {
//        mf.restartLoader();
//    }

}
