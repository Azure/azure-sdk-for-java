/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

import java.util.concurrent.ConcurrentHashMap;


/**
 * Class for storing and retrieving encryption keystore providers.
 *
 * @param <T>
 *        user's object of type T.
 */
public class UserKeyStoreProviderCatalog<T> {
    private static UserKeyStoreProviderCatalog<?> instance = null;

    private ConcurrentHashMap<T, ConcurrentHashMap<String, EncryptionKeyStoreProvider>> keyStorage = new ConcurrentHashMap<T, ConcurrentHashMap<String, EncryptionKeyStoreProvider>>();

    private UserKeyStoreProviderCatalog() {}

    /**
     * Retrieves a singleton instance of UserKeyStoreProviderCatalog.
     *
     * @param <T>
     *        template type
     * @return UserKeyStoreProviderCatalog singleton instance
     */
    public static <T> UserKeyStoreProviderCatalog<?> getInstance() {
        if (null == instance) {
            instance = new UserKeyStoreProviderCatalog<T>();
        }

        return instance;
    }

    /**
     * Registers a keystore provider.
     *
     * @param userKey
     *        User's object of type T.
     * @param encryptionKeyStoreProvider
     *        Keystore provider object.
     */
    public void registerKeyStoreProvider(T userKey, EncryptionKeyStoreProvider encryptionKeyStoreProvider) {
        keyStorage.put(userKey, new ConcurrentHashMap<String, EncryptionKeyStoreProvider>());
    }

    /**
     * Retrieves a keystore provider.
     *
     * @param userKey
     *        User's object of type T.
     * @param providerName
     *        Name of provider.
     * @return Corresponding EncryptionKeyStoreProvider instance if exists.
     * @throws MicrosoftDataEncryptionException
     *         if provider is null
     */
    public EncryptionKeyStoreProvider getKeyStoreProvider(T userKey, String providerName) throws MicrosoftDataEncryptionException {
        keyStorage.put(userKey, new ConcurrentHashMap<String, EncryptionKeyStoreProvider>());
        ConcurrentHashMap<String, EncryptionKeyStoreProvider> value = keyStorage.get(userKey);
        EncryptionKeyStoreProvider provider = value.get(userKey);
        if (provider != null) {
            return provider;
        }

        throw new MicrosoftDataEncryptionException(MicrosoftDataEncryptionExceptionResource.getResource("R_KeystoreProviderError"));
    }

    /**
     * Removes the provider instance that corresponds to the user's key.
     *
     * @param userKey
     *        User's object of type T.
     */
    public void clearUserProviders(T userKey) {
        keyStorage.remove(userKey);
    }

    /**
     * Removes all provider instances held by this class.
     */
    public void clear() {
        keyStorage.clear();
    }
}
