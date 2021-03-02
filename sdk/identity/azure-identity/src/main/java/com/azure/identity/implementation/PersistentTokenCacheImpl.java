package com.azure.identity.implementation;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.logging.ClientLogger;
import com.microsoft.aad.msal4j.ITokenCacheAccessAspect;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;
import com.microsoft.aad.msal4jextensions.PersistenceSettings;
import com.microsoft.aad.msal4jextensions.PersistenceTokenCacheAccessAspect;
import com.microsoft.aad.msal4jextensions.persistence.linux.KeyRingAccessException;
import com.sun.jna.Platform;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PersistentTokenCacheImpl implements ITokenCacheAccessAspect {
    private static final String DEFAULT_PUBLIC_CACHE_FILE_NAME = "msal.cache";
    private static final String DEFAULT_CONFIDENTIAL_CACHE_FILE_NAME = "msal.confidential.cache";
    private static final Path DEFAULT_CACHE_FILE_PATH = Platform.isWindows()
        ? Paths.get(System.getProperty("user.home"), "AppData", "Local", ".IdentityService")
        : Paths.get(System.getProperty("user.home"), ".IdentityService");
    private static final String DEFAULT_KEYCHAIN_SERVICE = "Microsoft.Developer.IdentityService";
    private static final String DEFAULT_PUBLIC_KEYCHAIN_ACCOUNT = "MSALCache";
    private static final String DEFAULT_CONFIDENTIAL_KEYCHAIN_ACCOUNT = "MSALConfidentialCache";
    private static final String DEFAULT_KEYRING_NAME = "default";
    private static final String DEFAULT_KEYRING_SCHEMA = "msal.cache";
    private static final String DEFAULT_PUBLIC_KEYRING_ITEM_NAME = DEFAULT_PUBLIC_KEYCHAIN_ACCOUNT;
    private static final String DEFAULT_CONFIDENTIAL_KEYRING_ITEM_NAME = DEFAULT_CONFIDENTIAL_KEYCHAIN_ACCOUNT;
    private static final String DEFAULT_KEYRING_ATTR_NAME = "MsalClientID";
    private static final String DEFAULT_KEYRING_ATTR_VALUE = "Microsoft.Developer.IdentityService";

    private final ClientLogger logger = new ClientLogger(PersistentTokenCacheImpl.class);
    private boolean allowUnencryptedStorage;
    private String name;
    private PersistenceTokenCacheAccessAspect cacheAccessAspect;
    private MsalClientType msalClientType;

    public PersistentTokenCacheImpl() {
        super();
    }

    public PersistentTokenCacheImpl setAllowUnencryptedStorage(boolean allowUnencryptedStorage) {
        this.allowUnencryptedStorage = allowUnencryptedStorage;
        return this;
    }

    public PersistentTokenCacheImpl setName(String name) {
        this.name = name;
        return this;
    }

    Mono<Boolean> registerCache() {
        return Mono.defer(() -> {
                try {
                    PersistenceSettings persistenceSettings;
                    switch (msalClientType) {
                        case CONFIDENTIAL:
                            persistenceSettings = getConfidentialClientPersistenceSettings();
                            break;
                        case PUBLIC:
                            persistenceSettings = getPublicClientPersistenceSettings();
                            break;
                        default:
                            persistenceSettings = getPublicClientPersistenceSettings();
                            break;
                    }
                    cacheAccessAspect = new PersistenceTokenCacheAccessAspect(persistenceSettings);
                    return Mono.just(true);
                } catch (Throwable t) {
                    return Mono.error(logger.logExceptionAsError(new ClientAuthenticationException(
                        "Shared token cache is unavailable in this environment.", null, t)));
                }
            });
    }

    public void beforeCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
        cacheAccessAspect.beforeCacheAccess(iTokenCacheAccessContext);
    }

    public void afterCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
        cacheAccessAspect.afterCacheAccess(iTokenCacheAccessContext);
    }

    public enum MsalClientType {
        PUBLIC,
        CONFIDENTIAL;
    }

    public void setMsalClientType(MsalClientType msalClientType) {
        this.msalClientType = msalClientType;
    }

    private PersistenceSettings getPublicClientPersistenceSettings() {
        PersistenceSettings.Builder persistenceSettingsBuilder = PersistenceSettings.builder(
            name != null ? name : DEFAULT_PUBLIC_CACHE_FILE_NAME, DEFAULT_CACHE_FILE_PATH);
        if (Platform.isMac()) {
            persistenceSettingsBuilder.setMacKeychain(
                DEFAULT_KEYCHAIN_SERVICE, name != null ? name : DEFAULT_PUBLIC_KEYCHAIN_ACCOUNT);
            return persistenceSettingsBuilder.build();
        } else if (Platform.isLinux()) {
            try {
                persistenceSettingsBuilder
                    .setLinuxKeyring(DEFAULT_KEYRING_NAME, DEFAULT_KEYRING_SCHEMA,
                        name != null ? name : DEFAULT_PUBLIC_KEYRING_ITEM_NAME, DEFAULT_KEYRING_ATTR_NAME,
                        DEFAULT_KEYRING_ATTR_VALUE, null, null);
                return persistenceSettingsBuilder.build();
            } catch (KeyRingAccessException e) {
                if (!allowUnencryptedStorage) {
                    throw logger.logExceptionAsError(e);
                }
                persistenceSettingsBuilder.setLinuxUseUnprotectedFileAsCacheStorage(true);
                return persistenceSettingsBuilder.build();
            }
        }
        return persistenceSettingsBuilder.build();
    }

    private PersistenceSettings getConfidentialClientPersistenceSettings() {
        PersistenceSettings.Builder persistenceSettingsBuilder = PersistenceSettings.builder(
            name != null ? name : DEFAULT_CONFIDENTIAL_CACHE_FILE_NAME, DEFAULT_CACHE_FILE_PATH);
        if (Platform.isMac()) {
            persistenceSettingsBuilder.setMacKeychain(
                DEFAULT_KEYCHAIN_SERVICE, name != null ? name : DEFAULT_CONFIDENTIAL_KEYCHAIN_ACCOUNT);
        }
        if (Platform.isLinux()) {
            try {
                persistenceSettingsBuilder
                    .setLinuxKeyring(DEFAULT_KEYRING_NAME, DEFAULT_KEYRING_SCHEMA,
                        name != null ? name : DEFAULT_CONFIDENTIAL_KEYRING_ITEM_NAME, DEFAULT_KEYRING_ATTR_NAME,
                        DEFAULT_KEYRING_ATTR_VALUE, null, null);
                return persistenceSettingsBuilder.build();
            } catch (KeyRingAccessException e) {
                if (!allowUnencryptedStorage) {
                    throw logger.logExceptionAsError(e);
                }
                persistenceSettingsBuilder.setLinuxUseUnprotectedFileAsCacheStorage(true);
                return persistenceSettingsBuilder.build();
            }
        }
        return persistenceSettingsBuilder.build();
    }


}
