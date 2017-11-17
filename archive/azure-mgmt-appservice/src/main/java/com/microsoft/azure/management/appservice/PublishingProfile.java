/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * Endpoints and credentials for publishing to a web app.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
@Beta
public interface PublishingProfile {
    /**
     * @return the url for FTP publishing, with ftp:// and the root folder.
     * E.g. ftp://ftp.contoso.com/site/wwwroot
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
     * @return the url for FTP publishing, with https:// upfront.
     * E.g. https://contoso.com:443/myRepo.git
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
