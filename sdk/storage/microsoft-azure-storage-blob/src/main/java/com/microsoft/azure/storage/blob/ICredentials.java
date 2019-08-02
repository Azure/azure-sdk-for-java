// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import com.microsoft.rest.v2.policy.RequestPolicyFactory;

/**
 * Credentials represent any credential type
 * it is used to create a credential policy Factory.
 */
public interface ICredentials extends RequestPolicyFactory {

}
