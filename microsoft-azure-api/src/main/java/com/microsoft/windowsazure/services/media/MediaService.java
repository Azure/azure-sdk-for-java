/**
 * Copyright 2012 Microsoft Corporation
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

import java.net.URI;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.media.implementation.MediaBlobContainerWriter;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;
import com.sun.jersey.api.client.Client;

/**
 * 
 * Access media services functionality. This class cannot
 * be instantiated.
 * 
 */
public class MediaService {

    private MediaService() {
    }

    /**
     * Creates an instance of the <code>MediaServicesContract</code> API.
     * 
     */
    public static MediaContract create() {
        return Configuration.getInstance().create(MediaContract.class);
    }

    /**
     * Creates an instance of the <code>MediaServicesContract</code> API using the specified configuration.
     * 
     * @param config
     *            A <code>Configuration</code> object that represents the configuration for the service bus service.
     * 
     */
    public static MediaContract create(Configuration config) {
        return config.create(MediaContract.class);
    }

    /**
     * Creates an instance of the <code>MediaServicesContract</code> API.
     * 
     */
    public static MediaContract create(String profile) {
        return Configuration.getInstance().create(profile, MediaContract.class);
    }

    /**
     * Creates an instance of the <code>MediaServicesContract</code> API using the specified configuration.
     * 
     * @param config
     *            A <code>Configuration</code> object that represents the configuration for the service bus service.
     * 
     */
    public static MediaContract create(String profile, Configuration config) {
        return config.create(profile, MediaContract.class);
    }

    /**
     * Creates an instance of the <code>WritableBlobContainerContract</code> API that will
     * write to the blob container given by the provided locator.
     * 
     * @param locator
     *            locator specifying where to upload to
     * @return the implementation of <code>WritableBlobContainerContract</code>
     */
    public static WritableBlobContainerContract createBlobWriter(LocatorInfo locator) {
        if (locator.getLocatorType() != LocatorType.SAS) {
            throw new IllegalArgumentException("Can only write to SAS locators");
        }

        LocatorParser p = new LocatorParser(locator);

        return new MediaBlobContainerWriter(createUploaderClient(), p.getAccountName(), p.getStorageUri(),
                p.getContainer(), p.getSASToken());
    }

    /**
     * Helper class to encapsulate pulling information out of the locator.
     */
    private static class LocatorParser {
        URI locatorPath;

        LocatorParser(LocatorInfo locator) {
            locatorPath = URI.create(locator.getPath());
        }

        String getAccountName() {
            return locatorPath.getHost().split("\\.")[0];
        }

        String getStorageUri() {
            return locatorPath.getScheme() + "://" + locatorPath.getAuthority();
        }

        String getContainer() {
            return locatorPath.getPath().substring(1);
        }

        String getSASToken() {
            return locatorPath.getRawQuery();
        }
    }

    private static Client createUploaderClient() {
        Client client = Client.create();
        return client;
    }
}
