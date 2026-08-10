// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Deprecated generated code

package com.azure.resourcemanager.compute.fluent;

import com.azure.core.http.HttpPipeline;
import java.time.Duration;

/**
 * The interface for CloudServiceManagementClient class.
 */
public interface CloudServiceManagementClient {
    /**
     * Gets Subscription credentials which uniquely identify Microsoft Azure subscription. The subscription ID forms
     * part of the URI for every service call.
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
     * Gets Api Version.
     * 
     * @return the apiVersion value.
     */
    String getApiVersion();

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
     * Gets the CloudServiceRoleInstancesClient object to access its operations.
     * 
     * @return the CloudServiceRoleInstancesClient object.
     */
    CloudServiceRoleInstancesClient getCloudServiceRoleInstances();

    /**
     * Gets the CloudServiceRolesClient object to access its operations.
     * 
     * @return the CloudServiceRolesClient object.
     */
    CloudServiceRolesClient getCloudServiceRoles();

    /**
     * Gets the CloudServicesClient object to access its operations.
     * 
     * @return the CloudServicesClient object.
     */
    CloudServicesClient getCloudServices();

    /**
     * Gets the CloudServicesUpdateDomainsClient object to access its operations.
     * 
     * @return the CloudServicesUpdateDomainsClient object.
     */
    CloudServicesUpdateDomainsClient getCloudServicesUpdateDomains();

    /**
     * Gets the CloudServiceOperatingSystemsClient object to access its operations.
     * 
     * @return the CloudServiceOperatingSystemsClient object.
     */
    CloudServiceOperatingSystemsClient getCloudServiceOperatingSystems();
}
