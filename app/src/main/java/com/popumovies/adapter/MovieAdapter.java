package com.popumovies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.popumovies.MainActivityFragment;
import com.popumovies.R;
import com.squareup.picasso.Picasso;

/**
 * {@link MovieAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class MovieAdapter extends CursorRecyclerAdapter<MovieAdapter.ViewHolder> {


    private Context mContext;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView posterView;

        public ViewHolder(View view) {
            super(view);
            posterView = (ImageView) view.findViewById(R.id.list_item_poster);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_movie, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    public MovieAdapter(Context context, Cursor c, int flags) {
        super(c);
        mContext = context;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        // Poster image is composed of 3 parts
        // 1 - The base URL will look like: http://image.tmdb.org/t/p/.
        // 2 - Then you will need a ‘size’, which will be one of the following:
        //  "w92", "w154", "w185", "w342", "w500", "w780", or "original". For most phones we recommend using “w185”.
        // 3 - And finally the poster path returned by the query, in this case “/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg”
        Picasso.with(mContext).load("http://image.tmdb.org/t/p/w185"+cursor.getString(MainActivityFragment.COLUMN_POSTER_PATH))
                .into(holder.posterView);
        holder.posterView.setContentDescription(cursor.getString(
                MainActivityFragment.COLUMN_ORIGINAL_TITLE));

    }

}