// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Deprecated generated code.

package com.azure.resourcemanager.monitor.fluent;

import com.azure.core.http.HttpPipeline;
import java.time.Duration;

/**
 * The interface for AlertRuleManagementClient class.
 */
public interface AlertRuleManagementClient {
    /**
     * Gets The ID of the target subscription.
     * 
     * @return the subscriptionId value.
     */
    String getSubscriptionId();

    /**
     * Gets server parameter.
     * 
     * @return the endpoint value.
     */
    String getEndpoint();

    /**
     * Gets The HTTP pipeline to send requests through.
     * 
     * @return the httpPipeline value.
     */
    HttpPipeline getHttpPipeline();

    /**
     * Gets The default poll interval for long-running operation.
     * 
     * @return the defaultPollInterval value.
     */
    Duration getDefaultPollInterval();

    /**
     * Gets the AlertRulesClient object to access its operations.
     * 
     * @return the AlertRulesClient object.
     */
    AlertRulesClient getAlertRules();

    /**
     * Gets the DiagnosticSettingsOperationsClient object to access its operations.
     * 
     * @return the DiagnosticSettingsOperationsClient object.
     */
    DiagnosticSettingsOperationsClient getDiagnosticSettingsOperations();

    /**
     * Gets the DiagnosticSettingsCategoriesClient object to access its operations.
     * 
     * @return the DiagnosticSettingsCategoriesClient object.
     */
    DiagnosticSettingsCategoriesClient getDiagnosticSettingsCategories();
}
