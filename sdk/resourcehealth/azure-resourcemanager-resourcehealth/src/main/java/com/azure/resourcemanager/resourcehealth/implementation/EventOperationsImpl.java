// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.resourcehealth.implementation;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resourcehealth.fluent.EventOperationsClient;
import com.azure.resourcemanager.resourcehealth.fluent.models.EventInner;
import com.azure.resourcemanager.resourcehealth.models.Event;
import com.azure.resourcemanager.resourcehealth.models.EventOperations;

public final class EventOperationsImpl implements EventOperations {
    private static final ClientLogger LOGGER = new ClientLogger(EventOperationsImpl.class);

    private final EventOperationsClient innerClient;

    private final com.azure.resourcemanager.resourcehealth.ResourceHealthManager serviceManager;

    public EventOperationsImpl(EventOperationsClient innerClient,
        com.azure.resourcemanager.resourcehealth.ResourceHealthManager serviceManager) {
        this.innerClient = innerClient;
        this.serviceManager = serviceManager;
    }

    public Response<Event> getBySubscriptionIdAndTrackingIdWithResponse(String eventTrackingId, String filter,
        String queryStartTime, Context context) {
        Response<EventInner> inner = this.serviceClient()
            .getBySubscriptionIdAndTrackingIdWithResponse(eventTrackingId, filter, queryStartTime, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new EventImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public Event getBySubscriptionIdAndTrackingId(String eventTrackingId) {
        EventInner inner = this.serviceClient().getBySubscriptionIdAndTrackingId(eventTrackingId);
        if (inner != null) {
            return new EventImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public Response<Event> fetchDetailsBySubscriptionIdAndTrackingIdWithResponse(String eventTrackingId,
        Context context) {
        Response<EventInner> inner
            = this.serviceClient().fetchDetailsBySubscriptionIdAndTrackingIdWithResponse(eventTrackingId, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new EventImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public Event fetchDetailsBySubscriptionIdAndTrackingId(String eventTrackingId) {
        EventInner inner = this.serviceClient().fetchDetailsBySubscriptionIdAndTrackingId(eventTrackingId);
        if (inner != null) {
            return new EventImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public Response<Event> getByTenantIdAndTrackingIdWithResponse(String eventTrackingId, String filter,
        String queryStartTime, Context context) {
        Response<EventInner> inner = this.serviceClient()
            .getByTenantIdAndTrackingIdWithResponse(eventTrackingId, filter, queryStartTime, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new EventImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public Event getByTenantIdAndTrackingId(String eventTrackingId) {
        EventInner inner = this.serviceClient().getByTenantIdAndTrackingId(eventTrackingId);
        if (inner != null) {
            return new EventImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public Response<Event> fetchDetailsByTenantIdAndTrackingIdWithResponse(String eventTrackingId, Context context) {
        Response<EventInner> inner
            = this.serviceClient().fetchDetailsByTenantIdAndTrackingIdWithResponse(eventTrackingId, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new EventImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public Event fetchDetailsByTenantIdAndTrackingId(String eventTrackingId) {
        EventInner inner = this.serviceClient().fetchDetailsByTenantIdAndTrackingId(eventTrackingId);
        if (inner != null) {
            return new EventImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    private EventOperationsClient serviceClient() {
        return this.innerClient;
    }

    private com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager() {
        return this.serviceManager;
    }
}
