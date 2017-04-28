package com.popumovies.endpoint;

//import com.facebook.stetho.okhttp3.StethoInterceptor;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.popumovies.utils.HelperOkHttpClient;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by manu on 4/5/16.
 */
public class ApiManager {
    private final String LOG_TAG = ApiManager.class.getSimpleName();

    //TODO: hardcode alert??
    // Trailing slash is needed
    final static private String BASE_URL = "http://api.themoviedb.org/3/";
    final static private String API_KEY_LABEL = "api_key";
    final static private String API_KEY_VALUE = "ba7a9d0e2fb18d7d47b1b4bfaabc4d04";

    private final Retrofit retrofit;

    public ApiManager() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        OkHttpClient okHttpClient = getOkHttpClient();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

    }

    private OkHttpClient getOkHttpClient() {
//        final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
//        okHttpClientBuilder.addInterceptor(
//                new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
//                        Request request = chain.request();
//                        HttpUrl url = request.url().newBuilder().addQueryParameter(
//                                API_KEY_LABEL, API_KEY_VALUE).build();
//                        request = request.newBuilder().url(url).build();
//                        return chain.proceed(request);
//                    }
//                });
////        okHttpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
//        OkHttpClient okHttpClient = okHttpClientBuilder.build();
        OkHttpClient.Builder okHttpClientBuilder = HelperOkHttpClient.getOkHttpClientBuilder();
        if (okHttpClientBuilder.interceptors().size() == 0) {
            okHttpClientBuilder.addInterceptor(
                    new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request();
                            HttpUrl url = request.url().newBuilder().addQueryParameter(
                                    API_KEY_LABEL, API_KEY_VALUE).build();
                            request = request.newBuilder().url(url).build();
                            Log.d(LOG_TAG, "url => " + url.toString());
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
