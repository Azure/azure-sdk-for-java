// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.networktraversal;

import com.azure.communication.networktraversal.models.CommunicationRelayConfiguration;
import com.azure.communication.networktraversal.models.GetRelayConfigurationOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Synchronous client interface for Communication Service NetworkTraversal operations
 *
 * <p><strong>Instantiating a synchronous CommunicationRelayClient</strong></p>
 *
 * <!-- src_embed readme-sample-createCommunicationNetworkTraversalClient -->
 * <pre>
 * &#47;&#47; You can find your endpoint and access key from your resource in the Azure Portal
 * String endpoint = &quot;https:&#47;&#47;&lt;RESOURCE_NAME&gt;.communication.azure.com&quot;;
 * AzureKeyCredential keyCredential = new AzureKeyCredential&#40;&quot;&lt;access-key&gt;&quot;&#41;;
 *
 * CommunicationRelayClient communicationRelayClient = new CommunicationRelayClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;keyCredential&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createCommunicationNetworkTraversalClient -->
 * <p>View {@link CommunicationRelayClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see CommunicationRelayClientBuilder
 */
@ServiceClient(builder = CommunicationRelayClientBuilder.class, isAsync = false)
public final class CommunicationRelayClient {

    private final CommunicationRelayAsyncClient client;
    private final ClientLogger logger = new ClientLogger(CommunicationRelayClient.class);

    CommunicationRelayClient(CommunicationRelayAsyncClient communicationNetworkingClient) {
        client = communicationNetworkingClient;
    }

    /**
     * Gets a Relay Configuration.
     *
     * @return The obtained Communication Relay Configuration
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRelayConfiguration getRelayConfiguration() {
        return client.getRelayConfiguration().block();
    }

    /**
     * Gets a Relay Configuration for a CommunicationUserIdentifier.
     *
     * @param options of the GetRelayConfigurationOptions request
     * @return The obtained Communication Relay Configuration
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRelayConfiguration getRelayConfiguration(GetRelayConfigurationOptions options) {
        return client.getRelayConfiguration(options).block();
    }

    /**
     * Gets a Relay Configuration with response.
     *
     * @param options of the GetRelayConfigurationOptions request
     * @param context A {@link Context} representing the request context.
     * @return The obtained Communication Relay Configuration
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRelayConfiguration> getRelayConfigurationWithResponse(GetRelayConfigurationOptions options, Context context) {
        Response<CommunicationRelayConfiguration> response =
            client.getRelayConfigurationWithResponse(options, context).block();

        return response;
    }
}
