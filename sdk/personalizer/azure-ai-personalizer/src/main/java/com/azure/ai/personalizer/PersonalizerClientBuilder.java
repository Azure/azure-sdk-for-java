// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.util.logging.ClientLogger;


/**
 * A builder for creating a new instance of PersonalizerAsyncClient and PersonalizerClient.
 */
@ServiceClientBuilder(serviceClients = {PersonalizerAsyncClient.class, PersonalizerClient.class})
public final class PersonalizerClientBuilder extends PersonalizerClientBuilderBase {

    /**
     * Construct a new instance of PersonalizerClientBuilder object.
     */
    public PersonalizerClientBuilder() {
        super();
        this.setLogger(new ClientLogger(PersonalizerClientBuilder.class));
    }

    /**
     * Create a {@link PersonalizerClient} object to invoke the Personalizer service.
     * @return the PersonalizerClient object.
     */
    public PersonalizerClient buildClient() {
        return new PersonalizerClient(buildAsyncClient());
    }

    /**
     * Create a {@link PersonalizerAsyncClient} object to invoke the Personalizer service in an asynchronous manner.
     * @return the created object.
     */
    public PersonalizerAsyncClient buildAsyncClient() {
        return new PersonalizerAsyncClient(getService());
    }
}
