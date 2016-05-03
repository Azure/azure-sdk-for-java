package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigureBase;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class ComputeManager {
    private final RestClient restClient;

    public static Configure configure() {
        return new AzureConfigureImpl();
    }

    public static ComputeManager authenticate(ServiceClientCredentials credentials) {
        return new ComputeManager(credentials);
    }

    public static ComputeManager authenticate(RestClient restClient) {
        return new ComputeManager(restClient);
    }

    public interface Configure extends AzureConfigureBase<Configure> {
        ComputeManager authenticate(ServiceClientCredentials credentials);
    }

    public interface Subscription {
        AvailabilitySets availabilitySets();
        VirtualMachines virtualMachines();
    }

    private ComputeManager(ServiceClientCredentials credentials) {
        this.restClient = new RestClient
                .Builder("https://management.azure.com")
                .withCredentials(credentials)
                .build();
    }

    private ComputeManager(RestClient restClient) {
        this.restClient = restClient;
    }

    public ComputeManager.Subscription withSubscription(String subscriptionId) {
        return new AzureSubscriptionImpl(restClient, subscriptionId);
    }
}
