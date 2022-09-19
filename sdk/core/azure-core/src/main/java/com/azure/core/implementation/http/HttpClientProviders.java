// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.logging.ClientLogger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * This class handles loading available HTTP clients
 */
public final class HttpClientProviders {
    private static final String NO_DEFAULT_PROVIDER = "A request was made to load the default HttpClient provider "
        + "but one could not be found on the classpath. If you are using a dependency manager, consider including a "
        + "dependency on azure-core-http-netty or azure-core-http-okhttp. Depending on your existing dependencies, you "
        + "have the choice of Netty or OkHttp implementations. Additionally, refer to "
        + "https://aka.ms/azsdk/java/docs/custom-httpclient to learn about writing your own implementation.";

    private static final String CANNOT_FIND_SPECIFIC_PROVIDER = "A request was made to use a specific "
        + "HttpClientProvider to create an instance of HttpClient but it wasn't found on the classpath. If you're "
        + "using a dependency manager ensure you're including the dependency that provides the specific "
        + "implementation. If you're including the specific implementation ensure that the HttpClientProvider service "
        + "it supplies is being included in the 'META-INF/services' file 'com'azure.core.http.HttpClientProvider'. "
        + "The requested HttpClientProvider was: ";


    private static final ClientLogger LOGGER = new ClientLogger(HttpClientProviders.class);

    private static final HttpClientProvider DEFAULT_PROVIDER;
    private static final Map<String, HttpClientProvider> AVAILABLE_PROVIDERS;

    static {
        // Use as classloader to load provider-configuration files and provider classes the classloader
        // that loaded this class. In most cases this will be the System classloader.
        // But this choice here provides additional flexibility in managed environments that control
        // classloading differently (OSGi, Spring and others) and don't/ depend on the
        // System classloader to load HttpClientProvider classes.
        ServiceLoader<HttpClientProvider> serviceLoader = ServiceLoader.load(HttpClientProvider.class,
            HttpClientProviders.class.getClassLoader());

        AVAILABLE_PROVIDERS = new HashMap<>();
        // Use the first provider found in the service loader iterator.
        Iterator<HttpClientProvider> it = serviceLoader.iterator();
        if (it.hasNext()) {
            DEFAULT_PROVIDER = it.next();
            String defaultProviderName = DEFAULT_PROVIDER.getClass().getName();
            AVAILABLE_PROVIDERS.put(defaultProviderName, DEFAULT_PROVIDER);
            LOGGER.verbose("Using {} as the default HttpClientProvider.", defaultProviderName);
        } else {
            DEFAULT_PROVIDER = null;
        }

        while (it.hasNext()) {
            HttpClientProvider additionalProvider = it.next();
            String additionalProviderName = additionalProvider.getClass().getName();
            AVAILABLE_PROVIDERS.put(additionalProviderName, additionalProvider);
            LOGGER.verbose("Additional provider found on the classpath: {}", additionalProviderName);
        }
    }

    private HttpClientProviders() {
        // no-op
    }

    public static HttpClient createInstance() {
        return createInstance(null);
    }

    public static HttpClient createInstance(ClientOptions clientOptions) {
        if (DEFAULT_PROVIDER == null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(NO_DEFAULT_PROVIDER));
        }

        if (clientOptions instanceof HttpClientOptions) {
            HttpClientOptions httpClientOptions = (HttpClientOptions) clientOptions;
            String selectedImplementation = httpClientOptions.getHttpClientImplementation();
            if (CoreUtils.isNullOrEmpty(selectedImplementation)) {
                return DEFAULT_PROVIDER.createInstance(httpClientOptions);
            } else {
                HttpClientProvider provider = AVAILABLE_PROVIDERS.get(selectedImplementation);
                if (provider == null) {
                    throw LOGGER.logExceptionAsError(
                        new IllegalStateException(CANNOT_FIND_SPECIFIC_PROVIDER + selectedImplementation));
                } else {
                    return provider.createInstance(httpClientOptions);
                }
            }
        }

        return DEFAULT_PROVIDER.createInstance();
    }
}
