// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.connectionstring;

import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.sas.CommonSasQueryParameters;

import java.util.Objects;

/**
 * Type represents settings for accessing a storage account.
 */
public final class StorageAuthenticationSettings {
    private final Type type;
    private final String sasToken;
    private final Account account;

    /**
     * @return the settings type (None, Account Name and Key, Sas token)
     */
    public Type getType() {
        return this.type;
    }

    /**
     * @return the sas token
     */
    public String getSasToken() {
        return this.sasToken;
    }

    /**
     * @return the account instance containing account name and key
     */
    public Account getAccount() {
        return this.account;
    }

    /**
     * Creates {@link StorageAuthenticationSettings} from the given connection settings.
     *
     * @param settings the connection settings.
     * @return the StorageAuthenticationSettings.
     */
    public static StorageAuthenticationSettings fromConnectionSettings(final ConnectionSettings settings) {
        final String accountName = settings.getSettingValue(Constants.ConnectionStringConstants.ACCOUNT_NAME);
        final String accountKey = settings.getSettingValue(Constants.ConnectionStringConstants.ACCOUNT_KEY_NAME);
        final String sasSignature =
                settings.getSettingValue(Constants.ConnectionStringConstants.SHARED_ACCESS_SIGNATURE_NAME);

        if (accountName != null && accountKey != null && sasSignature == null) {
            return new StorageAuthenticationSettings(new Account(accountName, accountKey));
        }
        if (accountKey == null && sasSignature != null) {
            return new StorageAuthenticationSettings(sasSignature);
        }
        return new StorageAuthenticationSettings();
    }

    /**
     * @return get a {@link StorageAuthenticationSettings} for emulator
     */
    public static StorageAuthenticationSettings forEmulator() {
        return new StorageAuthenticationSettings(new Account(Constants.ConnectionStringConstants.EMULATOR_ACCOUNT_NAME,
                Constants.ConnectionStringConstants.EMULATOR_ACCOUNT_KEY));
    }

    /**
     * Creates default {@link StorageAuthenticationSettings} indicating absence of authentication
     * setting.
     */
    private StorageAuthenticationSettings() {
        this.type = Type.NONE;
        this.account = null;
        this.sasToken = null;
    }

    /**
     * Creates {@link StorageAuthenticationSettings} indicating Sas token based authentication
     * settings.
     *
     * @param sasToken the sas token
     */
    private StorageAuthenticationSettings(String sasToken) {
        this.type = Type.SAS_TOKEN;
        // sanitize SAS
        this.sasToken = new CommonSasQueryParameters(
            SasImplUtils.parseQueryString(Objects.requireNonNull(sasToken)), /*remove from map*/ false).encode();
        this.account = null;
    }

    /**
     * Creates {@link StorageAuthenticationSettings} indicating account name and key based
     * authentication settings.
     *
     * @param account the account instance holding account name and key
     */
    private StorageAuthenticationSettings(Account account) {
        this.type = Type.ACCOUNT_NAME_KEY;
        this.account = Objects.requireNonNull(account);
        this.sasToken = null;
    }

    /**
     * Authentication settings type.
     */
    public enum Type {
        /**
         * No auth.
         */
        NONE(),
        /**
         * Auth based on storage account name and key.
         */
        ACCOUNT_NAME_KEY(),
        /**
         * Auth based on SAS token.
         */
        SAS_TOKEN(),
    }

    /**
     * Type to hold storage account name and access key.
     */
    public static final class Account {
        private String name;
        private String accessKey;

        /**
         * Creates Account.
         *
         * @param name the storage account name
         * @param accessKey the storage access key
         */
        private Account(String name, String accessKey) {
            this.name = Objects.requireNonNull(name);
            this.accessKey = Objects.requireNonNull(accessKey);
        }

        /**
         * @return the storage account name
         */
        public String getName() {
            return this.name;
        }

        /**
         * @return the storage account access key
         */
        public String getAccessKey() {
            return this.accessKey;
        }
    }
}

