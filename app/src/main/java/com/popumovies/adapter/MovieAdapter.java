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

package com.popumovies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.popumovies.MainActivityFragment;
import com.popumovies.R;
import com.popumovies.data.MovieContract.MovieEntry;
import com.popumovies.utils.HelperOkHttpClient;
import com.squareup.picasso.Picasso;

import java.net.URISyntaxException;

/**
 * {@link MovieAdapter} exposes a list of movies
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class MovieAdapter extends CursorRecyclerAdapter<MovieAdapter.ViewHolder> {
    private final String LOG_TAG = MovieAdapter.class.getSimpleName();
    private final Picasso mPicasso;


    private final Context mContext;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder extends BaseRecyclerViewAdapterViewHolder {
        public final ImageView posterView;
        private final FloatingActionButton posterViewFavorite;

        public ViewHolder(View view) {
            super(view);
            posterView = (ImageView) view.findViewById(R.id.list_item_poster);
            posterViewFavorite = (FloatingActionButton) view.findViewById(R.id.list_item_poster_favorite);

            // We set listeners to the whole item view, but we could also
            // specify listeners for the title or the icon.
            view.setOnClickListener(this);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_movie, parent, false);
        return new ViewHolder(itemView);
    }

    public MovieAdapter(Context context, Cursor c) {
        super(c);
        mContext = context;

        mPicasso = new HelperOkHttpClient().getPicassoInstance(mContext);

    }


    @Override
    public void onBindViewHolder(ViewHolder holder, final Cursor cursor) {
        holder.setClickListener(new ViewHolder.ClickListener() {
            @Override
            public void onClick(View v, int pos) throws URISyntaxException {
                long movieId = getItemId(pos);
                // I send both pos and id, callback receiver will assert what to use
                // based on twoPane
                ((MainActivityFragment.Callback) mContext).onItemSelected(pos,movieId);
            }
        });

        // Poster image is composed of 3 parts
        // 1 - The base URL will look like: http://image.tmdb.org/t/p/.
        // 2 - Then you will need a ‘size’, which will be one of the following:
        //  "w92", "w154", "w185", "w342", "w500", "w780", or "original".
        // For most phones we recommend using “w185”.
        // 3 - And finally the poster path returned by the query,
        // in this case “/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg”
        final String poster = "http://image.tmdb.org/t/p/w185"
                +cursor.getString(MovieEntry.COLUMN_POS_POSTER_PATH);

        mPicasso.load(poster)
                .into(holder.posterView);

        holder.posterView.setContentDescription(cursor.getString(
                MovieEntry.COLUMN_POS_ORIGINAL_TITLE));

        if (cursor.getInt(MovieEntry.COLUMN_POS_FAVORITE) != 0)
            holder.posterViewFavorite.setVisibility(View.VISIBLE);
        else
            holder.posterViewFavorite.setVisibility(View.INVISIBLE);

    }
}