// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.core.annotation.ServiceClient;

/**
 * Asynchronous client for Communication service phone number operations
 */
@ServiceClient(builder = PhoneNumberClientBuilder.class, isAsync = true)
public final class PhoneNumberAsyncClient {

    PhoneNumberAsyncClient() {}

}
