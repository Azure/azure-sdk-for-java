// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Deprecated generated code.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.resourcemanager.monitor.fluent.AlertRuleManagementClient;
import com.azure.resourcemanager.monitor.fluent.AlertRulesClient;
import com.azure.resourcemanager.monitor.fluent.DiagnosticSettingsCategoriesClient;
import com.azure.resourcemanager.monitor.fluent.DiagnosticSettingsOperationsClient;
import com.azure.resourcemanager.resources.fluentcore.AzureServiceClient;
import java.time.Duration;

/**
 * Initializes a new instance of the AlertRuleManagementClientImpl type.
 */
@ServiceClient(builder = AlertRuleManagementClientBuilder.class)
public final class AlertRuleManagementClientImpl extends AzureServiceClient implements AlertRuleManagementClient {
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
     * The AlertRulesClient object to access its operations.
     */
    private final AlertRulesClient alertRules;

    /**
     * Gets the AlertRulesClient object to access its operations.
     * 
     * @return the AlertRulesClient object.
     */
    public AlertRulesClient getAlertRules() {
        return this.alertRules;
    }

    /**
     * The DiagnosticSettingsOperationsClient object to access its operations.
     */
    private final DiagnosticSettingsOperationsClient diagnosticSettingsOperations;

    /**
     * Gets the DiagnosticSettingsOperationsClient object to access its operations.
     * 
     * @return the DiagnosticSettingsOperationsClient object.
     */
    public DiagnosticSettingsOperationsClient getDiagnosticSettingsOperations() {
        return this.diagnosticSettingsOperations;
    }

    /**
     * The DiagnosticSettingsCategoriesClient object to access its operations.
     */
    private final DiagnosticSettingsCategoriesClient diagnosticSettingsCategories;

    /**
     * Gets the DiagnosticSettingsCategoriesClient object to access its operations.
     * 
     * @return the DiagnosticSettingsCategoriesClient object.
     */
    public DiagnosticSettingsCategoriesClient getDiagnosticSettingsCategories() {
        return this.diagnosticSettingsCategories;
    }

    /**
     * Initializes an instance of AlertRuleManagementClient client.
     * 
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param serializerAdapter The serializer to serialize an object into a string.
     * @param defaultPollInterval The default poll interval for long-running operation.
     * @param environment The Azure environment.
     * @param subscriptionId The ID of the target subscription.
     * @param endpoint server parameter.
     */
    AlertRuleManagementClientImpl(HttpPipeline httpPipeline, SerializerAdapter serializerAdapter,
        Duration defaultPollInterval, AzureEnvironment environment, String subscriptionId, String endpoint) {
        super(httpPipeline, serializerAdapter, environment);
        this.httpPipeline = httpPipeline;
        this.serializerAdapter = serializerAdapter;
        this.defaultPollInterval = defaultPollInterval;
        this.subscriptionId = subscriptionId;
        this.endpoint = endpoint;
        this.alertRules = new AlertRulesClientImpl(this);
        this.diagnosticSettingsOperations = new DiagnosticSettingsOperationsClientImpl(this);
        this.diagnosticSettingsCategories = new DiagnosticSettingsCategoriesClientImpl(this);
    }
}
