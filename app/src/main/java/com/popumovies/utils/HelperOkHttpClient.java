package com.popumovies.utils;

import android.content.Context;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * This file contains code that should only be executed for buildType debug as
 * Stetho related interceptors, or debug flags (as picasso.setIndicatorsEnabled), etc
 */
public class HelperOkHttpClient {
    private static OkHttpClient.Builder okHttpClientBuilder;
    private Picasso picasso;

    public HelperOkHttpClient() {


    }

    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        if (okHttpClientBuilder == null)
            okHttpClientBuilder = new OkHttpClient.Builder();
        return okHttpClientBuilder;
    }

    public Picasso getPicassoInstance(Context context) {
//        OkHttpClient okHttpClient = getOkHttpClientBuilder().build();
//        Picasso picasso = new Picasso.Builder(context)
//                .downloader(new OkHttp3Downloader(okHttpClient))
//                .build();

        //todo: offline, this leads to app unstable, at the end too many open files
        // maybe we need to put the builder somehwere else
        if (picasso == null ) {
            File httpCacheDirectory = new File(context.getCacheDir(), "responses");
            int cacheSize = 10 * 1024 * 1024;
            Cache cache = new Cache(httpCacheDirectory, cacheSize);

            //        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            OkHttpClient okHttpClient = getOkHttpClientBuilder()
                    .addNetworkInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Response originalResponse = chain.proceed(chain.request());
                            return originalResponse.newBuilder().header("Cache-Control", "max-age=" + (60 * 60 * 24 * 365)).build();
                        }
                    })
                    .cache(cache)
                    .build();

            //        okHttpClient.cache(new Cache(mainActivity.getCacheDir(), Integer.MAX_VALUE));
            OkHttp3Downloader okHttpDownloader = new OkHttp3Downloader(okHttpClient);
            picasso = new Picasso.Builder(context).downloader(okHttpDownloader).build();
            picasso.setIndicatorsEnabled(true);
        }
        return picasso;
//        Picasso picasso = Picasso.with(context);
//        picasso.setIndicatorsEnabled(true);
//        return picasso;
    }
}
