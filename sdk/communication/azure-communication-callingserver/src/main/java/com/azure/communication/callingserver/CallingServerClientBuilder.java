// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.core.annotation.ServiceClientBuilder;

/**
 * Client builder that creates CallingServerAsyncClient and CallingServerClient.
 *
 * <p><strong>Instantiating synchronous and asynchronous Calling Server Clients</strong></p>
 */

@ServiceClientBuilder(serviceClients = { CallingServerClient.class, CallingServerAsyncClient.class })
public final class CallingServerClientBuilder {
}
