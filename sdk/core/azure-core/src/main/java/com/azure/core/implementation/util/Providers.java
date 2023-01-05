// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_HTTP_CLIENT_IMPLEMENTATION;

public final class Providers<T> {
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
    private static final ClientLogger LOGGER = new ClientLogger(Providers.class);
    private final T defaultProvider;
    private final Map<String, T> availableProviders;

    private final String defaultImplementation;
    private final boolean noDefaultImplementation;


    public Providers(Class<T> clazz) {
        // Use as classloader to load provider-configuration files and provider classes the classloader
        // that loaded this class. In most cases this will be the System classloader.
        // But this choice here provides additional flexibility in managed environments that control
        // classloading differently (OSGi, Spring and others) and don't depend on the
        // System classloader to load HttpClientProvider classes.
        ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz, Providers.class.getClassLoader());

        availableProviders = new HashMap<>();
        // Use the first provider found in the service loader iterator.
        Iterator<T> it = serviceLoader.iterator();
        if (it.hasNext()) {
            defaultProvider = it.next();
            String defaultProviderName = defaultProvider.getClass().getName();
            availableProviders.put(defaultProviderName, defaultProvider);
            LOGGER.verbose("Using {} as the default {}.", defaultProviderName, clazz.getName());
        } else {
            defaultProvider = null;
        }

        while (it.hasNext()) {
            T additionalProvider = it.next();
            String additionalProviderName = additionalProvider.getClass().getName();
            availableProviders.put(additionalProviderName, additionalProvider);
            LOGGER.verbose("Additional provider found on the classpath: {}", additionalProviderName);
        }

        defaultImplementation = Configuration.getGlobalConfiguration()
            .get(PROPERTY_AZURE_HTTP_CLIENT_IMPLEMENTATION);
        noDefaultImplementation = CoreUtils.isNullOrEmpty(defaultImplementation);
    }


    public <U> U createInstance(Function<T, U> createInstance,
                                U fallBackInstance,
                                Class<? extends T> selectedImplementation,
                                String noDefaultProviderFoundMessage,
                                String noSpecificProviderFoundMessage) {
        if (defaultProvider == null) {
            if (fallBackInstance == null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(noDefaultProviderFoundMessage));
            }

            return fallBackInstance;
        }

        if (selectedImplementation == null && noDefaultImplementation) {
            return createInstance.apply(defaultProvider);
        }

        String implementationName = (selectedImplementation == null)
            ? defaultImplementation
            : selectedImplementation.getName();
        T provider = availableProviders.get(implementationName);
        if (provider == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException(noSpecificProviderFoundMessage + implementationName));
        }

        return createInstance.apply(provider);
    }
}
