// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.util.Context;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * App Configuration Utility methods, use internally.
 */
public class Utility {
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";
    public static final String APP_CONFIG_TRACING_NAMESPACE_VALUE = "Microsoft.AppConfiguration";

    public static final String ID = "id";
    public static final String DESCRIPTION = "description";
    public static final String DISPLAY_NAME = "display_name";
    public static final String ENABLED = "enabled";
    public static final String CONDITIONS = "conditions";
    public static final String CLIENT_FILTERS = "client_filters";
    public static final String NAME = "name";
    public static final String PARAMETERS = "parameters";
    public static final String URI = "uri";

    /**
     * Represents any value in Etag.
     */
    public static final String ETAG_ANY = "*";


    /**
     * Enable the sync stack rest proxy.
     *
     * @param context It offers a means of passing arbitrary data (key-value pairs) to pipeline policies.
     * Most applications do not need to pass arbitrary data to the pipeline and can pass Context.NONE or null.
     *
     * @return The Context.
     */
    public static Context enableSyncRestProxy(Context context) {
        context = context == null ? Context.NONE : context;
        return context.addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    }

    public static Context addTracingNamespace(Context context) {
        context = context == null ? Context.NONE : context;
        return context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE);
    }
}
