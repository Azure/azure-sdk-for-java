// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.implementation;

public class Constants {
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CLOUD_EVENT_CONTENT_TYPE = "application/cloudevents-batch+json; charset=utf-8";
    public static final String TRACE_PARENT = "traceparent";
    public static final String TRACE_STATE = "tracestate";
    public static final String TRACE_PARENT_PLACEHOLDER_UUID = "TP-14b6b15b-74b6-4178-847e-d142aa2727b2";
    public static final String TRACE_STATE_PLACEHOLDER_UUID = "TS-14b6b15b-74b6-4178-847e-d142aa2727b2";
    public static final String TRACE_PARENT_PLACEHOLDER = ",\"" + TRACE_PARENT + "\":\"TP-14b6b15b-74b6-4178-847e-d142aa2727b2\"";
    public static final String TRACE_STATE_PLACEHOLDER = ",\"" + TRACE_STATE + "\":\"TS-14b6b15b-74b6-4178-847e-d142aa2727b2\"";

    // Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    public static final String EVENT_GRID_TRACING_NAMESPACE_VALUE = "Microsoft.EventGrid";
}
