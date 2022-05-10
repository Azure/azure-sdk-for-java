package com.azure.communication.jobrouter;

import com.azure.core.annotation.ServiceClient;
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
    public JobRouterClient(JobRouterAsyncClient client) {

        this.client = client;
    }
}
