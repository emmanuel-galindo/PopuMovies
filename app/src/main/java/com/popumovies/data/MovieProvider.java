/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.popumovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.popumovies.data.MovieContract.MovieEntry;
import com.popumovies.data.MovieContract.VideoEntry;
import com.popumovies.data.MovieContract.ReviewEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MovieProvider extends ContentProvider {
    private static String LOG_TAG = ContentProvider.class.getSimpleName();

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    static final int MOVIE = 100;
    static final int MOVIE_WITH_ID = 101;
    static final int MOVIE_POPULAR = 102;
    static final int MOVIE_HIGH_RATED = 103;
    static final int MOVIE_FAVORITE = 104;
    static final int MOVIE_WITH_VIDEO = 105;
    static final int MOVIE_WITH_REVIEW = 106;
    static final int VIDEO = 107;
    static final int REVIEW = 108;

    private static final SQLiteQueryBuilder sMovieWithVideoQueryBuilder;
    private static final SQLiteQueryBuilder sMovieWithReviewQueryBuilder;



    static{
        sMovieWithVideoQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //movie INNER JOIN video ON movie.tmdb_id = video.tmdb_id
        sMovieWithVideoQueryBuilder.setTables(MovieEntry.TABLE_NAME);
        sMovieWithVideoQueryBuilder.setTables(
                MovieEntry.TABLE_NAME + " INNER JOIN " +
                        VideoEntry.TABLE_NAME +
                        " ON " + MovieEntry.TABLE_NAME +
                        "." + MovieEntry.COLUMN_TMDB_ID +
                        " = " + VideoEntry.TABLE_NAME +
                        "." + VideoEntry.COLUMN_TMDB_ID);
    }

    static{
        sMovieWithReviewQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //movie INNER JOIN review ON movie.tmdb_id = review.tmdb_id
        sMovieWithReviewQueryBuilder.setTables(MovieEntry.TABLE_NAME);
        sMovieWithReviewQueryBuilder.setTables(
                MovieEntry.TABLE_NAME + " INNER JOIN " +
                        ReviewEntry.TABLE_NAME +
                        " ON " + MovieEntry.TABLE_NAME +
                        "." + MovieEntry.COLUMN_TMDB_ID +
                        " = " + ReviewEntry.TABLE_NAME +
                        "." + ReviewEntry.COLUMN_TMDB_ID);
    }



    private static final SQLiteQueryBuilder sMovieQueryBuilder;
    static{
        sMovieQueryBuilder = new SQLiteQueryBuilder();
        sMovieQueryBuilder.setTables(MovieEntry.TABLE_NAME);
    }

    private static final String sMovieIdSelection =
            MovieEntry.TABLE_NAME +
                    "." + MovieEntry.COLUMN_ID + " = ?";


    private static final String sMovieTmdbIdSelection =
            MovieEntry.TABLE_NAME +
                    "." + MovieEntry.COLUMN_TMDB_ID + " = ?";

    private static final String sMovieFavoriteSelection =
            MovieEntry.TABLE_NAME +
                    "." + MovieEntry.COLUMN_FAVORITE + " = ?";


    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;
        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/popular", MOVIE_POPULAR);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/high_rated", MOVIE_HIGH_RATED);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/favorite", MOVIE_FAVORITE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/video", MOVIE_WITH_VIDEO);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/review", MOVIE_WITH_REVIEW);
        matcher.addURI(authority, MovieContract.PATH_VIDEO, VIDEO);
        matcher.addURI(authority, MovieContract.PATH_REVIEW, REVIEW);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.

     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case MOVIE:
            case MOVIE_POPULAR:
            case MOVIE_HIGH_RATED:
            case MOVIE_FAVORITE:
            case MOVIE_WITH_VIDEO:
                return MovieEntry.CONTENT_TYPE;
            case MOVIE_WITH_ID:
                return MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "movie"
            case MOVIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MOVIE_WITH_ID: {
                long id = Long.parseLong(uri.getPathSegments().get(1));

                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        sMovieIdSelection,
                        new String[]{Long.toString(id)},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MOVIE_POPULAR: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        MovieEntry.COLUMN_POPULARITY + " DESC",
                        "20"
                );
                break;
            }
            case MOVIE_HIGH_RATED: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        MovieEntry.COLUMN_VOTE_AVERAGE + " DESC",
                        "20"
                );
                break;
            }
            case MOVIE_FAVORITE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        sMovieFavoriteSelection,
                        new String[]{"1"},
                        null,
                        null,
                        null
                );
                break;
            }
            case MOVIE_WITH_VIDEO: {
                retCursor = getMovieWithVideo(uri, VideoEntry.VIDEO_COLUMNS, sortOrder);
                break;
            }
            case MOVIE_WITH_REVIEW: {
                retCursor = getMovieWithReview(uri, ReviewEntry.REVIEW_COLUMNS, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private Cursor getMovieWithVideo(Uri uri, String[] projection, String sortOrder) {
        long id = Long.parseLong(uri.getPathSegments().get(1));

        return sMovieWithVideoQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMovieIdSelection,
                new String[]{Long.toString(id)},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMovieWithReview(Uri uri, String[] projection, String sortOrder) {
        long id = Long.parseLong(uri.getPathSegments().get(1));

        return sMovieWithReviewQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMovieIdSelection,
                new String[]{Long.toString(id)},
                null,
                null,
                sortOrder
        );
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                normalizeDate(values);
                long _id = db.insert(MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieEntry.buildSingleMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case MOVIE:
                rowsDeleted = db.delete(
                        MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case VIDEO:
                rowsDeleted = db.delete(
                        VideoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEW:
                rowsDeleted = db.delete(
                        ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(MovieEntry.COLUMN_RELEASE_DATE)) {
            Date date;
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                date = format.parse((String) values.get(
                        MovieEntry.COLUMN_RELEASE_DATE));
            }
            catch (ParseException ex) {
                // why it has to fail that hard?
                date = new Date();
                Log.d(LOG_TAG, ex.getMessage());
            }
            long dateValue = date.getTime();
            values.put(MovieEntry.COLUMN_RELEASE_DATE, dateValue);


        }
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE:
                normalizeDate(values);
                rowsUpdated = db.update(MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case MOVIE_WITH_ID: {
                long id = Long.parseLong(uri.getPathSegments().get(1));
                rowsUpdated = db.update(MovieEntry.TABLE_NAME, values,
                        sMovieIdSelection, new String[]{Long.toString(id)});
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case MOVIE:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
//                        long _id = db.insert(MovieEntry.TABLE_NAME, null, value);
//                        if (_id != -1) {
//                            returnCount++;
//                        }
                        /* Custom insert query to allow the use of coalesce to
                        replace on conflict w/out removing the favorites flag, and updating the
                        favorites data to current
                         */
                        String INSERT_MOVIE = "INSERT INTO movie(popularity,vote_average,title," +
                                "original_title,tmdb_id,release_date,poster_path,vote_count,"+
                                "overview,favorite) VALUES (?,?,?,?,?,?,?,?,?," +
                                "coalesce((select favorite from movie where tmdb_id = ?),0))";
                        Object[] bindArgs = null;
                        int size = (value != null && value.size() > 0)
                                ? value.size() : 0;
                        if (size > 0) {
                            bindArgs = new Object[size+1];
                            int i = 0;
                            for (String colName : value.keySet()) {
                                bindArgs[i++] = value.get(colName);
                            }
                            bindArgs[i] = value.get(MovieEntry.COLUMN_TMDB_ID);
                        }
                        db.execSQL(INSERT_MOVIE,bindArgs);
                        long _id = DatabaseUtils.longForQuery(
                                db, "SELECT CASE changes() WHEN 0 THEN -1 ELSE last_insert_rowid() END", null);
                        if (_id > 0) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case VIDEO:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(VideoEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case REVIEW:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ReviewEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}