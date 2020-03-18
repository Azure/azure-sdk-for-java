// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.ValidationUtil;
import com.microsoft.aad.msal4jextensions.PersistenceSettings;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Fluent credential builder for instantiating a {@link SharedTokenCacheCredential}.
 *
 * @see SharedTokenCacheCredential
 */
public class SharedTokenCacheCredentialBuilder extends AadCredentialBuilderBase<SharedTokenCacheCredentialBuilder> {
    private final ClientLogger logger = new ClientLogger(SharedTokenCacheCredentialBuilder.class);
    private String username;
    private String cacheFileName;
    private Path cacheFileDirectory;
    private String keychainService;
    private String keychainAccount;
    private String keyringName;
    private KeyringItemSchema keyringItemSchema;
    private String keyringItemName;
    private final LinkedHashMap<String, String> attributes = new LinkedHashMap<>(); // preserve order
    private boolean useUnprotectedFileOnLinux = false;

    /**
     * Sets the username for the account.
     *
     * @param username The username for the account.
     *
     * @return The updated SharedTokenCacheCredentialBuilder object.
     */
    public SharedTokenCacheCredentialBuilder username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Sets the location for the token cache file on Windows or Linux systems. The default
     * location is <code>{user home}/AppData/Local/.IdentityService/msal.cache</code> on
     * Windows and <code>~/.IdentityService/msal.cache</code> on Linux.
     *
     * @param cacheFileLocation The location for the token cache file.
     *
     * @return The updated SharedTokenCacheCredentialBuilder object.
     */
    public SharedTokenCacheCredentialBuilder cacheFileLocation(Path cacheFileLocation) {
        this.cacheFileName = cacheFileLocation.getFileName().toString();
        this.cacheFileDirectory = cacheFileLocation.getParent();
        return this;
    }

    /**
     * Sets the service name for the Keychain item on MacOS. The default value is
     * "Microsoft.Developer.IdentityService".
     *
     * @param serviceName The service name for the Keychain item.
     *
     * @return The updated SharedTokenCacheCredentialBuilder object.
     */
    public SharedTokenCacheCredentialBuilder keychainService(String serviceName) {
        this.keychainService = serviceName;
        return this;
    }

    /**
     * Sets the account name for the Keychain item on MacOS. The default value is
     * "MSALCache".
     *
     * @param accountName The account name for the Keychain item.
     *
     * @return The updated SharedTokenCacheCredentialBuilder object.
     */
    public SharedTokenCacheCredentialBuilder keychainAccount(String accountName) {
        this.keychainAccount = accountName;
        return this;
    }

    /**
     * Sets the name of the Gnome keyring to store the cache on Gnome keyring enabled
     * Linux systems. The default value is "default".
     *
     * @param keyringName The name of the Gnome keyring.
     *
     * @return The updated SharedTokenCacheCredentialBuilder object.
     */
    public SharedTokenCacheCredentialBuilder keyringName(String keyringName) {
        this.keyringName = keyringName;
        return this;
    }

    /**
     * Sets the schema of the Gnome keyring to store the cache on Gnome keyring enabled
     * Linux systems. The default value is <code>KeyringItemSchema.GenericSecret</code>.
     *
     * @param keyringItemSchema The schema of the Gnome keyring.
     *
     * @return The updated SharedTokenCacheCredentialBuilder object.
     */
    public SharedTokenCacheCredentialBuilder keyringItemSchema(KeyringItemSchema keyringItemSchema) {
        this.keyringItemSchema = keyringItemSchema;
        return this;
    }

    /**
     * Sets the name of the Gnome keyring item to store the cache on Gnome keyring enabled
     * Linux systems. The default value is "MSALCache".
     *
     * @param keyringItemName The name of the Gnome keyring item.
     *
     * @return The updated SharedTokenCacheCredentialBuilder object.
     */
    public SharedTokenCacheCredentialBuilder keyringItemName(String keyringItemName) {
        this.keyringItemName = keyringItemName;
        return this;
    }

    /**
     * Adds an attribute to the Gnome keyring item to store the cache on Gnome keyring enabled
     * Linux systems. Only 2 attributes are allowed.
     *
     * @param attributeName The name of the attribute.
     * @param attributeValue The value of the attribute.
     *
     * @return The updated SharedTokenCacheCredentialBuilder object.
     * @throws IllegalArgumentException if there are already 2 attributes
     */
    public SharedTokenCacheCredentialBuilder addKeyringItemAttribute(String attributeName, String attributeValue) {
        if (this.attributes.size() < 2) {
            this.attributes.put(attributeName, attributeValue);
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Currently does not support more than 2 attributes for linux Keyring"));
        }
        return this;
    }

    /**
     * Sets whether to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is false by default.
     *
     * @param useUnprotectedFileOnLinux whether to use an unprotected file for cache storage.
     *
     * @return The updated SharedTokenCacheCredentialBuilder object.
     */
    public SharedTokenCacheCredentialBuilder useUnprotectedFileOnLinux(boolean useUnprotectedFileOnLinux) {
        this.useUnprotectedFileOnLinux = useUnprotectedFileOnLinux;
        return this;
    }

    /**
     * Creates a new {@link SharedTokenCacheCredentialBuilder} with the current configurations.
     *
     * @return a {@link SharedTokenCacheCredentialBuilder} with the current configurations.
     */
    public SharedTokenCacheCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("cacheFileName", cacheFileName);
                put("cacheFileDirectory", cacheFileDirectory);
            }});
        List<String> attributeKeyList = new ArrayList<>(attributes.keySet());
        while (attributeKeyList.size() < 2) {
            attributeKeyList.add(null);
        }
        String key1 = attributeKeyList.get(0);
        String key2 = attributeKeyList.get(1);
        PersistenceSettings.Builder persistenceBuilder = PersistenceSettings.builder(cacheFileName, cacheFileDirectory)
                .setLinuxUseUnprotectedFileAsCacheStorage(useUnprotectedFileOnLinux);
        if (keychainService != null && keychainAccount != null) {
            persistenceBuilder.setMacKeychain(keychainService, keychainAccount);
        }
        if (keyringName != null && keyringItemName != null && keyringItemSchema != null) {
            persistenceBuilder.setLinuxKeyring(keyringName, keyringItemSchema.toString(), keyringItemName,
                    key1, attributes.getOrDefault(key1, null), key2, attributes.getOrDefault(key2, null));
        }
        return new SharedTokenCacheCredential(
                username, clientId, tenantId, identityClientOptions, persistenceBuilder.build());
    }
}
