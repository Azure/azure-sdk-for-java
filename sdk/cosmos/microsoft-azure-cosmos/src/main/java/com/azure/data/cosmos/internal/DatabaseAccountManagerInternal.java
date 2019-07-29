// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.ConnectionPolicy;
import reactor.core.publisher.Flux;

import java.net.URI;

public interface DatabaseAccountManagerInternal {

    /**
     * Gets database account information.
     *
     * @param endpoint the endpoint from which gets the database account
     * @return the database account.
     */
    Flux<DatabaseAccount> getDatabaseAccountFromEndpoint(URI endpoint);

    /**
     * Gets the connection policy
     *
     * @return connection policy
     */
    ConnectionPolicy getConnectionPolicy();

    /**
     * Gets the service endpoint
     *
     * @return service endpoint
     */
    URI getServiceEndpoint();

}
