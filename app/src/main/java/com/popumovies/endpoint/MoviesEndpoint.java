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

import com.popumovies.model.Movie;
import com.popumovies.model.MoviesResults;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

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
