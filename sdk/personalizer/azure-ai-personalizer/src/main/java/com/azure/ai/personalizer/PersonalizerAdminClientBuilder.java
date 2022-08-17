// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.util.logging.ClientLogger;


/**
 * A builder for creating a new instance of PersonalizerAdminAsyncClient and PersonalizerAdminClient.
 */
@ServiceClientBuilder(serviceClients = {PersonalizerAsyncClient.class, PersonalizerClient.class})
public final class PersonalizerAdminClientBuilder extends PersonalizerClientBuilderBase {

    PersonalizerAdminClientBuilder() {
        super();
        this.setLogger(new ClientLogger(PersonalizerAdminClientBuilder.class));
    }

    /**
     * Create a {@link PersonalizerAdminClient} object to invoke the administrative functions of Personalizer service.
     * @return the created object.
     */
    public PersonalizerAdminClient buildAdminClient() {
        return new PersonalizerAdminClient(buildAdminAsyncClient());
    }

    /**
     * Create a {@link PersonalizerAdminAsyncClient} object to invoke the administrative functions of Personalizer service in an asynchronous manner.
     * @return the created object.
     */
    public PersonalizerAdminAsyncClient buildAdminAsyncClient() {
        return new PersonalizerAdminAsyncClient(getService());
    }

}
