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

package com.popumovies.endpoint;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.popumovies.R;
import com.popumovies.utils.HelperOkHttpClient;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {
    private final String LOG_TAG = ApiManager.class.getSimpleName();

    static private String mBaseUrl;
    static private String mApiKeyLabel;
    static private String mApiKeyValue;

    private final Retrofit retrofit;

    public ApiManager(Context context) {
        mBaseUrl = context.getString(R.string.TMDB_API_URL);
        mApiKeyLabel = context.getString(R.string.TMDB_API_KEY_LABEL);
        mApiKeyValue = context.getString(R.string.TMDB_API_KEY_VALUE);

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        OkHttpClient okHttpClient = getOkHttpClient();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(mBaseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

    }

    private OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder okHttpClientBuilder = HelperOkHttpClient.getOkHttpClientBuilder();
        if (okHttpClientBuilder.interceptors().size() == 0) {
            okHttpClientBuilder.addInterceptor(
                    new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request();
                            HttpUrl url = request.url().newBuilder().addQueryParameter(
                                    mApiKeyLabel, mApiKeyValue).build();
                            request = request.newBuilder().url(url).build();
                            //Log.d(LOG_TAG, "url => " + url.toString());
                            return chain.proceed(request);
                        }
                    }
            );
        }
        return okHttpClientBuilder.build();
    }

    public MoviesEndpoint movies() {
        return getRetrofit().create(MoviesEndpoint.class);
    }

    private Retrofit getRetrofit() {
        return retrofit;
    }
}
