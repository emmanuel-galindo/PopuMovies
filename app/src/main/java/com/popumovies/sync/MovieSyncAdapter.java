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
import android.net.Uri;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
        Log.d(LOG_TAG, "Starting sync");
        Context context = getContext();

        mgr = new ApiManager();

        List<Movie> results;
        MoviesResults page = new MoviesResults();
//            Response<List<MoviesResults>> page = call.execute();
        //TODO: Implement a check on X-Rate-Limit (https://www.themoviedb.org/faq/api)
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



    }

    private MoviesResults getMoviesFromAPI(ApiManager mgr, int idFilter) throws IOException {
        MoviesResults page = new MoviesResults();
        if (idFilter == R.id.action_sort_popularity) {
            page = mgr.movies().popularList().execute().body();
            idFilter = R.id.action_sort_votes;
        }
        else if (idFilter == R.id.action_sort_votes) {
            page = mgr.movies().topRatedList().execute().body();
            idFilter = R.id.action_sort_popularity;
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
            Log.d(LOG_TAG,"Processing movie: " + movie.getTitle());
            Movie movieWithExtras;
            try {
//            Response<List<MoviesResults>> page = call.execute();
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

            if ( vectorReviewContentValues.size() > 0 ) {
                getContext().getContentResolver().bulkInsert(ReviewEntry.CONTENT_URI,
                        vectorReviewContentValues.toArray(new ContentValues[vectorReviewContentValues.size()]));
            }

        }
        //TODO: Add a spin when refreshing and adding records to DB
        Log.d(LOG_TAG, "MovieTask Complete. " + vectorMovieContentValues.size() + " Inserted");
        return true;
    }

    public String getMoviesJSON(String sortBy) throws IOException {
        //            final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";

        //http://api.themoviedb.org/3/movie/102899?api_key=ba7a9d0e2fb18d7d47b1b4bfaabc4d04&append_to_response=videos,reviews
        final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/"+sortBy+"?";
        final String API_KEY = "api_key";

        //TODO: put this on a resource
        String api_key = "ba7a9d0e2fb18d7d47b1b4bfaabc4d04";

        Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendQueryParameter(API_KEY, api_key)
                .build();

        Log.d(LOG_TAG, "Opening "+builtUri.toString());
        URL url = new URL(builtUri.toString());

        // Create the request to OpenWeatherMap, and open the connection
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();

        // Read the input stream into a String
        InputStream inputStream = urlConnection.getInputStream();
        StringBuilder buffer = new StringBuilder();
        if (inputStream == null) {
            // Nothing to do.
            return "" ;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
            // But it does make debugging a *lot* easier if you print out the completed
            // buffer for debugging.
            buffer.append(line + "\n");
        }

        if (buffer.length() == 0) {
            // Stream was empty.  No point in parsing.
            return "";
        }
        return buffer.toString();
    }

    public Void getMoviesDataFromJson(String forecastJsonStr)
            throws JSONException {

        final String TMDB_LIST = "results";
        final String TMDB_ID = "id";
        final String TMDB_TITLE = "original_title";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_VOTE_COUNT = "vote_count";
        final String TMDB_POPULARITY = "popularity";




        try {
            JSONObject moviesJson = new JSONObject(forecastJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_LIST);

            // Insert the new moviesinformation into the database
            Vector<ContentValues> cVVector = new Vector<>(moviesArray.length());


            for(int i = 0; i < moviesArray.length(); i++) {
                // These are the values that will be collected.
                int id;
                String title;
                String originalTitle;
                String overview;
                String releaseDate;
                String posterPath;
                double voteAverage;
                int voteCount;
                double popularity;



                // Get the JSON object representing the day
                JSONObject movieObj = moviesArray.getJSONObject(i);

                id = movieObj.getInt(TMDB_ID);
                title = movieObj.getString(TMDB_TITLE);
                originalTitle = movieObj.getString(TMDB_ORIGINAL_TITLE);
                overview = movieObj.getString(TMDB_OVERVIEW);
                releaseDate = movieObj.getString(TMDB_RELEASE_DATE);
                posterPath= movieObj.getString(TMDB_POSTER_PATH);
                voteAverage = movieObj.getDouble(TMDB_VOTE_AVERAGE);
                voteCount = movieObj.getInt(TMDB_VOTE_COUNT);
                popularity = movieObj.getDouble(TMDB_POPULARITY);

                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieEntry.COLUMN_TMDB_ID, id);
                movieValues.put(MovieEntry.COLUMN_TITLE, title);
                movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, originalTitle);
                movieValues.put(MovieEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(MovieEntry.COLUMN_POSTER_PATH, posterPath);
                movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
                movieValues.put(MovieEntry.COLUMN_VOTE_COUNT, voteCount);
                movieValues.put(MovieEntry.COLUMN_POPULARITY, popularity);

                cVVector.add(movieValues);

                // TODO: Prefetch the posters
            }

            // add to database
            if ( cVVector.size() > 0 ) {
                // Student: call bulkInsert to add the weatherEntries to the database here
                getContext().getContentResolver().bulkInsert(MovieEntry.CONTENT_URI,
                        cVVector.toArray(new ContentValues[cVVector.size()]));
            }

            Log.d(LOG_TAG, "MovieTask Complete. " + cVVector.size() + " Inserted");


        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }
    
//    private void notifyMovie() {
//        Context context = getContext();
//        //checking the last update and notify if it' the first of the day
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
//        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
//                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));
//
//        if ( displayNotifications ) {
//
//            String lastNotificationKey = context.getString(R.string.pref_last_notification);
//            long lastSync = prefs.getLong(lastNotificationKey, 0);
//
//            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
//                // Last sync was more than 1 day ago, let's send a notification with the movie.
//                String locationQuery = Utility.getPreferredLocation(context);
//
//                Uri movieUri = MovieEntry.buildMovieLocationWithDate(locationQuery, System.currentTimeMillis());
//
//                // we'll query our contentProvider, as always
//                Cursor cursor = context.getContentResolver().query(movieUri, NOTIFY_MOVIE_PROJECTION, null, null, null);
//
//                if (cursor.moveToFirst()) {
//                    int movieId = cursor.getInt(INDEX_MOVIE_ID);
//                    double high = cursor.getDouble(INDEX_MAX_TEMP);
//                    double low = cursor.getDouble(INDEX_MIN_TEMP);
//                    String desc = cursor.getString(INDEX_SHORT_DESC);
//
//                    int iconId = Utility.getIconResourceForMovieCondition(movieId);
//                    Resources resources = context.getResources();
//                    Bitmap largeIcon = BitmapFactory.decodeResource(resources,
//                            Utility.getArtResourceForMovieCondition(movieId));
//                    String title = context.getString(R.string.app_name);
//
//                    // Define the text of the forecast.
//                    String contentText = String.format(context.getString(R.string.format_notification),
//                            desc,
//                            Utility.formatTemperature(context, high),
//                            Utility.formatTemperature(context, low));
//
//                    // NotificationCompatBuilder is a very convenient way to build backward-compatible
//                    // notifications.  Just throw in some data.
//                    NotificationCompat.Builder mBuilder =
//                            new NotificationCompat.Builder(getContext())
//                                    .setColor(resources.getColor(R.color.sunshine_light_blue))
//                                    .setSmallIcon(iconId)
//                                    .setLargeIcon(largeIcon)
//                                    .setContentTitle(title)
//                                    .setContentText(contentText);
//
//                    // Make something interesting happen when the user clicks on the notification.
//                    // In this case, opening the app is sufficient.
//                    Intent resultIntent = new Intent(context, MainActivity.class);
//
//                    // The stack builder object will contain an artificial back stack for the
//                    // started Activity.
//                    // This ensures that navigating backward from the Activity leads out of
//                    // your application to the Home screen.
//                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//                    stackBuilder.addNextIntent(resultIntent);
//                    PendingIntent resultPendingIntent =
//                            stackBuilder.getPendingIntent(
//                                    0,
//                                    PendingIntent.FLAG_UPDATE_CURRENT
//                            );
//                    mBuilder.setContentIntent(resultPendingIntent);
//
//                    NotificationManager mNotificationManager =
//                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
//                    // MOVIE_NOTIFICATION_ID allows you to update the notification later on.
//                    mNotificationManager.notify(MOVIE_NOTIFICATION_ID, mBuilder.build());
//
//                    //refreshing last sync
//                    SharedPreferences.Editor editor = prefs.edit();
//                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
//                    editor.commit();
//                }
//                cursor.close();
//            }
//        }
//    }

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

            onAccountCreated(newAccount, context);
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