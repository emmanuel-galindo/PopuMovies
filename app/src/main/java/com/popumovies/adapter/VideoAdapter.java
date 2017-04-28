package com.popumovies.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.popumovies.R;
import com.popumovies.data.MovieContract.VideoEntry;
import com.popumovies.utils.HelperOkHttpClient;
import com.squareup.picasso.Picasso;

import java.net.URISyntaxException;

/**
 * {@link VideoAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class VideoAdapter extends CursorRecyclerAdapter<VideoAdapter.ViewHolder> {
    private static final String YOUTUBE_DEV_KEY = "AIzaSyAYPDK84iJv_dDiDr13V4MJ7eMKWYYVaGI";
    private final String LOG_TAG = VideoAdapter.class.getSimpleName();


    private final Context mContext;
    private final Picasso mPicasso;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder extends BaseRecyclerViewAdapterViewHolder {

        private final ImageView mThumbnail;


        public ViewHolder(View view) {
            super(view);
            mThumbnail = (ImageView) view.findViewById(R.id.imageview_list_item_thumbnail);

            // We set listeners to the whole item view, but we could also
            // specify listeners for the title or the icon.
            view.setOnClickListener(this);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_video, parent, false);
        return new ViewHolder(itemView);
    }

    public VideoAdapter(Context context, Cursor c) {
        super(c);
        mContext = context;
        mPicasso = new HelperOkHttpClient().getPicassoInstance(mContext);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, final Cursor cursor) {
        holder.setClickListener(new ViewHolder.ClickListener() {
            @Override
            public void onClick(View v, int pos) throws URISyntaxException {
                // Load the selected youtube id
                String youtubeId = cursor.getString(VideoEntry.COLUM_POS_KEY);
                // Youtube app cannot be installed in the emulator
                // We check if the youtube app is installed and available
                YouTubeInitializationResult availableReason =
                        YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(mContext);
                if ( availableReason ==
                        YouTubeInitializationResult.SUCCESS) {
                    //Context context = v.getContext();
                    // 0 arg is the time time to wait until it starts playing the video
                    // last two true args are for autoplay at startup, and to show the video in
                    // a box on the same activity
                    Intent intent = YouTubeStandalonePlayer.createVideoIntent((Activity) mContext,
                            YOUTUBE_DEV_KEY, youtubeId, 0, true, true);
                    mContext.startActivity(intent);
                    YouTubeInitializationResult errorReason =
                            YouTubeStandalonePlayer.getReturnedInitializationResult(intent);
                } else {
//                    if (availableReason.isUserRecoverableError()) {
//                        availableReason.getErrorDialog((Activity) mContext, 1).show();
//                    } else {
//                        Toast.makeText(
//                                mContext,
//                                R.string.youtube_not_available + availableReason.toString(),
//                                Toast.LENGTH_LONG);
//                    }
                    // If it is not available, fallback to webbrowser
                    Intent webIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com/watch?v=" + youtubeId));
                    mContext.startActivity(webIntent);
                }
            }
        });

        /*
        Load the thumbnail. Here is a good explanation of the links
        http://stackoverflow.com/questions/2068344/how-do-i-get-a-youtube-video-thumbnail-from-the-youtube-api
         */
        //TODO: hardcode...
        String thumb = "http://img.youtube.com/vi/" +
                cursor.getString(VideoEntry.COLUM_POS_KEY) +
                "/1.jpg";
        Log.d(LOG_TAG, "Loading thumb => " + thumb);
        mPicasso.load(thumb)
                .into(holder.mThumbnail);
//        Picasso.with(mContext).load(thumb)
//                .into(holder.mThumbnail);
        holder.mThumbnail.setContentDescription(cursor.getString(
                VideoEntry.COLUM_POS_NAME));

    }

}