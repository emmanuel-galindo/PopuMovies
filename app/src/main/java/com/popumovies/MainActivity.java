package com.popumovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {
    private static final String DETAILFRAGMENT_TAG = "DFTAG";


    private boolean mTwoPane;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity","onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TODO: normalize how to evaluate if in twopane
        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp-land). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
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
            if (savedInstanceState == null || (
                        (
                                savedInstanceState != null &&
                                        getSupportFragmentManager()
                                                .findFragmentByTag(DETAILFRAGMENT_TAG) == null
                        )
                    )
            ){

                Intent intent = getIntent();
    //        Uri movieUri = intent.getData();
            //TODO: Evaluate and get rid of position(pager) when not necesarry
                long position = intent.getIntExtra("pos", 0);
                long movieId = intent.getLongExtra("movieId",0);

                DetailActivityFragment frag = new DetailActivityFragment();
                // if movieId has something in this stage, is because this is the redirect from
                // the detail fragment
                if (movieId > 0) {
                    Bundle args = new Bundle();
                    args.putString(DetailActivityFragment.DETAIL_MOVIEID, Long.toString(movieId));
                    args.putBoolean(DetailActivityFragment.DETAIL_TWOPANE, mTwoPane);
                    frag.setArguments(args);
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, frag,
                                DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
    }



    // This is meant to be executed when a user press the back button on the detail view
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("MainActivity","onRestart");
        MainActivityFragment mf = (MainActivityFragment)getSupportFragmentManager().
                findFragmentById(R.id.fragment_main);
        if (mf != null) {
            // if data changes in detail activity, as favorites, we want it reflected on the
            // saved instance
            mf.restartLoader();
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.d("MainActivity","onResume");
//    }

    //TODO: test onResume (see sunshine, onlocationchanged)

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//    }

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

    // Callback when a item is clicked.
    // If it is a single pane, it will use the pos, so Pager (for swipe between movies) work
    // If it is two pane, it replaces the fragment with a new one w/correct arguments
    @Override
    public void onItemSelected(int pos, long movieId) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            DetailActivityFragment df = new DetailActivityFragment();
            if ( df != null ) {
                Bundle args = new Bundle();
                args.putString(DetailActivityFragment.DETAIL_MOVIEID, Long.toString(movieId));
                args.putBoolean(DetailActivityFragment.DETAIL_TWOPANE, mTwoPane);

                df.setArguments(args);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, df, DETAILFRAGMENT_TAG)
//                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                        .commit();
            }

        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra("pos",pos)
                    .putExtra("movieId", movieId);
            startActivity(intent);
        }
    }

    // First of all, why to do this? because it wont allow a new fragment to be created when the
    // loader finishes in the mainactivityfragment
    // In two panel, it looks for the recentely created fragment and continue the load of data
    // In summary, at onCreate it init an empty fragment, and after the list of movies is
    // queries from db, it picks the first element and call initMoviePane to continue loading
    @Override
    public void initMoviePane(long movieId) {
        if (mTwoPane) {
            DetailActivityFragment df = (DetailActivityFragment) getSupportFragmentManager()
                    .findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( df != null ) {
                df.onTwoPaneMovieSelected(movieId);
            }
        }
    }

}
