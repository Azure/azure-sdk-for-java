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

public final class Providers<TProvider, TInstance> {
    private static final ClientLogger LOGGER = new ClientLogger(Providers.class);
    private final TProvider defaultProvider;
    private final Map<String, TProvider> availableProviders;

    private final String defaultImplementation;
    private final boolean noDefaultImplementation;
    private final String noProviderMessage;
    private final Class<TProvider> providerClass;
    private String noSpecificProviderMessage;

    public Providers(Class<TProvider> providerClass, String noProviderErrorMessage) {
        this.providerClass = providerClass;
        // Use as classloader to load provider-configuration files and provider classes the classloader
        // that loaded this class. In most cases this will be the System classloader.
        // But this choice here provides additional flexibility in managed environments that control
        // classloading differently (OSGi, Spring and others) and don't depend on the
        // System classloader to load TProvider classes.
        ServiceLoader<TProvider> serviceLoader = ServiceLoader.load(providerClass, Providers.class.getClassLoader());

        availableProviders = new HashMap<>();
        // Use the first provider found in the service loader iterator.
        Iterator<TProvider> it = serviceLoader.iterator();
        if (it.hasNext()) {
            defaultProvider = it.next();
            String defaultProviderName = defaultProvider.getClass().getName();
            availableProviders.put(defaultProviderName, defaultProvider);
            LOGGER.verbose("Using {} as the default {}.", defaultProviderName, providerClass.getName());
        } else {
            defaultProvider = null;
        }

        while (it.hasNext()) {
            TProvider additionalProvider = it.next();
            String additionalProviderName = additionalProvider.getClass().getName();
            availableProviders.put(additionalProviderName, additionalProvider);
            LOGGER.verbose("Additional provider found on the classpath: {}", additionalProviderName);
        }

        defaultImplementation = Configuration.getGlobalConfiguration()
            .get(PROPERTY_AZURE_HTTP_CLIENT_IMPLEMENTATION);
        noDefaultImplementation = CoreUtils.isNullOrEmpty(defaultImplementation);
        noProviderMessage = noProviderErrorMessage;
    }

    private String formatNoSpecificProviderErrorMessage(String selectedImplementation) {
        return String.format("A request was made to use a specific "
                + "%s but it wasn't found on the classpath. If you're using a dependency manager ensure you're "
                + "including the dependency that provides the specific implementation. If you're including the "
                + "specific implementation ensure that the %s service it supplies is being included in the "
                + "'META-INF/services' file '%s'. The requested %s was: ",
                providerClass.getName(), providerClass.getName(), providerClass.getName(), selectedImplementation);
    }

    public TInstance createInstance(Function<TProvider, TInstance> createInstance,
                                TInstance fallBackInstance, Class<? extends TProvider> selectedImplementation) {
        if (defaultProvider == null) {
            if (fallBackInstance == null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(noProviderMessage));
            }

            return fallBackInstance;
        }

        if (selectedImplementation == null && noDefaultImplementation) {
            return createInstance.apply(defaultProvider);
        }

        String implementationName = (selectedImplementation == null)
            ? defaultImplementation
            : selectedImplementation.getName();

        TProvider provider = availableProviders.get(implementationName);
        if (provider == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException(formatNoSpecificProviderErrorMessage(implementationName)));
        }

        return createInstance.apply(provider);
    }
}
