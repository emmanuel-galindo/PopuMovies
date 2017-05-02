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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.popumovies.adapter.ReviewAdapter;
import com.popumovies.adapter.VideoAdapter;
import com.popumovies.data.MovieContract;
import com.popumovies.data.MovieContract.MovieEntry;
import com.popumovies.utils.HelperOkHttpClient;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final int FRAGMENT_DETAIL_MOVIE_LOADER = 1;
    private static final int FRAGMENT_DETAIL_VIDEO_LOADER = 2;
    private static final int FRAGMENT_DETAIL_REVIEW_LOADER = 3;
    public static final String DETAIL_MOVIEID = "_id";


    private View mRootView;
    private Uri mUri;
    private VideoAdapter mVideoAdapter;
    private ReviewAdapter mReviewAdapter;

    private long mMovieId;
    private TextView mTitleView;
    private TextView mDescriptionView;
    private RatingBar mVoteAverageRatingBar;
    private TextView mVoteAverageView;
    private TextView mYearView;
    private ImageView mPosterView;
    private FloatingActionButton mFavoriteButton;
    private ImageView mBackgroundView;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "OnCreateView");
        mRootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mTitleView = (TextView) mRootView.findViewById(R.id.textview_movie_title);
        mPosterView = (ImageView) mRootView.findViewById(R.id.imageview_movie_poster);
        mBackgroundView = (ImageView) mRootView.findViewById(R.id.imageview_movie_background);
        mYearView = (TextView) mRootView.findViewById(R.id.textview_movie_year);
        mVoteAverageView = (TextView) mRootView.findViewById(R.id.textview_movie_vote_average);
        mVoteAverageRatingBar = (RatingBar) mRootView.findViewById(R.id.ratingbar_movie_vote_average);
        mDescriptionView = (TextView) mRootView.findViewById(R.id.textview_movie_description);
        mFavoriteButton = (FloatingActionButton) mRootView.findViewById(R.id.button_fav);

        RecyclerView mRecyclerViewVideo = (RecyclerView) mRootView.findViewById(R.id.recyclerview_video);
        LinearLayoutManager mVideoLayoutManager = new LinearLayoutManager(getActivity(), 0, false);
        mRecyclerViewVideo.setLayoutManager(mVideoLayoutManager);
        mRecyclerViewVideo.getLayoutManager().setAutoMeasureEnabled(true);
        mRecyclerViewVideo.setHasFixedSize(false);
        mVideoAdapter = new VideoAdapter(getActivity(), null);
        mRecyclerViewVideo.setAdapter(mVideoAdapter);

        RecyclerView mRecyclerViewReview = (RecyclerView) mRootView.findViewById(R.id.recyclerview_review);
        mRecyclerViewReview.setHasFixedSize(true);
        LinearLayoutManager mReviewLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerViewReview.setLayoutManager(mReviewLayoutManager);
        mReviewAdapter = new ReviewAdapter(getActivity(), null);
        mRecyclerViewReview.setAdapter(mReviewAdapter);

        // Detect if details are present in the arguments (ViewPager) or if they need to be
        // read from the database again (landscape, two pane)
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovieId = Long.parseLong(arguments.getString(DETAIL_MOVIEID));
            mUri = MovieEntry.buildSingleMovieUri(mMovieId);

            if (arguments.size() > 1) {
                fillForm(arguments);
            } else {
                getLoaderManager().initLoader(FRAGMENT_DETAIL_MOVIE_LOADER, null, this);
            }
        }

        return mRootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(MainActivity.MOVIE_SAVE_KEY, mMovieId);
        //outState.putInt("POSITION", mPosition);

    }

    @Override
    public void onPause() {
        Log.d(LOG_TAG,"onPause");
        //getActivity().setMovieId(mMovieId);
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
        }
        return true;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        if (id == FRAGMENT_DETAIL_VIDEO_LOADER) {
            Uri videoUri = MovieEntry.buildMovieWithVideos(mMovieId);
            if (videoUri != null) {
                return new CursorLoader(
                        getActivity(),
                        videoUri,
                        null,
                        null,
                        null,
                        null
                );
            }
        }
        else if (id == FRAGMENT_DETAIL_REVIEW_LOADER) {
            Uri reviewUri = MovieEntry.buildMovieWithReviews(mMovieId);
            if (reviewUri != null) {
                return new CursorLoader(
                        getActivity(),
                        reviewUri,
                        null,
                        null,
                        null,
                        null
                );
            }
        }
        else if (id == FRAGMENT_DETAIL_MOVIE_LOADER) {
            mUri = MovieEntry.buildSingleMovieUri(mMovieId);
            if (mUri != null) {
                return new CursorLoader(
                        getActivity(),
                        mUri,
                        null,
                        null,
                        null,
                        null
                );
            }
        }
        return null;    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) { return; }

        // here we evaluate if more than 3 to show expand, and more of less
        if (loader.getId() == FRAGMENT_DETAIL_VIDEO_LOADER) {
            mVideoAdapter.swapCursor(data);
        }
        else if (loader.getId() == FRAGMENT_DETAIL_REVIEW_LOADER)
            mReviewAdapter.swapCursor(data);
        else if (loader.getId() == FRAGMENT_DETAIL_MOVIE_LOADER) {
            // Instead of using an adapter, we fill a Bundle (args) with cursor's data
            updateMovieDetails(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
    }

    // This is called both from single and two panel to populate the data with a common struct
    // bundle
    private void fillForm(Bundle arguments) {
        String originalTitle = arguments.getString(MovieEntry.COLUMN_ORIGINAL_TITLE);
        Log.d(LOG_TAG, "fillForm/originalTitle => " + originalTitle);
        Date release_date = new Date(Long.parseLong(
                arguments.getString(MovieEntry.COLUMN_RELEASE_DATE)));
        String posterPath = arguments.getString(MovieEntry.COLUMN_POSTER_PATH);
        String backgroundPath = arguments.getString(MovieEntry.COLUMN_BACKGROUND_PATH);
        String voteAverage = arguments.getString(MovieEntry.COLUMN_VOTE_AVERAGE);
        String overView = arguments.getString(MovieEntry.COLUMN_OVERVIEW);
        boolean favorite;
        favorite = (arguments.getString(MovieEntry.COLUMN_FAVORITE).equals("1"));

        // Showing the original title as Movie title
        mTitleView.setText(originalTitle);

        // extract the year
        SimpleDateFormat df = new SimpleDateFormat("yyyy");
        mYearView.setText(df.format(release_date));

        mVoteAverageView.setText(String.format(getString(R.string.vote_avg_string),voteAverage));

        float voteAverageNumber = Float.parseFloat(voteAverage) / 2;
        mVoteAverageRatingBar.setRating(voteAverageNumber);

        mDescriptionView.setText(overView);


        // Poster image is composed of 3 parts
        // 1 - The base URL will look like: http://image.tmdb.org/t/p/.
        // 2 - Then you will need a ‘size’, which will be one of the following:
        //  "w92", "w154", "w185", "w342", "w500", "w780", or "original". For most phones we recommend using “w185”.
        // 3 - And finally the poster path returned by the query, in this case “/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg”
        Context context = getContext();
        String poster = context.getString(R.string.TMDB_POSTER_URL)
                + context.getString(R.string.tmdb_poster_resolution)
                + "/" + posterPath;
        String background = context.getString(R.string.TMDB_POSTER_URL)
                + context.getString(R.string.tmdb_background_resolution)
                + "/" + backgroundPath;
        Log.d(LOG_TAG, "Loading poster url => " + poster);
        Picasso picasso = new HelperOkHttpClient().getPicassoInstance(context);
        picasso.load(poster)
                .into(mPosterView);
        picasso.load(background)
                .into(mBackgroundView);

        // Accessibility feature
        mPosterView.setContentDescription(arguments.getString(MovieEntry.COLUMN_ORIGINAL_TITLE));
        mBackgroundView.setContentDescription(arguments.getString(MovieEntry.COLUMN_ORIGINAL_TITLE));



        mFavoriteButton.setSelected(favorite);
        if (favorite)
            mFavoriteButton.setImageResource(R.drawable.ic_favorite_black_36dp);
        else
            mFavoriteButton.setImageResource(R.drawable.ic_favorite_border_black_36dp);

        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                //mUri = content://com.popumovies/movie/4
                onClickFavorite(v, mUri);
                if (v.isSelected()) {
                    mFavoriteButton.setImageResource(R.drawable.ic_favorite_black_36dp);
                } else {
                    mFavoriteButton.setImageResource(R.drawable.ic_favorite_border_black_36dp);
                }
            }
        });

        getLoaderManager().initLoader(FRAGMENT_DETAIL_VIDEO_LOADER, null, this);
        getLoaderManager().initLoader(FRAGMENT_DETAIL_REVIEW_LOADER, null, this);
    }



    // When in twopane, this is called after the cursor loader finishes, to
    // map the cursor to the arguments array
    private void updateMovieDetails(Cursor cursor) {
        if (cursor.moveToFirst()) {
            Bundle args = new Bundle();
            for (int i = 0; i < cursor.getColumnCount(); ++i) {
                args.putString(cursor.getColumnName(i), cursor.getString(i));
            }
            fillForm(args);
        }
    }

    private void onClickFavorite(View v, Uri mUri) {
        v.setSelected(!v.isSelected());
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, v.isSelected());
        Log.d(LOG_TAG,"calling update with args: uri=>" + mUri +",mv=>"+movieValues.toString());
        getActivity().getContentResolver().update(
                mUri,
                movieValues,
                null,
                null);
        // List should be refreshed
        // I would expect the observer from the ContentProvider to detect the chage, but
        // it seems to be missing something.
        // Some comments point to adapter.notifyDataSetChanged();
        MainActivityFragment mf = (MainActivityFragment) getActivity()
                .getSupportFragmentManager().findFragmentByTag(MainActivity.MAINFRAGMENT_TAG);
        mf.restartLoader();
    }



    public void setMovieId(long movieId) {
        mMovieId = movieId;
    }

    public long getMovieId() {
        return mMovieId;
    }


}