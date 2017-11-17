/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.storage.AccountStatus;

/**
 * An instance of this class stores the availability of a storage account.
 */
@LangDefinition
public class AccountStatuses {
    private AccountStatus primary;
    private AccountStatus secondary;

    /**
     * Creates an instance of AccountStatuses class.
     * @param primary the status of the primary location
     * @param secondary the status of the secondary location
     */
    public AccountStatuses(AccountStatus primary, AccountStatus secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    /**
     * @return the status indicating whether the primary location of the storage
     * account is available or unavailable.
     */
    public AccountStatus primary() {
        return primary;
    }

    /**
     * @return the status indicating whether the secondary location of the
     * storage account is available or unavailable. Only available if the
     * accountType is StandardGRS or StandardRAGRS.
     */
    public AccountStatus secondary() {
        return secondary;
    }
}
