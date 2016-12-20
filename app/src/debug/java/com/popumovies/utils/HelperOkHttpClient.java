package com.popumovies.utils;

import android.content.Context;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;

/**
 * This file contains code that should only be executed for buildType debug as
 * Stetho related interceptors, or debug flags (as picasso.setIndicatorsEnabled), etc
 */
public class HelperOkHttpClient {
    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
        return okHttpClientBuilder;
    }

    public static Picasso getPicassoInstance(Context context) {
//        OkHttpClient okHttpClient = getOkHttpClientBuilder().build();
//        Picasso picasso = new Picasso.Builder(context)
//                .downloader(new OkHttp3Downloader(okHttpClient))
//                .build();
        Picasso picasso = Picasso.with(context);
        picasso.setIndicatorsEnabled(true);
        return picasso;
    }
}
