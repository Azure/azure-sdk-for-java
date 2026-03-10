// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Deprecated generated code

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.resourcemanager.appservice.fluent.AppServiceCertificateOrdersClient;
import com.azure.resourcemanager.appservice.fluent.CertificateOrdersDiagnosticsClient;
import com.azure.resourcemanager.appservice.fluent.CertificateRegistrationManagementClient;
import com.azure.resourcemanager.appservice.fluent.CertificateRegistrationProvidersClient;
import com.azure.resourcemanager.resources.fluentcore.AzureServiceClient;
import java.time.Duration;

/**
 * Initializes a new instance of the CertificateRegistrationManagementClientImpl type.
 */
@ServiceClient(builder = CertificateRegistrationManagementClientBuilder.class)
public final class CertificateRegistrationManagementClientImpl extends AzureServiceClient
    implements CertificateRegistrationManagementClient {
    /**
     * The ID of the target subscription.
     */
    private final String subscriptionId;

    /**
     * Gets The ID of the target subscription.
     * 
     * @return the subscriptionId value.
     */
    public String getSubscriptionId() {
        return this.subscriptionId;
    }

    /**
     * server parameter.
     */
    private final String endpoint;

    /**
     * Gets server parameter.
     * 
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Api Version.
     */
    private final String apiVersion;

    /**
     * Gets Api Version.
     * 
     * @return the apiVersion value.
     */
    public String getApiVersion() {
        return this.apiVersion;
    }

    /**
     * The HTTP pipeline to send requests through.
     */
    private final HttpPipeline httpPipeline;

    /**
     * Gets The HTTP pipeline to send requests through.
     * 
     * @return the httpPipeline value.
     */
    public HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /**
     * The serializer to serialize an object into a string.
     */
    private final SerializerAdapter serializerAdapter;

    /**
     * Gets The serializer to serialize an object into a string.
     * 
     * @return the serializerAdapter value.
     */
    SerializerAdapter getSerializerAdapter() {
        return this.serializerAdapter;
    }

    /**
     * The default poll interval for long-running operation.
     */
    private final Duration defaultPollInterval;

    /**
     * Gets The default poll interval for long-running operation.
     * 
     * @return the defaultPollInterval value.
     */
    public Duration getDefaultPollInterval() {
        return this.defaultPollInterval;
    }

    /**
     * The CertificateRegistrationProvidersClient object to access its operations.
     */
    private final CertificateRegistrationProvidersClient certificateRegistrationProviders;

    /**
     * Gets the CertificateRegistrationProvidersClient object to access its operations.
     * 
     * @return the CertificateRegistrationProvidersClient object.
     */
    public CertificateRegistrationProvidersClient getCertificateRegistrationProviders() {
        return this.certificateRegistrationProviders;
    }

    /**
     * The AppServiceCertificateOrdersClient object to access its operations.
     */
    private final AppServiceCertificateOrdersClient appServiceCertificateOrders;

    /**
     * Gets the AppServiceCertificateOrdersClient object to access its operations.
     * 
     * @return the AppServiceCertificateOrdersClient object.
     */
    public AppServiceCertificateOrdersClient getAppServiceCertificateOrders() {
        return this.appServiceCertificateOrders;
    }

    /**
     * The CertificateOrdersDiagnosticsClient object to access its operations.
     */
    private final CertificateOrdersDiagnosticsClient certificateOrdersDiagnostics;

    /**
     * Gets the CertificateOrdersDiagnosticsClient object to access its operations.
     * 
     * @return the CertificateOrdersDiagnosticsClient object.
     */
    public CertificateOrdersDiagnosticsClient getCertificateOrdersDiagnostics() {
        return this.certificateOrdersDiagnostics;
    }

    /**
     * Initializes an instance of CertificateRegistrationManagementClient client.
     * 
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param serializerAdapter The serializer to serialize an object into a string.
     * @param defaultPollInterval The default poll interval for long-running operation.
     * @param environment The Azure environment.
     * @param subscriptionId The ID of the target subscription.
     * @param endpoint server parameter.
     */
    CertificateRegistrationManagementClientImpl(HttpPipeline httpPipeline, SerializerAdapter serializerAdapter,
        Duration defaultPollInterval, AzureEnvironment environment, String subscriptionId, String endpoint) {
        super(httpPipeline, serializerAdapter, environment);
        this.httpPipeline = httpPipeline;
        this.serializerAdapter = serializerAdapter;
        this.defaultPollInterval = defaultPollInterval;
        this.subscriptionId = subscriptionId;
        this.endpoint = endpoint;
        this.apiVersion = "2024-11-01";
        this.certificateRegistrationProviders = new CertificateRegistrationProvidersClientImpl(this);
        this.appServiceCertificateOrders = new AppServiceCertificateOrdersClientImpl(this);
        this.certificateOrdersDiagnostics = new CertificateOrdersDiagnosticsClientImpl(this);
    }
}
