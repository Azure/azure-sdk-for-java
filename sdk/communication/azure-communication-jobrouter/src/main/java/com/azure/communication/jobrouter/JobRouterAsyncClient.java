package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.implementation.AzureCommunicationRoutingServiceImpl;
import com.azure.communication.jobrouter.implementation.JobRoutersImpl;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;

/**
 * Async Client that supports chat operations.
 *
 * <p><strong>Instantiating an asynchronous Chat Client</strong></p>
 *
 * <!-- src_embed com.azure.communication.jobrouter.jobrouterasyncclient.instantiation -->
 * <pre>
 *
 * &#47;&#47; Initialize the job router client builder
 * final JobRouterClientBuilder builder = new JobRouterClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;credential&#41;;
 *
 * &#47;&#47; Build the chat client
 * JobRouterAsyncClient jobRouterClient = builder.buildAsyncClient&#40;&#41;;
 *
 * </pre>
 * <!-- end com.azure.communication.chat.chatasyncclient.instantiation -->
 *
 * <p>View {@link JobRouterClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see JobRouterClientBuilder
 */
@ServiceClient(builder = JobRouterClientBuilder.class, isAsync = true)
public class JobRouterAsyncClient {
    private final ClientLogger logger = new ClientLogger(JobRouterAsyncClient.class);

    private final AzureCommunicationRoutingServiceImpl jobRouterServiceClient;
    private final JobRoutersImpl jobRouterClient;

    public JobRouterAsyncClient(AzureCommunicationRoutingServiceImpl jobRouterServiceClient) {
        this.jobRouterServiceClient = jobRouterServiceClient;
        this.jobRouterClient = jobRouterServiceClient.getJobRouters();
    }
}
