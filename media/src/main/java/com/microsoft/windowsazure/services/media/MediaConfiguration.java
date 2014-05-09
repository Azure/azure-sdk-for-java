/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.media;

import com.microsoft.windowsazure.Configuration;

/**
 * Provides functionality to create a media services configuration.
 * 
 */
public final class MediaConfiguration {

    private MediaConfiguration() {
    }

    /**
     * Defines the media service configuration URI constant.
     * 
     */
    public static final String URI = "media.uri";

    /**
     * Defines the OAUTH configuration URI constant.
     * 
     */
    public static final String OAUTH_URI = "media.oauth.uri";

    /**
     * Defines the OAUTH configuration client ID constant.
     * 
     */
    public static final String OAUTH_CLIENT_ID = "media.oauth.client.id";

    /**
     * Defines the OAUTH configuration client secret constant.
     * 
     */
    public static final String OAUTH_CLIENT_SECRET = "media.oauth.client.secret";

    /**
     * Defines the SCOPE of the media service sent to OAUTH.
     */
    public static final String OAUTH_SCOPE = "media.oauth.scope";

    /**
     * Creates a media service configuration using the specified media service
     * base URI, OAUTH URI, client ID, and client secret.
     * 
     * @param mediaServiceUri
     *            A <code>String</code> object that represents the media service
     *            URI.
     * 
     * @param oAuthUri
     *            A <code>String</code> object that represents the OAUTH URI.
     * 
     * @param clientId
     *            A <code>String</code> object that represents the client ID.
     * 
     * @param clientSecret
     *            A <code>String</code> object that represents the client
     *            secret.
     * 
     * @param scope
     *            A <code>String</code> object that represents the scope.
     * 
     * @return A <code>Configuration</code> object that can be used when
     *         creating an instance of the <code>MediaService</code> class.
     * 
     */
    public static Configuration configureWithOAuthAuthentication(
            String mediaServiceUri, String oAuthUri, String clientId,
            String clientSecret, String scope) {
        return configureWithOAuthAuthentication(null,
                Configuration.getInstance(), mediaServiceUri, oAuthUri,
                clientId, clientSecret, scope);
    }

    /**
     * Creates a media service configuration using the specified configuration,
     * media service base URI, OAuth URI, client ID, and client secret.
     * 
     * @param configuration
     *            A previously instantiated <code>Configuration</code> object.
     * 
     * @param mediaServiceUri
     *            A <code>String</code> object that represents the URI of media
     *            service.
     * 
     * @param oAuthUri
     *            A <code>String</code> object that represents the URI of OAuth
     *            service.
     * 
     * @param clientId
     *            A <code>String</code> object that represents the client ID.
     * 
     * @param clientSecret
     *            A <code>String</code> object that represents the client
     *            secret.
     * 
     * @param scope
     *            A <code>String</code> object that represents the scope.
     * 
     * @return A <code>Configuration</code> object that can be used when
     *         creating an instance of the <code>MediaService</code> class.
     * 
     */
    public static Configuration configureWithOAuthAuthentication(
            Configuration configuration, String mediaServiceUri,
            String oAuthUri, String clientId, String clientSecret, String scope) {
        return configureWithOAuthAuthentication(null, configuration,
                mediaServiceUri, oAuthUri, clientId, clientSecret, scope);
    }

    /**
     * Creates a media service configuration using the specified profile,
     * configuration, media service base URI, OAuth URI, client ID, and client
     * secret.
     * 
     * @param profile
     *            A <code>String</code> object that represents the profile.
     * 
     * @param configuration
     *            A previously instantiated <code>Configuration</code> object.
     * 
     * @param mediaServiceUri
     *            A <code>String</code> object that represents the URI of media
     *            service.
     * 
     * @param oAuthUri
     *            A <code>String</code> object that represents the URI of OAUTH
     *            service.
     * 
     * @param clientId
     *            A <code>String</code> object that represents the client ID.
     * 
     * @param clientSecret
     *            A <code>String</code> object that represents the client
     *            secret.
     * 
     * @param scope
     *            A <code>String</code> object that represents the scope.
     * 
     * @return A <code>Configuration</code> object that can be used when
     *         creating an instance of the <code>MediaService</code> class.
     * 
     */
    public static Configuration configureWithOAuthAuthentication(
            String profile, Configuration configuration,
            String mediaServiceUri, String oAuthUri, String clientId,
            String clientSecret, String scope) {

        if (profile == null) {
            profile = "";
        } else if (profile.length() != 0 && !profile.endsWith(".")) {
            profile = profile + ".";
        }

        configuration.setProperty(profile + URI, mediaServiceUri);
        configuration.setProperty(profile + OAUTH_URI, oAuthUri);
        configuration.setProperty(profile + OAUTH_CLIENT_ID, clientId);
        configuration.setProperty(profile + OAUTH_CLIENT_SECRET, clientSecret);
        configuration.setProperty(profile + OAUTH_SCOPE, scope);

        return configuration;
    }
}
