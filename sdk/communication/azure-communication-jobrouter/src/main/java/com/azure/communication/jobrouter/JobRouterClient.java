package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

/**
 * Sync Client that supports chat operations.
 *
 * <p><strong>Instantiating a synchronous JobRouter Client</strong></p>
 *
 * <!-- src_embed com.azure.communication.jobrouter.jobrouterclient.instantiation -->
 * <pre>
 *
 * &#47;&#47; Initialize the job router client builder
 * final JobRouterClientBuilder builder = new JobRouterClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;credential&#41;;
 *
 * &#47;&#47; Build the job router client
 * JobRouterClient chatClient = builder.buildClient&#40;&#41;;
 *
 * </pre>
 * <!-- end com.azure.communication.chat.chatclient.instantiation -->
 *
 * <p>View {@link JobRouterClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see JobRouterClientBuilder
 */
@ServiceClient(builder = JobRouterClientBuilder.class, isAsync = false)
public class JobRouterClient {

    private final ClientLogger logger = new ClientLogger(JobRouterClient.class);

    private final JobRouterAsyncClient client;

    /**
     * Creates a JobRouterClient that sends requests to the job router service at {@code serviceEndpoint}. Each
     * service call goes through the {@code pipeline}.
     *
     * @param client The {@link JobRouterAsyncClient} that the client routes its request through.
     */
    JobRouterClient(JobRouterAsyncClient client) {
        this.client = client;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> upsertDistributionPolicy(String id, DistributionPolicy distributionPolicy) {
        return this.upsertDistributionPolicyWithResponse(id, distributionPolicy, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> upsertDistributionPolicyWithResponse(String id, DistributionPolicy distributionPolicy, Context context) {
        return this.client.upsertDistributionPolicyWithResponse(id, distributionPolicy, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDistributionPolicy(String id) {
        return this.deleteDistributionPolicyWithResponse(id, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDistributionPolicyWithResponse(String id, Context context) {
        return this.client.deleteDistributionPolicyWithResponse(id, context).block();
    }
}
