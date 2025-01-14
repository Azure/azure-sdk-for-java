// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.v2.implementation.util;

import com.azure.core.v2.util.CoreUtils;
import io.clientcore.core.util.ClientLogger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;

/**
 * Helper class that unifies SPI instances creation.
 *
 * @param <TProvider> Service Provider interface.
 * @param <TInstance> Service interface type.
 */
public final class Providers<TProvider, TInstance> {
    private static final ClientLogger LOGGER = new ClientLogger(Providers.class);
    private final TProvider defaultProvider;
    private final String defaultProviderName;
    private final Map<String, TProvider> availableProviders;

    private final String defaultImplementation;
    private final boolean noDefaultImplementation;
    private final String noProviderMessage;
    private final Class<TProvider> providerClass;

    /**
     * Resolves available providers.
     * @param providerClass Provider class.
     * @param defaultImplementationName Explicit name of implementation provider class to use.
     * @param noProviderErrorMessage Error message to throw and log in case no providers are found.
     */
    public Providers(Class<TProvider> providerClass, String defaultImplementationName, String noProviderErrorMessage) {
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
            defaultProviderName = defaultProvider.getClass().getName();
            availableProviders.put(defaultProviderName, defaultProvider);
            LOGGER.atVerbose().log("Using " + defaultProviderName + " as the default " + providerClass.getName() + ".");
        } else {
            defaultProvider = null;
            defaultProviderName = null;
        }

        while (it.hasNext()) {
            TProvider additionalProvider = it.next();
            String additionalProviderName = additionalProvider.getClass().getName();
            availableProviders.put(additionalProviderName, additionalProvider);
            LOGGER.atVerbose().log("Additional provider found on the classpath: " + additionalProviderName);
        }

        defaultImplementation = defaultImplementationName;
        noDefaultImplementation = CoreUtils.isNullOrEmpty(defaultImplementation);
        noProviderMessage = noProviderErrorMessage;
    }

    private String formatNoSpecificProviderErrorMessage(String selectedImplementation) {
        return "A request was made to use a specific " + providerClass.getSimpleName() + " but it wasn't found on the "
            + "classpath. If you're using a dependency manager ensure you're including the dependency that provides "
            + "the specific implementation. If you're including the specific implementation ensure that the "
            + providerClass.getSimpleName() + " service it supplies is being included in the 'META-INF/services' file "
            + "'" + providerClass.getName() + "'. The requested provider was: " + selectedImplementation + ".";
    }

    /**
     * Creates instance of service.
     *
     * @param createInstance callback that creates service instance with resolved provider.
     * @param fallbackInstance service instance to return if provider is not found. Usually a no-op implementation.
     * If null and no provider (satisfying all conditions) is found, throws {@link IllegalStateException}
     * @param selectedImplementation Explicit provider implementation class. It still must be registered in
     * META-INF/services.
     * @return created service instance.
     *
     * @throws IllegalStateException when requested provider cannot be found and fallback instance is null.
     */
    public TInstance create(Function<TProvider, TInstance> createInstance, TInstance fallbackInstance,
        Class<? extends TProvider> selectedImplementation) {
        TProvider provider;
        String implementationName;
        if (selectedImplementation == null && noDefaultImplementation) {
            implementationName = defaultProviderName;
            provider = defaultProvider;
            if (provider == null) {
                if (fallbackInstance == null) {
                    throw LOGGER.logThrowableAsError(new IllegalStateException(noProviderMessage));
                }

                return fallbackInstance;
            }
        } else {
            implementationName
                = selectedImplementation == null ? defaultImplementation : selectedImplementation.getName();
            provider = availableProviders.get(implementationName);
            if (provider == null) {
                // no fallback here - user requested specific implementation, and it was not found
                throw LOGGER.logThrowableAsError(
                    new IllegalStateException(formatNoSpecificProviderErrorMessage(implementationName)));
            }
        }

        try {
            return createInstance.apply(provider);
        } catch (ClassCastException ex) {
            throw LOGGER.logThrowableAsError(
                new IllegalStateException(formatNoSpecificProviderErrorMessage(implementationName), ex));
        }
    }
}
