/*
 * Copyright 2015 Miguel Teixeira
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
 * 
 */

package com.popumovies.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Videos {
    @SerializedName("results")
    public List<Video> results;

    public static class Video {
        @SerializedName("id")
        private String id;
        @SerializedName("iso_639_1")
        private String iso6391;
        @SerializedName("iso_3166_1")
        private String iso31661;
        @SerializedName("key")
        private String key;
        @SerializedName("name")
        private String name;
        @SerializedName("site")
        private String site;
        @SerializedName("size")
        private int size;
        @SerializedName("type")
        private String type;
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
         *
         * @return
         *     The id
         */
        public String getId() {
            return id;
        }

        /**
         *
         * @param id
         *     The id
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         *
         * @return
         *     The iso6391
         */
        public String getIso6391() {
            return iso6391;
        }

        /**
         *
         * @param iso6391
         *     The iso_639_1
         */
        public void setIso6391(String iso6391) {
            this.iso6391 = iso6391;
        }

        /**
         *
         * @return
         *     The iso31661
         */
        public String getIso31661() {
            return iso31661;
        }

        /**
         *
         * @param iso31661
         *     The iso_3166_1
         */
        public void setIso31661(String iso31661) {
            this.iso31661 = iso31661;
        }

        /**
         *
         * @return
         *     The key
         */
        public String getKey() {
            return key;
        }

        /**
         *
         * @param key
         *     The key
         */
        public void setKey(String key) {
            this.key = key;
        }

        /**
         *
         * @return
         *     The name
         */
        public String getName() {
            return name;
        }

        /**
         *
         * @param name
         *     The name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         *
         * @return
         *     The site
         */
        public String getSite() {
            return site;
        }

        /**
         *
         * @param site
         *     The site
         */
        public void setSite(String site) {
            this.site = site;
        }

        /**
         *
         * @return
         *     The size
         */
        public int getSize() {
            return size;
        }

        /**
         *
         * @param size
         *     The size
         */
        public void setSize(int size) {
            this.size = size;
        }

        /**
         *
         * @return
         *     The type
         */
        public String getType() {
            return type;
        }

        /**
         *
         * @param type
         *     The type
         */
        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }


}
