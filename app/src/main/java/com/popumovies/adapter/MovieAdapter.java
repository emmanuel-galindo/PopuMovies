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
//        private final ImageView posterViewStar;
        private final FloatingActionButton posterViewFavorite;

        // --Commented out by Inspection (4/28/17 11:42 AM):private ClickListener clickListener;


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
                //Context context = v.getContext();

                long movieId = getItemId(pos);

//                Uri data = MovieEntry.buildSingleMovieUri(id);
                // I send both pos and id, callback receiver will assert what to use
                // based on twoPane
                ((MainActivityFragment.Callback) mContext).onItemSelected(pos,movieId);
//                Intent intent = new Intent(mContext, DetailActivity.class).setData(data);
//                intent.putExtra("pos",pos);
//                mContext.startActivity(intent);
            }
        });

        // Poster image is composed of 3 parts
        // 1 - The base URL will look like: http://image.tmdb.org/t/p/.
        // 2 - Then you will need a ‘size’, which will be one of the following:
        //  "w92", "w154", "w185", "w342", "w500", "w780", or "original". For most phones we recommend using “w185”.
        // 3 - And finally the poster path returned by the query, in this case “/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg”
        final String poster = "http://image.tmdb.org/t/p/w185"+cursor.getString(MovieEntry.COLUMN_POS_POSTER_PATH);
        //Log.d(LOG_TAG, "Loading poster url => " + poster);



//        File httpCacheDirectory = new File(mContext.getCacheDir(), "responses");
//        int cacheSize = 10*1024*1024;
//        Cache cache = new Cache(httpCacheDirectory, cacheSize);
//
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addNetworkInterceptor(new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
//                        Response originalResponse = chain.proceed(chain.request());
//                        return originalResponse.newBuilder().header("Cache-Control", "max-age=" + (60 * 60 * 24 * 365)).build();
//                    } })
//                .cache(cache)
//                .build();
//
////        okHttpClient.cache(new Cache(mainActivity.getCacheDir(), Integer.MAX_VALUE));
//        OkHttp3Downloader okHttpDownloader = new OkHttp3Downloader(okHttpClient);
//        Picasso picasso = new Picasso.Builder(mContext).downloader(okHttpDownloader).build();
//        picasso.setIndicatorsEnabled(true);
//        picasso.load(poster).into(holder.posterView);
////


//        picasso.setLoggingEnabled(true);
        mPicasso.load(poster)
//                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.posterView);

//        Picasso.with(mContext)
//                .load(poster)
//                .networkPolicy(NetworkPolicy.OFFLINE)
//                .into(holder.posterView);

//        Picasso.with(mContext).load(poster)
//                .into(holder.posterView);

//        Picasso picasso = Picasso.with(mContext);
//        picasso.setIndicatorsEnabled(true);
//        picasso.load(poster).into(holder.posterView);
        holder.posterView.setContentDescription(cursor.getString(
                MovieEntry.COLUMN_POS_ORIGINAL_TITLE));

//        String title = cursor.getString(MovieEntry.COLUMN_POS_TITLE);
//        int fav = cursor.getInt(MovieEntry.COLUMN_POS_FAVORITE);
        // if favorite, show the star
        if (cursor.getInt(MovieEntry.COLUMN_POS_FAVORITE) != 0)
            holder.posterViewFavorite.setVisibility(View.VISIBLE);
        else
            holder.posterViewFavorite.setVisibility(View.INVISIBLE);

    }

}