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
 * Stetho related interceptors, or debug flags (as mPicasso.setIndicatorsEnabled), etc
 */
public class HelperOkHttpClient {

    private static OkHttpClient.Builder mOkHttpClientBuilder;
    private Picasso mPicasso;

    public HelperOkHttpClient() {
    }

    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        if (mOkHttpClientBuilder == null)
            mOkHttpClientBuilder = new OkHttpClient.Builder();
        return mOkHttpClientBuilder;
    }

    public Picasso getPicassoInstance(Context context) {
        if (mPicasso == null ) {
            File httpCacheDirectory = new File(context.getCacheDir(), "responses");
            int cacheSize = 10 * 1024 * 1024;
            Cache cache = new Cache(httpCacheDirectory, cacheSize);

            OkHttpClient okHttpClient = getOkHttpClientBuilder()
                    .addNetworkInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Response originalResponse = chain.proceed(chain.request());
                            return originalResponse.newBuilder().header(
                                    "Cache-Control", "max-age=" + (60 * 60 * 24 * 365)).build();
                        }
                    })
                    .cache(cache)
                    .build();

            OkHttp3Downloader okHttpDownloader = new OkHttp3Downloader(okHttpClient);
            mPicasso = new Picasso.Builder(context).downloader(okHttpDownloader).build();
            mPicasso.setIndicatorsEnabled(true);
        }
        return mPicasso;
    }
}
