// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.networktraversal;

import com.azure.communication.networktraversal.implementation.CommunicationNetworkTraversalClientImpl;
import com.azure.communication.networktraversal.implementation.CommunicationNetworkTraversalsImpl;
import com.azure.communication.networktraversal.models.CommunicationRelayConfiguration;
import com.azure.communication.networktraversal.models.GetRelayConfigurationOptions;
import com.azure.communication.networktraversal.implementation.models.CommunicationRelayConfigurationRequest;
import com.azure.communication.networktraversal.implementation.models.CommunicationErrorResponseException;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.http.rest.Response;
import reactor.core.publisher.Mono;
import com.azure.core.util.FluxUtil;
import static com.azure.core.util.FluxUtil.withContext;
import com.azure.core.util.Context;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Asynchronous client interface for Azure Communication Network Traversal
 * operations
 *
 * <p><strong>Instantiating an asynchronous CommunicationRelayClient</strong></p>
 *
 * <!-- src_embed readme-sample-createCommunicationNetworkTraversalAsyncClient -->
 * <pre>
 * &#47;&#47; You can find your endpoint and access key from your resource in the Azure Portal
 * String endpoint = &quot;https:&#47;&#47;&lt;RESOURCE_NAME&gt;.communication.azure.com&quot;;
 * AzureKeyCredential keyCredential = new AzureKeyCredential&#40;&quot;&lt;access-key&gt;&quot;&#41;;
 *
 * CommunicationRelayAsyncClient communicationRelayClient = new CommunicationRelayClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;keyCredential&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createCommunicationNetworkTraversalAsyncClient -->
 * <p>View {@link CommunicationRelayClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see CommunicationRelayClientBuilder
 */
@ServiceClient(builder = CommunicationRelayClientBuilder.class, isAsync = true)
public final class CommunicationRelayAsyncClient {

    private final CommunicationNetworkTraversalsImpl client;
    private final ClientLogger logger = new ClientLogger(CommunicationRelayAsyncClient.class);

    CommunicationRelayAsyncClient(CommunicationNetworkTraversalClientImpl communicationNetworkingClient) {
        client = communicationNetworkingClient.getCommunicationNetworkTraversals();
    }

    /**
     * Gets a Relay Configuration.
     *
     * <p><strong>Sample code for getting a relay configuration without parameters</strong></p>
     *
     * <!-- src_embed readme-sample-getRelayConfigurationWithoutIdentity -->
     * <pre>
     * CommunicationRelayClient communicationRelayClient = createCommunicationNetworkTraversalClient&#40;&#41;;
     * CommunicationRelayConfiguration config = communicationRelayClient.getRelayConfiguration&#40;&#41;;
     *
     * System.out.println&#40;&quot;Expires on:&quot; + config.getExpiresOn&#40;&#41;&#41;;
     * List&lt;CommunicationIceServer&gt; iceServers = config.getIceServers&#40;&#41;;
     *
     * for &#40;CommunicationIceServer iceS : iceServers&#41; &#123;
     *     System.out.println&#40;&quot;URLS: &quot; + iceS.getUrls&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Username: &quot; + iceS.getUsername&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Credential: &quot; + iceS.getCredential&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;RouteType: &quot; + iceS.getRouteType&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end readme-sample-getRelayConfigurationWithoutIdentity -->
     *
     * @return The obtained Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRelayConfiguration> getRelayConfiguration() {
        GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
        return this.getRelayConfigurationWithResponse(options, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a Relay Configuration for a CommunicationUserIdentifier.
     *
     * <p><strong>Sample code for getting a relay configuration</strong></p>
     *
     * <!-- src_embed readme-sample-getRelayConfiguration -->
     * <pre>
     * CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient&#40;&#41;;
     *
     * CommunicationUserIdentifier user = communicationIdentityClient.createUser&#40;&#41;;
     * System.out.println&#40;&quot;User id: &quot; + user.getId&#40;&#41;&#41;;
     *
     * GetRelayConfigurationOptions options = new GetRelayConfigurationOptions&#40;&#41;;
     * options.setCommunicationUserIdentifier&#40;user&#41;;
     *
     * CommunicationRelayClient communicationRelayClient = createCommunicationNetworkTraversalClient&#40;&#41;;
     * CommunicationRelayConfiguration config = communicationRelayClient.getRelayConfiguration&#40;options&#41;;
     *
     * System.out.println&#40;&quot;Expires on:&quot; + config.getExpiresOn&#40;&#41;&#41;;
     * List&lt;CommunicationIceServer&gt; iceServers = config.getIceServers&#40;&#41;;
     *
     * for &#40;CommunicationIceServer iceS : iceServers&#41; &#123;
     *     System.out.println&#40;&quot;URLS: &quot; + iceS.getUrls&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Username: &quot; + iceS.getUsername&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Credential: &quot; + iceS.getCredential&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;RouteType: &quot; + iceS.getRouteType&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end readme-sample-getRelayConfiguration -->
     *
     * @param options of the GetRelayConfigurationOptions request
     * @return The obtained Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRelayConfiguration> getRelayConfiguration(GetRelayConfigurationOptions options) {
        return this.getRelayConfigurationWithResponse(options, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a Relay Configuration for a CommunicationUserIdentifier given options with response.
     *
     * @param options of the GetRelayConfigurationOptions request
     * @return The obtained Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRelayConfiguration>> getRelayConfigurationWithResponse(GetRelayConfigurationOptions options) {
        return withContext(context -> getRelayConfigurationWithResponse(options, null));
    }

    /**
     * Gets a Relay Configuration for a CommunicationUserIdentifier given a RouteType with response.
     *
     * @param options of the GetRelayConfigurationOptions request
     * @param context A {@link Context} representing the request context.
     * @return The obtained Communication Relay Configuration.
     */
    Mono<Response<CommunicationRelayConfiguration>> getRelayConfigurationWithResponse(GetRelayConfigurationOptions options, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            CommunicationRelayConfigurationRequest body = new CommunicationRelayConfigurationRequest();
            if (options != null) {
                if (options.getCommunicationUserIdentifier() != null) {
                    body.setId(options.getCommunicationUserIdentifier().getId());
                }

                if (options.getRouteType() != null) {
                    body.setRouteType(options.getRouteType());
                }

                if (options.getTtl() != null) {
                    body.setTtl(options.getTtl());
                }
            }
            return client.issueRelayConfigurationWithResponseAsync(body, context)
                .onErrorMap(CommunicationErrorResponseException.class, e -> e);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
