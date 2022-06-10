// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.resourcemanager.monitor.fluent.models.EventDataInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.time.OffsetDateTime;
import java.util.Map;

/** The Azure event log entries are of type EventData. */
public interface EventData extends HasInnerModel<EventDataInner> {

    /**
     * Get the authorization value.
     *
     * @return the authorization value
     */
    SenderAuthorization authorization();

    /**
     * Get the claims value.
     *
     * @return the claims value
     */
    Map<String, String> claims();

    /**
     * Get the caller value.
     *
     * @return the caller value
     */
    String caller();

    /**
     * Get the description value.
     *
     * @return the description value
     */
    String description();

    /**
     * Get the id value.
     *
     * @return the id value
     */
    String id();

    /**
     * Get the eventDataId value.
     *
     * @return the eventDataId value
     */
    String eventDataId();

    /**
     * Get the correlationId value.
     *
     * @return the correlationId value
     */
    String correlationId();

    /**
     * Get the eventName value.
     *
     * @return the eventName value
     */
    LocalizableString eventName();

    /**
     * Get the category value.
     *
     * @return the category value
     */
    LocalizableString category();

    /**
     * Get the httpRequest value.
     *
     * @return the httpRequest value
     */
    HttpRequestInfo httpRequest();

    /**
     * Get the level value.
     *
     * @return the level value
     */
    EventLevel level();

    /**
     * Get the resourceGroupName value.
     *
     * @return the resourceGroupName value
     */
    String resourceGroupName();

    /**
     * Get the resourceProviderName value.
     *
     * @return the resourceProviderName value
     */
    LocalizableString resourceProviderName();

    /**
     * Get the resourceId value.
     *
     * @return the resourceId value
     */
    String resourceId();

    /**
     * Get the resourceType value.
     *
     * @return the resourceType value
     */
    LocalizableString resourceType();

    /**
     * Get the operationId value.
     *
     * @return the operationId value
     */
    String operationId();

    /**
     * Get the operationName value.
     *
     * @return the operationName value
     */
    LocalizableString operationName();

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    Map<String, String> properties();

    /**
     * Get the status value.
     *
     * @return the status value
     */
    LocalizableString status();

    /**
     * Get the subStatus value.
     *
     * @return the subStatus value
     */
    LocalizableString subStatus();

    /**
     * Get the eventTimestamp value.
     *
     * @return the eventTimestamp value
     */
    OffsetDateTime eventTimestamp();

    /**
     * Get the submissionTimestamp value.
     *
     * @return the submissionTimestamp value
     */
    OffsetDateTime submissionTimestamp();

    /**
     * Get the subscriptionId value.
     *
     * @return the subscriptionId value
     */
    String subscriptionId();

    /**
     * Get the tenantId value.
     *
     * @return the tenantId value
     */
    String tenantId();
}
