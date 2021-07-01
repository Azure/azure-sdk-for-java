// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config;

import com.azure.core.credential.TokenCredential;

public interface TokenCredentialProvider {

    public TokenCredential credentialForAppConfig(String uri);

    public TokenCredential credentialForKeyVault(String uri);

}
