package com.popumovies.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.net.URISyntaxException;

class BaseRecyclerViewAdapterViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    private ClickListener clickListener;


    public BaseRecyclerViewAdapterViewHolder(View itemView) {
        super(itemView);
    }

    /* Interface for handling clicks - both normal and long ones. */
    public interface ClickListener {

        /**
         * Called when the view is clicked.
         *
         * @param v view that is clicked
         * @param position of the clicked item
         */
        void onClick(View v, int position) throws URISyntaxException;
    }
    /* Setter for listener. */
    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onClick(View v) {

        // If not long clicked, pass last variable as false.
        try {
            clickListener.onClick(v, getLayoutPosition());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
