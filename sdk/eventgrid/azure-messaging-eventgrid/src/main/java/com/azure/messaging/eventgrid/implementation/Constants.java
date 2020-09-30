package com.azure.messaging.eventgrid.implementation;

public class Constants {
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CLOUD_EVENT_CONTENT_TYPE = "application/cloudevents-batch+json; charset=utf-8";
    public static final String TRACE_PARENT = "traceparent";
    public static final String TRACE_STATE = "tracestate";
    public static final String TRACE_PARENT_PLACEHOLDER = "TP-14b6b15b-74b6-4178-847e-d142aa2727b2";
    public static final String TRACE_STATE_PLACEHOLDER = "TS-14b6b15b-74b6-4178-847e-d142aa2727b2";
    public static final String TRACE_PARENT_REPLACE = ",\"" + TRACE_PARENT + "\":\"TP-14b6b15b-74b6-4178-847e-d142aa2727b2\"";
    public static final String TRACE_STATE_REPLACE = ",\"" + TRACE_STATE + "\":\"TS-14b6b15b-74b6-4178-847e-d142aa2727b2\"";
    public static final String EVENT_GRID_TRACING_NAMESPACE_VALUE = "Microsoft.EventGrid";
}
