package com.popumovies;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.popumovies.data.MovieContract.MovieEntry;
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
import java.util.Vector;

class FetchMoviesTask extends AsyncTask<Void,Void,String> {
    private static final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
    private final String mSortBy;
    private final Context mContext;

    private FetchMoviesTask(Context context, String sortBy) {
        super();
        mContext = context;
        mSortBy = sortBy;

    }

    @Override
    protected String doInBackground(Void... params) {


        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;


        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            //http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=
            // get the sort_by preference
            int idSortBy = PrefUtil.getInt(
                    mContext, "sort_by", R.id.action_sort_popularity);
//            String sortBy;
//            if (idSortBy == R.id.action_sort_popularity)
//                sortBy = mContext.getString(R.string.sort_popularity);
//            else
//                sortBy = mContext.getString(R.string.sort_votes);
//            final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
            final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/"+mSortBy+"?";
            final String API_KEY = "api_key";

            String api_key = "ba7a9d0e2fb18d7d47b1b4bfaabc4d04";

            Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY, api_key)
                    .build();

            Log.d(LOG_TAG, "Opening "+builtUri.toString());
            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            moviesJsonStr  = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }


        return moviesJsonStr;
    }

    @Override
    protected void onPostExecute(String moviesJsonStr) {
//        super.onPostExecute(aVoid);
        try {
            getMoviesDataFromJson(moviesJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        // This will only happen if there was an error getting or parsing the forecast.
    }

    private Void getMoviesDataFromJson(String forecastJsonStr)
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
            }

            // add to database
            if ( cVVector.size() > 0 ) {
                // Student: call bulkInsert to add the weatherEntries to the database here
                mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI,
                        cVVector.toArray(new ContentValues[cVVector.size()]));
            }

            Log.d(LOG_TAG, "MovieTask Complete. " + cVVector.size() + " Inserted");


        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

}
