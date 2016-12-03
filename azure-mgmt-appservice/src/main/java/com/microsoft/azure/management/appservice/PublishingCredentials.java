/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.appservice.implementation.UserInner;

/**
 * A credential for publishing to a web app.
 */
@Fluent
public interface PublishingCredentials extends
        Wrapper<UserInner> {
    /**
     * @return the username used for FTP and Git publishing.
     */
    String username();

    /**
     * @return the password used for FTP and Git publishing.
     */
    String password();
}
