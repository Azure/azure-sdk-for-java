// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datadog.models;

import com.azure.resourcemanager.datadog.fluent.models.DatadogHostInner;
import java.util.List;

/**
 * An immutable client-side representation of DatadogHost.
 */
public interface DatadogHost {
    /**
     * Gets the name property: The name of the host.
     * 
     * @return the name value.
     */
    String name();

    /**
     * Gets the aliases property: The aliases for the host installed via the Datadog agent.
     * 
     * @return the aliases value.
     */
    List<String> aliases();

    /**
     * Gets the apps property: The Datadog integrations reporting metrics for the host.
     * 
     * @return the apps value.
     */
    List<String> apps();

    /**
     * Gets the meta property: The meta property.
     * 
     * @return the meta value.
     */
    DatadogHostMetadata meta();

    /**
     * Gets the inner com.azure.resourcemanager.datadog.fluent.models.DatadogHostInner object.
     * 
     * @return the inner object.
     */
    DatadogHostInner innerModel();
}
