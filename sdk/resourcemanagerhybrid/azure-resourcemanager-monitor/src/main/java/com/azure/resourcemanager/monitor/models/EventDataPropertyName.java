// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for EventDataPropertyName. */
public final class EventDataPropertyName extends ExpandableStringEnum<EventDataPropertyName> {
    /** Static value "authorization" for EventDataPropertyName. */
    public static final EventDataPropertyName AUTHORIZATION = fromString("authorization");

    /** Static value "claims" for EventDataPropertyName. */
    public static final EventDataPropertyName CLAIMS = fromString("claims");

    /** Static value "correlationId" for EventDataPropertyName. */
    public static final EventDataPropertyName CORRELATIONID = fromString("correlationId");

    /** Static value "description" for EventDataPropertyName. */
    public static final EventDataPropertyName DESCRIPTION = fromString("description");

    /** Static value "eventDataId" for EventDataPropertyName. */
    public static final EventDataPropertyName EVENTDATAID = fromString("eventDataId");

    /** Static value "eventName" for EventDataPropertyName. */
    public static final EventDataPropertyName EVENTNAME = fromString("eventName");

    /** Static value "eventTimestamp" for EventDataPropertyName. */
    public static final EventDataPropertyName EVENTTIMESTAMP = fromString("eventTimestamp");

    /** Static value "httpRequest" for EventDataPropertyName. */
    public static final EventDataPropertyName HTTPREQUEST = fromString("httpRequest");

    /** Static value "level" for EventDataPropertyName. */
    public static final EventDataPropertyName LEVEL = fromString("level");

    /** Static value "operationId" for EventDataPropertyName. */
    public static final EventDataPropertyName OPERATIONID = fromString("operationId");

    /** Static value "operationName" for EventDataPropertyName. */
    public static final EventDataPropertyName OPERATIONNAME = fromString("operationName");

    /** Static value "properties" for EventDataPropertyName. */
    public static final EventDataPropertyName PROPERTIES = fromString("properties");

    /** Static value "resourceGroupName" for EventDataPropertyName. */
    public static final EventDataPropertyName RESOURCEGROUPNAME = fromString("resourceGroupName");

    /** Static value "resourceProviderName" for EventDataPropertyName. */
    public static final EventDataPropertyName RESOURCEPROVIDERNAME = fromString("resourceProviderName");

    /** Static value "resourceId" for EventDataPropertyName. */
    public static final EventDataPropertyName RESOURCEID = fromString("resourceId");

    /** Static value "status" for EventDataPropertyName. */
    public static final EventDataPropertyName STATUS = fromString("status");

    /** Static value "submissionTimestamp" for EventDataPropertyName. */
    public static final EventDataPropertyName SUBMISSIONTIMESTAMP = fromString("submissionTimestamp");

    /** Static value "subStatus" for EventDataPropertyName. */
    public static final EventDataPropertyName SUBSTATUS = fromString("subStatus");

    /** Static value "subscriptionId" for EventDataPropertyName. */
    public static final EventDataPropertyName SUBSCRIPTIONID = fromString("subscriptionId");

    /**
     * Creates or finds a EventDataPropertyName from its string representation.
     *
     * @param name a name to look for
     * @return the corresponding WebhookAction
     */
    @JsonCreator
    public static EventDataPropertyName fromString(String name) {
        return fromString(name, EventDataPropertyName.class);
    }

    /** @return known WebhookAction values */
    public static Collection<EventDataPropertyName> values() {
        return values(EventDataPropertyName.class);
    }
}
