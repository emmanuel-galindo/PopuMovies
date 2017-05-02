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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.popumovies.R;
import com.popumovies.data.MovieContract.ReviewEntry;
import com.popumovies.utils.ExpandableTextView;

import java.net.URISyntaxException;

/**
 * {@link ReviewAdapter} exposes a list of movie reviews
 * from a {@link CursorRecyclerAdapter} to a {@link android.support.v7.widget.RecyclerView}.
 */
public class ReviewAdapter extends CursorRecyclerAdapter<ReviewAdapter.ViewHolder> {
    private final String LOG_TAG = ReviewAdapter.class.getSimpleName();

    private final Context mContext;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder extends BaseRecyclerViewAdapterViewHolder {

        private final ExpandableTextView mReviewText;
        private final TextView mReviewAuthor;


        public ViewHolder(View view) {
            super(view);
            mReviewAuthor = (TextView) view.findViewById(R.id.textview_list_item_author);
            mReviewText = (ExpandableTextView) view.findViewById(R.id.textview_list_item_review);

            // We set listeners to the whole item view, but we could also
            // specify listeners for the title or the icon.
            view.setOnClickListener(this);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_review, parent, false);
        return new ViewHolder(itemView);
    }

    public ReviewAdapter(Context context, Cursor c) {
        super(c);
        mContext = context;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, final Cursor cursor) {
        holder.setClickListener(new ViewHolder.ClickListener() {
            @Override
            public void onClick(View v, int pos) throws URISyntaxException {
                //Context context = v.getContext();
                // 0 arg is the time time to wait until it starts playing the review
                // last two true args are for autoplay at startup, and to show the review in
                // a box on the same activity
            }
        });

        /*
        Load the thumbnail. Here is a good explanation of the links
        http://stackoverflow.com/questions/2068344/how-do-i-get-a-youtube-review-thumbnail-from-the-youtube-api
         */
        holder.mReviewText.setText(cursor.getString(ReviewEntry.COLUM_POS_CONTENT));
        holder.mReviewAuthor.setText(String.format(mContext.getString(R.string.label_areviewby),
                cursor.getString(ReviewEntry.COLUM_POS_AUTHOR)));

    }

}