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

package com.popumovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.popumovies.R;
import com.popumovies.data.MovieContract.MovieEntry;
import com.popumovies.data.MovieContract.ReviewEntry;
import com.popumovies.data.MovieContract.VideoEntry;
import com.popumovies.endpoint.ApiManager;
import com.popumovies.model.Movie;
import com.popumovies.model.MoviesResults;
import com.popumovies.model.Reviews;
import com.popumovies.model.Videos;
import com.popumovies.utils.PrefUtil;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    private final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the movie, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    private static final int SYNC_INTERVAL = 60 * 180;
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int MOVIE_NOTIFICATION_ID = 3004;

    // Retrofit manager
    private ApiManager mgr;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Context context = getContext();

        mgr = new ApiManager(context);

        MoviesResults page = new MoviesResults();
        int idFilter = PrefUtil.getInt(context, context.getString(R.string.pref_filter_label));
        /*
            There are two filter options, popular and high rated.
            TMBD has a limit of requests of 40 requests every 10 secs.
            We do 42 requests (=)) therefore, lets first bring and show the
            results that the user has selected, and then wait 10secs for the other selection
         */
        try {
            page =getMoviesFromAPI(mgr, idFilter);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        addMoviesToDB(page.getResults(), true);

        idFilter = (idFilter==R.id.action_sort_popularity) ?
                R.id.action_sort_votes:
                R.id.action_sort_popularity;

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            page = getMoviesFromAPI(mgr, idFilter);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        addMoviesToDB(page.getResults(), false);
        getContext().getContentResolver().notifyChange(MovieEntry.CONTENT_URI, null);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.d("MovieSyncAdapter", "syncImmediately");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    private MoviesResults getMoviesFromAPI(ApiManager mgr, int idFilter) throws IOException {
        MoviesResults page = new MoviesResults();
        if (idFilter == R.id.action_sort_popularity) {
            page = mgr.movies().popularList().execute().body();
        }
        else if (idFilter == R.id.action_sort_votes) {
            page = mgr.movies().topRatedList().execute().body();
        }
        return page;
    }

    /*
        It adds the results from movie db webservice to the database.
        - mgr variable is the retrofit controller, it should be setup from the caller
        Arguments:
        - results is a list of the objects to insert
        - initial indicates when to remove old records

     */
    private boolean addMoviesToDB(List<Movie> results, boolean initial) {

        Vector<ContentValues> vectorMovieContentValues = new Vector<>(results.size());
        Vector<ContentValues> vectorVideoContentValues = new Vector<>(results.size());
        Vector<ContentValues> vectorReviewContentValues = new Vector<>(results.size());
        for(int i = 0; i < results.size(); i++) {
            Movie movie = results.get(i);
            //Log.d(LOG_TAG,"Processing movie: " + movie.getTitle());
            Movie movieWithExtras;
            try {
                movieWithExtras = mgr.movies().movie(movie.getId()).execute().body();
            } catch (Exception e ){
                // handle error
                Log.e(LOG_TAG,"Error getting Movies: " + e.toString());
                return false;
            }


            ContentValues movieValues = new ContentValues();

            movieValues.put(MovieEntry.COLUMN_TMDB_ID, movie.getId());
            movieValues.put(MovieEntry.COLUMN_TITLE, movie.getTitle());
            movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
            movieValues.put(MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
            movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
            movieValues.put(MovieEntry.COLUMN_BACKGROUND_PATH, movie.getBackdropPath());
            movieValues.put(MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
            movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
            movieValues.put(MovieEntry.COLUMN_VOTE_COUNT, movie.getVoteCount());
            movieValues.put(MovieEntry.COLUMN_POPULARITY, movie.getPopularity());

            vectorMovieContentValues.add(movieValues);

            for (int j=0; j < movieWithExtras.videos.results.size(); j++) {
                Videos.Video video = movieWithExtras.videos.results.get(j);

                ContentValues videoValues = new ContentValues();
                videoValues.put(VideoEntry.COLUMN_TMDB_ID, movie.getId());
                videoValues.put(VideoEntry.COLUMN_ISO6391, video.getIso6391());
                videoValues.put(VideoEntry.COLUMN_ISO31661, video.getIso31661());
                videoValues.put(VideoEntry.COLUMN_KEY, video.getKey());
                videoValues.put(VideoEntry.COLUMN_NAME, video.getName());
                videoValues.put(VideoEntry.COLUMN_SITE, video.getSite());
                videoValues.put(VideoEntry.COLUMN_SIZE, video.getSize());
                videoValues.put(VideoEntry.COLUMN_TYPE, video.getType());

                vectorVideoContentValues.add(videoValues);
            }

            for (int p=0; p < movieWithExtras.reviews.results.size(); p++) {
                Reviews.Review review = movieWithExtras.reviews.results.get(p);

                ContentValues reviewValues = new ContentValues();
                reviewValues.put(ReviewEntry.COLUMN_TMDB_ID, movie.getId());
                reviewValues.put(ReviewEntry.COLUMN_REVIEW_ID, review.getId());
                reviewValues.put(ReviewEntry.COLUMN_AUTHOR, review.getAuthor());
                reviewValues.put(ReviewEntry.COLUMN_CONTENT, review.getContent());
                reviewValues.put(ReviewEntry.COLUMN_URL, review.getUrl());

                vectorReviewContentValues.add(reviewValues);
            }

        }
        // add to database
        if ( vectorMovieContentValues.size() > 0 ) {
            // delete all rows from the Review table, except the reviews from movies
            // marked as favorite
            if (initial) {
                getContext().getContentResolver().delete(
                        ReviewEntry.CONTENT_URI,
                        ReviewEntry.TABLE_NAME + "." + ReviewEntry.COLUMN_TMDB_ID + " IN  ( SELECT " +
                                MovieEntry.COLUMN_TMDB_ID + " FROM " + MovieEntry.TABLE_NAME + " WHERE " +
                                MovieEntry.COLUMN_FAVORITE + " = 0 )",
                        null);


                // delete all rows from the Video table, except the videos from movies
                // marked as favorite
                getContext().getContentResolver().delete(
                        VideoEntry.CONTENT_URI,
                        VideoEntry.TABLE_NAME + "." + VideoEntry.COLUMN_TMDB_ID + " IN  ( SELECT " +
                                MovieEntry.COLUMN_TMDB_ID + " FROM " + MovieEntry.TABLE_NAME + " WHERE " +
                                MovieEntry.COLUMN_FAVORITE + " = 0 )",
                        null);

                // wipe everything from the Movie table, except the ones marked as favorite
                getContext().getContentResolver().delete(
                        MovieEntry.CONTENT_URI,
                        MovieEntry.TABLE_NAME + "." +
                                MovieEntry.COLUMN_FAVORITE + " = 0",
                        null);
            }
            getContext().getContentResolver().bulkInsert(MovieEntry.CONTENT_URI,
                    vectorMovieContentValues.toArray(new ContentValues[vectorMovieContentValues.size()]));

            if ( vectorVideoContentValues.size() > 0 ) {
                getContext().getContentResolver().bulkInsert(VideoEntry.CONTENT_URI,
                        vectorVideoContentValues.toArray(new ContentValues[vectorVideoContentValues.size()]));
            }

//            if ( vectorReviewContentValues.size() > 0 ) {
//                getContext().getContentResolver().bulkInsert(ReviewEntry.CONTENT_URI,
//                        vectorReviewContentValues.toArray(new ContentValues[vectorReviewContentValues.size()]));
//            }

        }
        Log.d(LOG_TAG, "MovieTask Complete. " + vectorMovieContentValues.size() + " Inserted");
        return true;
    }


    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    private static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    private static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            //Sync disabled!! uncomment to schedule the sync
//            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}