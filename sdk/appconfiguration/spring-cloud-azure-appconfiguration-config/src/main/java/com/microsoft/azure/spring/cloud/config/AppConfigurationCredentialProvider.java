/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.azure.core.credential.TokenCredential;

public interface AppConfigurationCredentialProvider {

    public TokenCredential getAppConfigCredential(String uri);

}
