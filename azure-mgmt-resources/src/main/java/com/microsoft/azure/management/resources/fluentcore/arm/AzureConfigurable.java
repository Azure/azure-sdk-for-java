/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.resources.fluentcore.arm;

import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;

public interface AzureConfigurable<T extends AzureConfigurable<T>> {
    T withLogLevel(HttpLoggingInterceptor.Level level);
    T withInterceptor(Interceptor interceptor);
    T withUserAgent(String userAgent);
}
