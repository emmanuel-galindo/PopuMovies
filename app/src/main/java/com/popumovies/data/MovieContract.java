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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the movie database.
 */
public class MovieContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.popumovies";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com..sunshine.app/movie/ is a valid path for
    // looking at movie data. content://com..sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_MOVIE = "movie";
    public static final String PATH_VIDEO = "video";
    public static final String PATH_REVIEW = "review";


    /* Inner class that defines the table contents of the movie table */
    public static final class MovieEntry implements BaseColumns {

        public static final String TABLE_NAME = "movie";
        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TMDB_ID = "tmdb_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_BACKGROUND_PATH = "background_path";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_FAVORITE = "favorite";
        //Columns default positions. When select * is used these index can be used.
        public static final int COLUMN_POS_ID = 0;
        public static final int COLUMN_POS_TMDB_ID = 1;
        public static final int COLUMN_POS_TITLE = 2;
        public static final int COLUMN_POS_ORIGINAL_TITLE = 3;
        public static final int COLUMN_POS_OVERVIEW = 4;
        public static final int COLUMN_POS_RELEASE_DATE = 5;
        public static final int COLUMN_POS_POSTER_PATH = 6;
        public static final int COLUMN_POS_BACKGROUND_PATH = 7;
        public static final int COLUMN_POS_VOTE_AVERAGE = 8;
        public static final int COLUMN_POS_VOTE_COUNT = 9;
        public static final int COLUMN_POS_POPULARITY = 10;
        public static final int COLUMN_POS_FAVORITE = 11;

        // Note: Per the custom bulkInsert, to allow coalesce to define favorite boolean
        // favorite field should always be the last in this list
        public static final String[] MOVIE_COLUMNS = {
                TABLE_NAME + "." + _ID,
                COLUMN_TMDB_ID,
                COLUMN_TITLE,
                COLUMN_ORIGINAL_TITLE,
                COLUMN_OVERVIEW,
                COLUMN_RELEASE_DATE,
                COLUMN_POSTER_PATH,
                COLUMN_BACKGROUND_PATH,
                COLUMN_VOTE_AVERAGE,
                COLUMN_VOTE_COUNT,
                COLUMN_POPULARITY,
                COLUMN_FAVORITE };


        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static Uri buildSingleMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildPopularMovieUri() {
            return CONTENT_URI.buildUpon().appendPath("popular").build();
        }
        public static Uri buildHighRatedMovieUri() {
            return CONTENT_URI.buildUpon().appendPath("high_rated").build();
        }
        public static Uri buildFavoriteMovieUri() {
            return CONTENT_URI.buildUpon().appendPath("favorite").build();
        }
        public static Uri buildMovieWithVideos(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id).buildUpon().
                    appendPath("video").build();
        }
        public static Uri buildMovieWithReviews(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id).buildUpon().
                    appendPath("review").build();
        }
    }


    /* Inner class that defines the table contents of the video table */
    public static final class VideoEntry implements BaseColumns {


        public static final String TABLE_NAME = "video";
        // Date, stored as long in milliseconds since the epoch
        // this is the id from the video, ex: 571c88a4c3a368431500009f
        public static final String COLUMN_ID = "_id";
        // this is the id of the movie related to this video. This is mapped by the application
        // as the relation key between this table and movie
        public static final String COLUMN_TMDB_ID = "tmdb_id";
        public static final String COLUMN_ISO6391 = "iso6391";
        public static final String COLUMN_ISO31661 = "iso31661";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_TYPE = "type";
        //Columns default positions. When select * is used these index can be used.
        public static final int COLUMN_POS_ID = 0;
        public static final int COLUM_POS_TMDB_ID = 1;
        public static final int COLUM_POS_ISO6391 = 2;
        public static final int COLUM_POS_ISO31661 = 3;
        public static final int COLUM_POS_KEY = 4;
        public static final int COLUM_POS_NAME = 5;
        public static final int COLUM_POS_SITE = 6;
        public static final int COLUM_POS_SIZE = 7;
        public static final int COLUM_POS_TYPE = 8;

        public static final String[] VIDEO_COLUMNS = {
                TABLE_NAME + "." + _ID,
                TABLE_NAME + "." + COLUMN_TMDB_ID,
                COLUMN_ISO6391,
                COLUMN_ISO31661,
                COLUMN_KEY,
                COLUMN_NAME,
                COLUMN_SITE,
                COLUMN_SIZE,
                COLUMN_TYPE
        };


        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEO).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;

        public static Uri buildSingleVideoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    /* Inner class that defines the table contents of the review table */
    public static final class ReviewEntry implements BaseColumns {


        public static final String TABLE_NAME = "review";
        // Date, stored as long in milliseconds since the epoch
        // this is the id from the review, ex: 571c88a4c3a368431500009f
        public static final String COLUMN_ID = "_id";
        // this is the id of the movie related to this review. This is mapped by the application
        // as the relation key between this table and movie
        public static final String COLUMN_TMDB_ID = "tmdb_id";
        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_URL = "url";
        //Columns default positions. When select * is used these index can be used.
        public static final int COLUMN_POS_ID = 0;
        public static final int COLUM_POS_TMDB_ID = 1;
        public static final int COLUM_POS_REVIEW_ID = 2;
        public static final int COLUM_POS_AUTHOR = 3;
        public static final int COLUM_POS_CONTENT = 4;
        public static final int COLUM_POS_URL = 5;

        public static final String[] REVIEW_COLUMNS = {
                TABLE_NAME + "." + _ID,
                TABLE_NAME + "." + COLUMN_TMDB_ID,
                COLUMN_REVIEW_ID,
                COLUMN_AUTHOR,
                COLUMN_CONTENT,
                COLUMN_URL
        };


        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static Uri buildSingleReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


}
