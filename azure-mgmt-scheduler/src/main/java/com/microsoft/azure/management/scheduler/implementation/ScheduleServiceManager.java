/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.scheduler.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.management.scheduler.JobCollections;
import com.microsoft.azure.management.scheduler.Jobs;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;

/**
 * Entry point to Azure Scheduler service management.
 */
@Beta(Beta.SinceVersion.V1_2_0)
public final class ScheduleServiceManager extends Manager<ScheduleServiceManager, SchedulerManagementClientImpl> {

    // Collections
    private JobCollections jobCollections;
    private Jobs jobs;

    /**
     * Get a Configurable instance that can be used to create ScheduleServiceManager with optional configuration.
     *
     * @return Configurable
     */
    public static Configurable configure() {
        return new ScheduleServiceManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of ScheduleServiceManager that exposes Scheduler resource management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription
     * @return the ScheduleServiceManager
     */
    public static ScheduleServiceManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new ScheduleServiceManager(new RestClient.Builder()
            .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
            .withCredentials(credentials)
            .withSerializerAdapter(new AzureJacksonAdapter())
            .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
            .withInterceptor(new ProviderRegistrationInterceptor(credentials))
            .build(), subscriptionId);
    }

    /**
     * Creates an instance of ScheduleServiceManager that exposes Scheduler resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription
     * @return the ScheduleServiceManager
     */
    public static ScheduleServiceManager authenticate(RestClient restClient, String subscriptionId) {
        return new ScheduleServiceManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of ScheduleServiceManager that exposes Scheduler resource management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription
         * @return the ScheduleServiceManager
         */
        ScheduleServiceManager authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements  Configurable {
        @Override
        public ScheduleServiceManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return ScheduleServiceManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private ScheduleServiceManager(RestClient restClient, String subscriptionId) {
        super(
            restClient,
            subscriptionId,
            new SchedulerManagementClientImpl(restClient).withSubscriptionId(subscriptionId));
    }

    /**
     * @return the job collection resource management API entry point
     */
    public JobCollections jobCollections() {
        if (jobCollections == null) {
            jobCollections = new JobCollectionsImpl(this);
        }

        return jobCollections;
    }

    /**
     * @return the job resource management API entry point
     */
    public Jobs jobs() {
        if (jobs == null) {
//            jobs = new JobsImpl(this);
        }

        return jobs;
    }
}
