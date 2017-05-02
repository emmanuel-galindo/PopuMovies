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

package com.popumovies.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Reviews {

    @SerializedName("results")
    public List<Review> results;
    @SerializedName("page")
    public Integer page;
    @SerializedName("total_pages")
    public Integer total_pages;
    @SerializedName("total_results")
    public Integer total_results;

    public static class Review {
        @SerializedName("id")
        public String id;
        @SerializedName("author")
        public String author;
        @SerializedName("content")
        public String content;
        @SerializedName("url")
        public String url;

        public String getAuthor() {
            return author;
        }

        public String getId() {
            return id;
        }

        public String getContent() {
            return content;
        }

        public String getUrl() {
            return url;
        }
    }

}
