package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.implementation.AzureCommunicationRoutingServiceImpl;
import com.azure.communication.jobrouter.implementation.JobRouterAdministrationsImpl;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;

@ServiceClient(builder = RouterAdministrationClientBuilder.class, isAsync = true)
public class RouterAdministrationAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(RouterAsyncClient.class);

    private final JobRouterAdministrationsImpl jobRouterAdmin;

    RouterAdministrationAsyncClient(AzureCommunicationRoutingServiceImpl jobRouterService) {
        this.jobRouterAdmin = jobRouterService.getJobRouterAdministrations();
    }
}
