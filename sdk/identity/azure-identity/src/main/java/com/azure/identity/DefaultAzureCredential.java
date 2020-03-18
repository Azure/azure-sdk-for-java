// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.identity.implementation.IdentityClientOptions;
import com.microsoft.aad.msal4jextensions.PersistenceSettings;
import com.sun.jna.Platform;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * Creates a credential using environment variables or the shared token cache. It tries to create a valid credential in
 * the following order:
 *
 * <ol>
 * <li>{@link EnvironmentCredential}</li>
 * <li>{@link ManagedIdentityCredential}</li>
 * <li>{@link SharedTokenCacheCredential}</li>
 * <li>{@link AzureCliCredential}</li>
 * <li>Fails if none of the credentials above could be created.</li>
 * </ol>
 */
@Immutable
public final class DefaultAzureCredential extends ChainedTokenCredential {
    private static final String DEFAULT_CACHE_FILE_NAME = "msal.cache";
    private static final Path DEFAULT_CACHE_DIRECTORY = Platform.isWindows() ?
            Paths.get(System.getProperty("user.home"), "AppData", "Local", ".IdentityService", "msal.cache") :
            Paths.get(System.getProperty("user.home"),".IdentityService", "msal.cache");
    private static final String DEFAULT_KEYCHAIN_SERVICE = "Microsoft.Developer.IdentityService";
    private static final String DEFAULT_KEYCHAIN_ACCOUNT = "MSALCache";
    private static final String DEFAULT_KEYRING_NAME = "default";
    private static final KeyRingItemSchema DEFAULT_KEYRING_SCHEMA = KeyRingItemSchema.GENERIC_SECRET;
    private static final String DEFAULT_KEYRING_ITEM_NAME = DEFAULT_KEYCHAIN_ACCOUNT;
    private static final String DEFAULT_KEYRING_ATTR_NAME = "MsalClientID";
    private static final String DEFAULT_KEYRING_ATTR_VALUE = "Microsoft.Developer.IdentityService";


    /**
     * Creates default DefaultAzureCredential instance to use. This will use AZURE_CLIENT_ID,
     * AZURE_CLIENT_SECRET, and AZURE_TENANT_ID environment variables to create a
     * ClientSecretCredential.
     *
     * If these environment variables are not available, then this will use the Shared MSAL
     * token cache.
     *
     * @param identityClientOptions the options to configure the IdentityClient
     */
    DefaultAzureCredential(IdentityClientOptions identityClientOptions) {
        super(new ArrayDeque<>(Arrays.asList(new EnvironmentCredential(identityClientOptions),
            new ManagedIdentityCredential(null, identityClientOptions),
            new SharedTokenCacheCredential(null, "04b07795-8ddb-461a-bbee-02f9e1bf7b46", null,
                identityClientOptions, PersistenceSettings.builder(DEFAULT_CACHE_FILE_NAME, DEFAULT_CACHE_DIRECTORY)
                    .setMacKeychain(DEFAULT_KEYCHAIN_SERVICE, DEFAULT_KEYCHAIN_ACCOUNT)
                    .setLinuxKeyring(DEFAULT_KEYRING_NAME, DEFAULT_KEYRING_SCHEMA.toString(), DEFAULT_KEYRING_ITEM_NAME,
                            DEFAULT_KEYRING_ATTR_NAME, DEFAULT_KEYRING_ATTR_VALUE, null, null)
                    // TODO: Check if libsecret is installed for Linux and use unprotected file cache if not
                    .build()),
            new AzureCliCredential(identityClientOptions))));
    }
}
