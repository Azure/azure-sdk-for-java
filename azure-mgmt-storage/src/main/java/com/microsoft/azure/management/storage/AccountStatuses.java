package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.storage.implementation.api.AccountStatus;

public class AccountStatuses {
    private AccountStatus primary;
    private AccountStatus secondary;

    public AccountStatuses(AccountStatus primary, AccountStatus secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    /**
     * Gets the status indicating whether the primary location of the storage
     * account is available or unavailable. Possible values include:
     * 'Available', 'Unavailable'.
     */
    public AccountStatus primary() {
        return primary;
    }

    /**
     * Gets the status indicating whether the secondary location of the
     * storage account is available or unavailable. Only available if the
     * accountType is StandardGRS or StandardRAGRS. Possible values include:
     * 'Available', 'Unavailable'.
     */
    public AccountStatus secondary() {
        return secondary;
    }
}
