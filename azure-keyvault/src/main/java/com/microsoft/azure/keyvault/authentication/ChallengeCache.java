/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault.authentication;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.microsoft.rest.credentials.ServiceClientCredentials;

import okhttp3.HttpUrl;

/**
 * An implementation of {@link ServiceClientCredentials} that supports automatic bearer token refresh.
 *
 */
class ChallengeCache {

    private final HashMap<String, Map<String, String>> cachedChallenges = new HashMap<String, Map<String, String>>();

    /**
     * Uses authority to retrieve the cached values.
     * 
     * @param url
     *            the url that is used as a cache key.
     * @return cached value or null if value is not available.
     */
    public Map<String, String> getCachedChallenge(HttpUrl url) {
        if (url == null) {
            return null;
        }
        String authority = getAuthority(url);
        authority = authority.toLowerCase(Locale.ENGLISH);
        return cachedChallenges.get(authority);
    }

    /**
     * Uses authority to cache challenge.
     * 
     * @param url
     *            the url that is used as a cache key.
     * @param challenge
     *            the challenge to cache.
     */
    public void addCachedChallenge(HttpUrl url, Map<String, String> challenge) {
        if (url == null || challenge == null) {
            return;
        }
        String authority = getAuthority(url);
        authority = authority.toLowerCase(Locale.ENGLISH);
        cachedChallenges.put(authority, challenge);
    }

    /**
     * Gets authority of a url.
     * 
     * @param url
     *            the url to get the authority for.
     * @return the authority.
     */
    public String getAuthority(HttpUrl url) {
        String scheme = url.scheme();
        String host = url.host();
        int port = url.port();
        StringBuilder builder = new StringBuilder();
        if (scheme != null) {
            builder.append(scheme).append("://");
        }
        builder.append(host);
        if (port >= 0) {
            builder.append(':').append(port);
        }
        return builder.toString();
    }
}
