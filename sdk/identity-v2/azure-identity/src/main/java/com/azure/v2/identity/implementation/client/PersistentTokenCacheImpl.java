// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.client;

import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.implementation.util.IdentityUtil;
import com.microsoft.aad.msal4j.ITokenCacheAccessAspect;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;
import com.microsoft.aad.msal4jextensions.PersistenceSettings;
import com.microsoft.aad.msal4jextensions.PersistenceTokenCacheAccessAspect;
import com.microsoft.aad.msal4jextensions.persistence.linux.KeyRingAccessException;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PersistentTokenCacheImpl implements ITokenCacheAccessAspect {
    private static final String DEFAULT_CACHE_FILE_NAME = "msal.cache";
    private static final String CAE_ENABLED_CACHE_SUFFIX = ".cae";
    private static final String CAE_DISABLED_CACHE_SUFFIX = ".nocae";
    private static final String DEFAULT_CONFIDENTIAL_CACHE_FILE_NAME = "msal.confidential.cache";
    static final Path DEFAULT_CACHE_FILE_PATH = IdentityUtil.isWindowsPlatform()
        ? Paths.get(System.getProperty("user.home"), "AppData", "Local", ".IdentityService")
        : Paths.get(System.getProperty("user.home"), ".IdentityService");
    private static final String DEFAULT_KEYCHAIN_SERVICE = "Microsoft.Developer.IdentityService";
    private static final String DEFAULT_KEYCHAIN_ACCOUNT = "MSALCache";
    private static final String DEFAULT_CONFIDENTIAL_KEYCHAIN_ACCOUNT = "MSALConfidentialCache";
    private static final String DEFAULT_KEYRING_NAME = "default";
    private static final String DEFAULT_KEYRING_SCHEMA = "msal.cache";
    private static final String DEFAULT_KEYRING_ITEM_NAME = DEFAULT_KEYCHAIN_ACCOUNT;
    private static final String DEFAULT_CONFIDENTIAL_KEYRING_ITEM_NAME = DEFAULT_CONFIDENTIAL_KEYCHAIN_ACCOUNT;
    private static final String DEFAULT_KEYRING_ATTR_NAME = "MsalClientID";
    private static final String DEFAULT_KEYRING_ATTR_VALUE = "Microsoft.Developer.IdentityService";

    private static final ClientLogger LOGGER = new ClientLogger(PersistentTokenCacheImpl.class);
    private boolean allowUnencryptedStorage;
    private String name;
    private PersistenceTokenCacheAccessAspect cacheAccessAspect;

    private final boolean caeEnabled;

    public PersistentTokenCacheImpl(boolean caeEnabled) {
        super();
        this.caeEnabled = caeEnabled;
    }

    public PersistentTokenCacheImpl setAllowUnencryptedStorage(boolean allowUnencryptedStorage) {
        this.allowUnencryptedStorage = allowUnencryptedStorage;
        return this;
    }

    public PersistentTokenCacheImpl setName(String name) {
        this.name = name;
        return this;
    }

    boolean registerCache() {
        try {
            PersistenceSettings persistenceSettings = getPersistenceSettings();
            cacheAccessAspect = new PersistenceTokenCacheAccessAspect(persistenceSettings);
            return true;
        } catch (IOException | RuntimeException t) {
            throw LOGGER.throwableAtError()
                .log("Shared token cache is unavailable in this environment.", t,
                    CredentialAuthenticationException::new);
        }
    }

    public void beforeCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
        cacheAccessAspect.beforeCacheAccess(iTokenCacheAccessContext);
    }

    public void afterCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
        cacheAccessAspect.afterCacheAccess(iTokenCacheAccessContext);
    }

    private PersistenceSettings getPersistenceSettings() {
        PersistenceSettings.Builder persistenceSettingsBuilder = PersistenceSettings
            .builder(getCacheName(name != null ? name : DEFAULT_CACHE_FILE_NAME), DEFAULT_CACHE_FILE_PATH);
        if (IdentityUtil.isMacPlatform()) {
            persistenceSettingsBuilder.setMacKeychain(DEFAULT_KEYCHAIN_SERVICE,
                getCacheName(name != null ? name : DEFAULT_KEYCHAIN_ACCOUNT));
            return persistenceSettingsBuilder.build();
        } else if (IdentityUtil.isLinuxPlatform()) {
            try {
                persistenceSettingsBuilder.setLinuxKeyring(DEFAULT_KEYRING_NAME, DEFAULT_KEYRING_SCHEMA,
                    getCacheName(name != null ? name : DEFAULT_KEYRING_ITEM_NAME), DEFAULT_KEYRING_ATTR_NAME,
                    DEFAULT_KEYRING_ATTR_VALUE, null, null);
                return persistenceSettingsBuilder.build();
            } catch (KeyRingAccessException e) {
                if (!allowUnencryptedStorage) {
                    // not logging here, caller is logging everything
                    throw e;
                }
                persistenceSettingsBuilder.setLinuxUseUnprotectedFileAsCacheStorage(true);
                return persistenceSettingsBuilder.build();
            }
        }
        return persistenceSettingsBuilder.build();
    }

    private String getCacheName(String name) {
        if (caeEnabled) {
            return name + CAE_ENABLED_CACHE_SUFFIX;
        } else {
            return name + CAE_DISABLED_CACHE_SUFFIX;
        }
    }
}
