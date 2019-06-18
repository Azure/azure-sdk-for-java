// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.policy.HttpPipelinePolicy;

/**
 * Credentials represent any credential type
 * it is used to create a credential policy Factory.
 */
public interface ICredentials extends HttpPipelinePolicy {

}
