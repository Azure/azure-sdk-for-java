/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * Endpoints and credentials for publishing to a web app.
 */
@Fluent
public interface PublishingProfile {
    /**
     * @return the url for FTP publishing
     */
    String ftpUrl();
    /**
     * @return the username used for FTP publishing
     */
    String ftpUsername();

    /**
     * @return the password used for FTP publishing
     */
    String ftpPassword();

    /**
     * @return the url for FTP publishing
     */
    String gitUrl();

    /**
     * @return the username used for Git publishing
     */
    String gitUsername();

    /**
     * @return the password used for Git publishing
     */
    String gitPassword();
}
