// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.api;

import com.azure.core.credential.TokenCredential;

/**
 * Interface to provide the {@link TokenCredential} that will be used to call the service.
 *
 * @author Warren Zhu
 */
public interface CredentialsProvider {

    TokenCredential getCredential();
}
