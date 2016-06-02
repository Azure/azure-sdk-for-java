/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation;


import com.microsoft.azure.management.storage.implementation.api.CheckNameAvailabilityResultInner;
import com.microsoft.azure.management.storage.implementation.api.Reason;

/**
 * The CheckNameAvailability operation response.
 */
public class CheckNameAvailabilityResult {
    private final CheckNameAvailabilityResultInner inner;

    /**
     * Creates an instance of CheckNameAvailabilityResult object.
     *
     * @param inner the inner result object
     */
    public CheckNameAvailabilityResult(CheckNameAvailabilityResultInner inner) {
        this.inner = inner;
    }

    /**
     * @return a boolean value that indicates whether the name is available for
     * you to use. If true, the name is available. If false, the name has
     * already been taken or invalid and cannot be used.
     */
    public Boolean isAvailable() {
        return inner.nameAvailable();
    }

    /**
     * @return the reason that a storage account name could not be used. The
     * Reason element is only returned if NameAvailable is false. Possible
     * values include: 'AccountNameInvalid', 'AlreadyExists'.
     */
    public Reason reason() {
        return inner.reason();
    }

    /**
     * @return an error message explaining the Reason value in more detail
     */
    public String message() {
        return inner.message();
    }
}
