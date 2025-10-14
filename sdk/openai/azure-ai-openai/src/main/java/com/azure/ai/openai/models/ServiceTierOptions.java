// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Specifies the latency tier to use for processing the request.
 * This parameter is relevant for customers subscribed to the scale tier service:
 *  - If set to 'auto', and the Project is Scale tier enabled, the system will utilize scale tier credits until they are exhausted.
 *  - If set to 'auto', and the Project is not Scale tier enabled, the request will be processed using the default service tier with a lower uptime SLA and no latency guaranty.
 *  - If set to 'default', the request will be processed using the default service tier with a lower uptime SLA and no latency guaranty.
 *  - When not set, the default behavior is 'auto'.
 * When this parameter is set, the response body will include the `service_tier` utilized.
 */
public final class ServiceTierOptions extends ExpandableStringEnum<ServiceTierOptions> {

    /**
     * Service tier option for auto.
     */
    public static final ServiceTierOptions AUTO = fromString("auto");

    /**
     * Service tier option for default.
     */
    public static final ServiceTierOptions DEFAULT = fromString("default");

    /**
     * Creates a new instance of ServiceTierOptions value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public ServiceTierOptions() {
    }

    /**
     * Creates or finds a ServiceTierOptions from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ServiceTierOptions.
     */
    public static ServiceTierOptions fromString(String name) {
        return fromString(name, ServiceTierOptions.class);
    }

    /**
     * Get known ServiceTierOptions values.
     *
     * @return a collection of known ServiceTierOptions values.
     */
    public static Collection<ServiceTierOptions> values() {
        return values(ServiceTierOptions.class);
    }
}
