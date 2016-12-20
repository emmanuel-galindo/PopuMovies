package com.popumovies.utils;

import okhttp3.OkHttpClient;

/**
 * Created by manu on 4/29/16.
 */
public class HelperOkHttpClient {
    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
        return okHttpClientBuilder;
    }

    public static Picasso getPicassoInstance(Context context) {
        Picasso picasso = Picasso.with(context);
        return picasso;
    }
}
