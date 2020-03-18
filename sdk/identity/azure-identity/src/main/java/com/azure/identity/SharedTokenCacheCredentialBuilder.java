// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

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
    private String username;
    private Path cacheFileLocation;
    private String keyChainService;
    private String keyChainAccount;
    private String keyRingName;
    private KeyRingItemSchema keyRingItemSchema;
    private String keyRingItemName;
    private LinkedHashMap<String, String> attributes = new LinkedHashMap<>(); // preserve order
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
        this.cacheFileLocation = cacheFileLocation;
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
    public SharedTokenCacheCredentialBuilder keyChainService(String serviceName) {
        this.keyChainService = serviceName;
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
    public SharedTokenCacheCredentialBuilder keyChainAccount(String accountName) {
        this.keyChainAccount = accountName;
        return this;
    }

    /**
     * Sets the name of the Gnome keyring to store the cache on Gnome keyring enabled
     * Linux systems. The default value is "default".
     *
     * @param keyRingName The name of the Gnome keyring.
     *
     * @return The updated SharedTokenCacheCredentialBuilder object.
     */
    public SharedTokenCacheCredentialBuilder keyRingName(String keyRingName) {
        this.keyRingName = keyRingName;
        return this;
    }

    /**
     * Sets the schema of the Gnome keyring to store the cache on Gnome keyring enabled
     * Linux systems. The default value is <code>KeyRingItemSchema.GenericSecret</code>.
     *
     * @param keyRingItemSchema The schema of the Gnome keyring.
     *
     * @return The updated SharedTokenCacheCredentialBuilder object.
     */
    public SharedTokenCacheCredentialBuilder keyRingItemSchema(KeyRingItemSchema keyRingItemSchema) {
        this.keyRingItemSchema = keyRingItemSchema;
        return this;
    }

    /**
     * Sets the name of the Gnome keyring item to store the cache on Gnome keyring enabled
     * Linux systems. The default value is "MSALCache".
     *
     * @param keyRingItemName The name of the Gnome keyring item.
     *
     * @return The updated SharedTokenCacheCredentialBuilder object.
     */
    public SharedTokenCacheCredentialBuilder keyRingItemName(String keyRingItemName) {
        this.keyRingItemName = keyRingItemName;
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
     */
    public SharedTokenCacheCredentialBuilder addKeyRingItemAttribute(String attributeName, String attributeValue) {
        if (this.attributes.size() < 2) {
            this.attributes.put(attributeName, attributeValue);
        } else {
            throw new IllegalArgumentException("Currently does not support more than 2 attributes for linux KeyRing");
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
            put("cacheFileLocation", cacheFileLocation);
        }});
        List<String> attributeKeyList = new ArrayList<>(attributes.keySet());
        while (attributeKeyList.size() < 2) {
            attributeKeyList.add(null);
        }
        String key1 = attributeKeyList.get(0);
        String key2 = attributeKeyList.get(1);
        PersistenceSettings.Builder persistenceSettings = PersistenceSettings.builder(
                    cacheFileLocation.getFileName().toString(), cacheFileLocation.getParent())
                .setLinuxUseUnprotectedFileAsCacheStorage(useUnprotectedFileOnLinux);
        if (keyChainService != null && keyChainAccount != null) {
            persistenceSettings.setMacKeychain(keyChainService, keyChainAccount);
        }
        if (keyRingName != null && keyRingItemName != null && keyRingItemSchema != null) {
            persistenceSettings.setLinuxKeyring(keyRingName, keyRingItemSchema.toString(), keyRingItemName,
                    key1, attributes.getOrDefault(key1, null), key2, attributes.getOrDefault(key2, null));
        }
        return new SharedTokenCacheCredential(
                username, clientId, tenantId, identityClientOptions, persistenceSettings.build());
    }
}
