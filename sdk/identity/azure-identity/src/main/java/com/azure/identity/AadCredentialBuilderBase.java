// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * The base class for credential builders that allow specifying a client ID and tenant ID for an Azure Active Directory.
 * @param <T> the type of the credential builder
 */
public abstract class AadCredentialBuilderBase<T extends AadCredentialBuilderBase<T>> extends CredentialBuilderBase<T> {
    String clientId;
    String tenantId;

    /**
     * Specifies the Azure Active Directory endpoint to acquire tokens.
     * @param authorityHost the Azure Active Directory endpoint
     * @return An updated instance of this builder with the authority host set as specified.
     */
    @SuppressWarnings("unchecked")
    public T authorityHost(String authorityHost) {
        this.identityClientOptions.setAuthorityHost(authorityHost);
        return (T) this;
    }

    /**
     * Sets the client ID of the application.
     *
     * @param clientId the client ID of the application.
     * @return An updated instance of this builder with the client id set as specified.
     */
    @SuppressWarnings("unchecked")
    public T clientId(String clientId) {
        this.clientId = clientId;
        return (T) this;
    }

    /**
     * Sets the tenant ID of the application.
     *
     * @param tenantId the tenant ID of the application.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    @SuppressWarnings("unchecked")
    public T tenantId(String tenantId) {
        this.tenantId = tenantId;
        return (T) this;
    }

    /**
     * Specifies the ExecutorService to be used to execute the authentication requests.
     * Developer is responsible for maintaining the lifecycle of the ExecutorService.
     *
     * <p>
     * If this is not configured, the {@link ForkJoinPool#commonPool()} will be used which is
     * also shared with other application tasks. If the common pool is heavily used for other tasks, authentication
     * requests might starve and setting up this executor service should be considered.
     * </p>
     *
     * <p> The executor service and can be safely shutdown if the TokenCredential is no longer being used by the
     * Azure SDK clients and should be shutdown before the application exits. </p>
     *
     * @param executorService the executor service to use for executing authentication requests.
     * @return An updated instance of this builder with the executor service set as specified.
     */
    @SuppressWarnings("unchecked")
    public T executorService(ExecutorService executorService) {
        this.identityClientOptions.setExecutorService(executorService);
        return (T) this;
    }

    /**
     * Sets the location for the token cache file on Windows or Linux systems. The default
     * location is <code>{user home}/AppData/Local/.IdentityService/msal.cache</code> on
     * Windows and <code>~/.IdentityService/msal.cache</code> on Linux.
     *
     * @param cacheFileLocation The location for the token cache file.
     *
     * @return The updated T object.
     */
    @SuppressWarnings("unchecked")
    public T cacheFileLocation(Path cacheFileLocation) {
        this.identityClientOptions.setCacheFileLocation(cacheFileLocation);
        return (T) this;
    }

    /**
     * Sets the service name for the Keychain item on MacOS. The default value is
     * "Microsoft.Developer.IdentityService".
     *
     * @param serviceName The service name for the Keychain item.
     *
     * @return The updated T object.
     */
    @SuppressWarnings("unchecked")
    public T keychainService(String serviceName) {
        this.identityClientOptions.setKeychainService(serviceName);
        return (T) this;
    }

    /**
     * Sets the account name for the Keychain item on MacOS. The default value is
     * "MSALCache".
     *
     * @param accountName The account name for the Keychain item.
     *
     * @return The updated T object.
     */
    @SuppressWarnings("unchecked")
    public T keychainAccount(String accountName) {
        this.identityClientOptions.setKeychainAccount(accountName);
        return (T) this;
    }

    /**
     * Sets the name of the Gnome keyring to store the cache on Gnome keyring enabled
     * Linux systems. The default value is "default".
     *
     * @param keyringName The name of the Gnome keyring.
     *
     * @return The updated T object.
     */
    @SuppressWarnings("unchecked")
    public T keyringName(String keyringName) {
        this.identityClientOptions.setKeyringName(keyringName);
        return (T) this;
    }

    /**
     * Sets the schema of the Gnome keyring to store the cache on Gnome keyring enabled
     * Linux systems. The default value is <code>KeyringItemSchema.GenericSecret</code>.
     *
     * @param keyringItemSchema The schema of the Gnome keyring.
     *
     * @return The updated T object.
     */
    @SuppressWarnings("unchecked")
    public T keyringItemSchema(KeyringItemSchema keyringItemSchema) {
        this.identityClientOptions.setKeyringItemSchema(keyringItemSchema);
        return (T) this;
    }

    /**
     * Sets the name of the Gnome keyring item to store the cache on Gnome keyring enabled
     * Linux systems. The default value is "MSALCache".
     *
     * @param keyringItemName The name of the Gnome keyring item.
     *
     * @return The updated T object.
     */
    @SuppressWarnings("unchecked")
    public T keyringItemName(String keyringItemName) {
        this.identityClientOptions.setKeyringItemName(keyringItemName);
        return (T) this;
    }

    /**
     * Adds an attribute to the Gnome keyring item to store the cache on Gnome keyring enabled
     * Linux systems. Only 2 attributes are allowed.
     *
     * @param attributeName The name of the attribute.
     * @param attributeValue The value of the attribute.
     *
     * @return The updated T object.
     * @throws IllegalArgumentException if there are already 2 attributes
     */
    @SuppressWarnings("unchecked")
    public T addKeyringItemAttribute(String attributeName, String attributeValue) {
        this.identityClientOptions.addKeyringItemAttribute(attributeName, attributeValue);
        return (T) this;
    }

    /**
     * Sets whether to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is false by default.
     *
     * @param useUnprotectedFileOnLinux whether to use an unprotected file for cache storage.
     *
     * @return The updated T object.
     */
    @SuppressWarnings("unchecked")
    public T useUnprotectedFileOnLinux(boolean useUnprotectedFileOnLinux) {
        this.identityClientOptions.setUseUnprotectedFileOnLinux(useUnprotectedFileOnLinux);
        return (T) this;
    }

    /**
     * Disable using the shared token cache.
     *
     * @param disabled whether to disable using the shared token cache.
     *
     * @return The updated identity client options.
     */
    @SuppressWarnings("unchecked")
    public T disableSharedTokenCache(boolean disabled) {
        this.identityClientOptions.disableSharedTokenCache(disabled);
        return (T) this;
    }
}
