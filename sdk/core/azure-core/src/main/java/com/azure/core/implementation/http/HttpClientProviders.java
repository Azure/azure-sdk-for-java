// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.HttpClientOptions;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * This class handles loading available HTTP clients
 */
public final class HttpClientProviders {
    private static final String CANNOT_FIND_HTTP_CLIENT = "A request was made to load the default HttpClient provider "
        + "but one could not be found on the classpath. If you are using a dependency manager, consider including a "
        + "dependency on azure-core-http-netty or azure-core-http-okhttp. Depending on your existing dependencies, you "
        + "have the choice of Netty or OkHttp implementations. Additionally, refer to "
        + "https://aka.ms/azsdk/java/docs/custom-httpclient to learn about writing your own implementation.";

    private static HttpClientProvider defaultProvider;

    static {
        // Use as classloader to load provider-configuration files and provider classes the classloader
        // that loaded this class. In most cases this will be the System classloader.
        // But this choice here provides additional flexibility in managed environments that control
        // classloading differently (OSGi, Spring and others) and don't/ depend on the
        // System classloader to load HttpClientProvider classes.
        ServiceLoader<HttpClientProvider> serviceLoader = ServiceLoader.load(
            HttpClientProvider.class,
            HttpClientProviders.class.getClassLoader());
        // Use the first provider found in the service loader iterator.
        Iterator<HttpClientProvider> it = serviceLoader.iterator();
        if (it.hasNext()) {
            defaultProvider = it.next();
        }
    }

    private HttpClientProviders() {
        // no-op
    }

    public static HttpClient createInstance() {
        return createInstance(null);
    }

    public static HttpClient createInstance(ClientOptions clientOptions) {
        if (defaultProvider == null) {
            throw new IllegalStateException(CANNOT_FIND_HTTP_CLIENT);
        }

        if (clientOptions instanceof HttpClientOptions) {
            return defaultProvider.createInstance((HttpClientOptions) clientOptions);
        }

        return defaultProvider.createInstance();
    }
}
