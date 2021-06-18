// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation;

import java.util.Objects;

/**
 * Type represents settings for accessing a storage account.
 */
public final class StorageAuthenticationSettings {
    private final Type type;
    private final String sasToken;
    private final Account account;

    /**
     * @return The settings type (None, Account Name and Key, Sas token).
     */
    public Type getType() {
        return this.type;
    }

    /**
     * @return The SAS token.
     */
    public String getSasToken() {
        return this.sasToken;
    }

    /**
     * @return The account instance containing account name and key.
     */
    public Account getAccount() {
        return this.account;
    }

    /**
     * Creates {@link StorageAuthenticationSettings} from the given connection settings.
     *
     * @param settings The {@link ConnectionSettings}.
     *
     * @return An instance of {@link StorageAuthenticationSettings}.
     */
    public static StorageAuthenticationSettings fromConnectionSettings(final ConnectionSettings settings) {
        final String accountName = settings.getSettingValue(StorageConstants.ConnectionStringConstants.ACCOUNT_NAME);
        final String accountKey = settings.getSettingValue(StorageConstants.ConnectionStringConstants.ACCOUNT_KEY_NAME);
        final String sasSignature =
            settings.getSettingValue(StorageConstants.ConnectionStringConstants.SHARED_ACCESS_SIGNATURE_NAME);

        if (accountName != null && accountKey != null && sasSignature == null) {
            return new StorageAuthenticationSettings(new Account(accountName, accountKey));
        }
        if (accountKey == null && sasSignature != null) {
            return new StorageAuthenticationSettings(sasSignature);
        }

        return new StorageAuthenticationSettings();
    }

    /**
     * @return A {@link StorageAuthenticationSettings} for emulator usage.
     */
    public static StorageAuthenticationSettings forEmulator() {
        return new StorageAuthenticationSettings(
            new StorageAuthenticationSettings.Account(
                StorageConstants.ConnectionStringConstants.EMULATOR_ACCOUNT_NAME,
                StorageConstants.ConnectionStringConstants.EMULATOR_ACCOUNT_KEY));
    }

    /**
     * Creates a default {@link StorageAuthenticationSettings} indicating the absence of authentication settings.
     */
    private StorageAuthenticationSettings() {
        this.type = Type.NONE;
        this.account = null;
        this.sasToken = null;
    }

    /**
     * Creates a {@link StorageAuthenticationSettings} indicating SAS token based authentication settings.
     *
     * @param sasToken The SAS token.
     */
    private StorageAuthenticationSettings(String sasToken) {
        this.type = Type.SAS_TOKEN;
        this.sasToken = Objects.requireNonNull(sasToken);
        this.account = null;
    }

    /**
     * Creates a {@link StorageAuthenticationSettings} indicating account name and key based authentication settings.
     *
     * @param account The account instance holding account name and key.
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
        private final String name;
        private final String accessKey;

        /**
         * Creates an {@link Account}.
         *
         * @param name The storage account name.
         * @param accessKey The storage access key.
         */
        private Account(String name, String accessKey) {
            this.name = Objects.requireNonNull(name);
            this.accessKey = Objects.requireNonNull(accessKey);
        }

        /**
         * @return The storage account name.
         */
        public String getName() {
            return this.name;
        }

        /**
         * @return The storage account access key.
         */
        public String getAccessKey() {
            return this.accessKey;
        }
    }
}
