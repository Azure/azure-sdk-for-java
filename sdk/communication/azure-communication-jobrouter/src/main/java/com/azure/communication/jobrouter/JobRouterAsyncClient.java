package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.implementation.AzureCommunicationRoutingServiceImpl;
import com.azure.communication.jobrouter.implementation.JobRoutersImpl;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.JobQueue;
import com.azure.communication.jobrouter.models.PagedDistributionPolicy;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.*;

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

    private JobRoutersImpl jobRouter;

    JobRouterAsyncClient(AzureCommunicationRoutingServiceImpl jobRouterService) {
        this.jobRouter = jobRouterService.getJobRouters();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DistributionPolicy>> upsertDistributionPolicyWithResponse(String id, DistributionPolicy distributionPolicy) {
        try {
            return withContext(context -> upsertDistributionPolicyWithResponse(id, distributionPolicy, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DistributionPolicy>> upsertDistributionPolicyWithResponse(String id, DistributionPolicy distributionPolicy, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return jobRouter.upsertDistributionPolicyWithResponseAsync(id, distributionPolicy, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDistributionPolicyWithResponse(String id) {
        try {
            return withContext(context -> deleteDistributionPolicyWithResponse(id, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteDistributionPolicyWithResponse(String id, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return jobRouter.deleteDistributionPolicyWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedDistributionPolicy> listDistributionPolicies(Integer maxPageSize) {
        try {
            return jobRouter.listDistributionPoliciesAsync(maxPageSize);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<JobQueue>> upsertQueueWithResponse(String id, JobQueue jobQueue) {
        try {
            return withContext(context -> upsertQueueWithResponse(id, jobQueue, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<JobQueue>> upsertQueueWithResponse(String id, JobQueue jobQueue, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return jobRouter.upsertQueueWithResponseAsync(id, jobQueue, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
