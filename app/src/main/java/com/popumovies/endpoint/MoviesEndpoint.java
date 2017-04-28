package com.popumovies.endpoint;

import com.popumovies.model.Movie;
import com.popumovies.model.MoviesResults;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by manu on 4/5/16.
 */
public interface MoviesEndpoint {

    // Request method and URL specified in the annotation
    // Callback for the parsed response is the last parameter
    @GET("movie/top_rated")
    Call<MoviesResults> topRatedList();

    @GET("movie/popular")
    Call<MoviesResults>     popularList();

    @GET("movie/{id}?append_to_response=videos,reviews")
    Call<Movie> movie(
            @Path("id") int tmdbId
    );


}
