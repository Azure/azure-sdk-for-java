// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;

/** Endpoints and credentials for publishing to a web app. */
@Fluent
public interface PublishingProfile {
    /**
     * Gets the URL for FTP publishing.
     *
     * @return the URL for FTP publishing, with ftp:// and the root folder. E.g. ftp://ftp.contoso.com/site/wwwroot
     */
    String ftpUrl();

    /**
     * Gets the username used for FTP publishing
     *
     * @return the username used for FTP publishing
     */
    String ftpUsername();

    /**
     * Gets the password used for FTP publishing.
     *
     * @return the password used for FTP publishing
     */
    String ftpPassword();

    /**
     * Gets the URL for FTP publishing.
     *
     * @return the URL for FTP publishing, with https:// upfront. E.g. https://contoso.com:443/myRepo.git
     */
    String gitUrl();

    /**
     * Gets the username used for Git publishing.
     *
     * @return the username used for Git publishing
     */
    String gitUsername();

    /**
     * Gets the password used for Git publishing.
     *
     * @return the password used for Git publishing
     */
    String gitPassword();
}
