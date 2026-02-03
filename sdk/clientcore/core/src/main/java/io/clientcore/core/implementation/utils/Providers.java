// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.utils;

import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.CoreUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;

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
     *
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

        TProvider defaultProvider = null;
        String defaultProviderName = null;
        this.availableProviders = new HashMap<>();

        // Load all provider instances.
        Iterator<TProvider> it = serviceLoader.iterator();
        while (it.hasNext()) {
            try {
                TProvider provider = it.next();
                String providerName = provider.getClass().getName();
                availableProviders.put(providerName, provider);
                if (defaultProvider == null) {
                    defaultProvider = provider;
                    defaultProviderName = providerName;
                    LOGGER.atVerbose()
                        .addKeyValue("providerName", providerName)
                        .addKeyValue("providerClass", providerClass.getName())
                        .log("Loaded default provider.");
                } else {
                    LOGGER.atVerbose()
                        .addKeyValue("providerName", providerName)
                        .log("Additional provider found on the classpath");
                }
            } catch (LinkageError | ServiceConfigurationError error) {
                LOGGER.atWarning().setThrowable(error).log("Failed to load a provider instance.");
            }
        }

        this.defaultProvider = defaultProvider;
        this.defaultProviderName = defaultProviderName;
        this.defaultImplementation = defaultImplementationName;
        this.noDefaultImplementation = CoreUtils.isNullOrEmpty(defaultImplementation);
        this.noProviderMessage = noProviderErrorMessage;
    }

    private String formatNoSpecificProviderErrorMessage(String selectedImplementation) {
        return "A request was made to use a specific " + providerClass.getSimpleName() + " but it wasn't found on the "
            + "classpath. If you're using a dependency manager ensure you're including the dependency that provides "
            + "the specific implementation. If you're including the specific implementation ensure that the "
            + providerClass.getSimpleName() + " service it supplies is being included in the 'META-INF/services' file "
            + "'" + providerClass.getName() + "'. The requested provider was: " + selectedImplementation + ".";
    }

    /**
     * Creates an instance of a service.
     *
     * @param createInstance Callback that creates a service instance with the resolved provider.
     * @param fallbackSupplier Supplier to get a fallback instance from if a provider to create a service instance is
     * not found. Usually a no-op implementation.
     * @param selectedImplementation Explicit provider implementation class. It still must be registered in
     * META-INF/services.
     *
     * @return The created service instance.
     *
     * @throws IllegalStateException when the requested provider cannot be found and the fallback supplier or the value
     * it returns are {@code null}.
     */
    public TInstance create(Function<TProvider, TInstance> createInstance, Supplier<TInstance> fallbackSupplier,
        Class<? extends TProvider> selectedImplementation) {
        TProvider provider;
        String implementationName;

        if (selectedImplementation == null && noDefaultImplementation) {
            implementationName = defaultProviderName;
            provider = defaultProvider;

            if (provider == null) {
                TInstance instance = fallbackSupplier == null ? null : fallbackSupplier.get();

                if (instance == null) {
                    throw LOGGER.throwableAtError().log(noProviderMessage, IllegalStateException::new);
                }

                return instance;
            }
        } else {
            implementationName
                = selectedImplementation == null ? defaultImplementation : selectedImplementation.getName();
            provider = availableProviders.get(implementationName);

            if (provider == null) {
                // No fallback here - user requested specific implementation, and it was not found.
                throw LOGGER.throwableAtError()
                    .log(formatNoSpecificProviderErrorMessage(implementationName), IllegalStateException::new);
            }
        }

        try {
            return createInstance.apply(provider);
        } catch (ClassCastException ex) {
            throw LOGGER.throwableAtError()
                .log(formatNoSpecificProviderErrorMessage(implementationName), ex, IllegalStateException::new);
        }
    }
}
