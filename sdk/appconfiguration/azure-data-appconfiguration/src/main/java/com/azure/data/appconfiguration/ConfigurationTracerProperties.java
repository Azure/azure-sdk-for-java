// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.tracing.TracerSpanAttributes;
import com.azure.core.util.tracing.TracerProperties;

/**
 * The tracing properties of Azure App Configuration supported by this client library.
 */
public class ConfigurationTracerProperties implements TracerProperties {

    // Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for a list of supported Resource provider namespaces.
    @Override
    public TracerSpanAttributes getTracerSpanAttributes() {
        return new TracerSpanAttributes("Microsoft.AppConfiguration");
    }

}
