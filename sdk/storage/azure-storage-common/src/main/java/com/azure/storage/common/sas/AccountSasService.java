// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.sas;

import com.azure.storage.common.implementation.Constants;

import java.util.Locale;

/**
 * This is a helper class to construct a string representing the services accessible by an AccountSAS. Setting a value
 * to true means that any SAS which uses these permissions will grant access to that service. Once all the
 * values are set, this should be serialized with toString and set as the services field on an
 * {@link AccountSasSignatureValues} object. It is possible to construct the services string without this class, but
 * the order of the services is particular and this class guarantees correctness.
 */
public final class AccountSasService {

    private boolean blob;

    private boolean file;

    private boolean queue;

    private boolean table;

    /**
     * Initializes an {@code AccountSasService} object with all fields set to false.
     */
    public AccountSasService() {
    }

    /**
     * Creates an {@code AccountSasService} from the specified services string. This method will throw an
     * {@code IllegalArgumentException} if it encounters a character that does not correspond to a valid service.
     *
     * @param servicesString A {@code String} which represents the {@code SharedAccessAccountServices}.
     *
     * @return A {@code AccountSasService} generated from the given {@code String}.
     * @throws IllegalArgumentException If {@code servicesString} contains a character other than b, f, q, or t.
     */
    public static AccountSasService parse(String servicesString) {
        AccountSasService services = new AccountSasService();

        for (int i = 0; i < servicesString.length(); i++) {
            char c = servicesString.charAt(i);
            switch (c) {
                case 'b':
                    services.blob = true;
                    break;
                case 'f':
                    services.file = true;
                    break;
                case 'q':
                    services.queue = true;
                    break;
                case 't':
                    services.table = true;
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format(Locale.ROOT, Constants.ENUM_COULD_NOT_BE_PARSED_INVALID_VALUE, "Services",
                                    servicesString, c));
            }
        }
        return services;
    }

    /**
     * @return the access status for blob resources.
     */
    public boolean hasBlobAccess() {
        return blob;
    }

    /**
     * Sets the access status for blob resources.
     *
     * @param blob Access status to set
     * @return the updated AccountSasService object.
     */
    public AccountSasService setBlobAccess(boolean blob) {
        this.blob = blob;
        return this;
    }

    /**
     * @return the access status for file resources.
     */
    public boolean hasFileAccess() {
        return file;
    }

    /**
     * Sets the access status for file resources.
     *
     * @param file Access status to set
     * @return the updated AccountSasService object.
     */
    public AccountSasService setFileAccess(boolean file) {
        this.file = file;
        return this;
    }

    /**
     * @return the access status for queue resources.
     */
    public boolean hasQueueAccess() {
        return queue;
    }

    /**
     * Sets the access status for queue resources.
     *
     * @param queue Access status to set
     * @return the updated AccountSasService object.
     */
    public AccountSasService setQueueAccess(boolean queue) {
        this.queue = queue;
        return this;
    }

    /**
     * @return the access status for table resources.
     */
    public boolean hasTableAccess() {
        return table;
    }

    /**
     * Sets the access status for table resources.
     *
     * @param table Access status to set
     * @return the updated AccountSasService object.
     */
    public AccountSasService setTableAccess(boolean table) {
        this.table = table;
        return this;
    }

    /**
     * Converts the given services to a {@code String}. Using this method will guarantee the services are in an order
     * accepted by the service.
     *
     * @return A {@code String} which represents the {@code AccountSASServices}.
     */
    @Override
    public String toString() {
        // The order of the characters should be as specified here to ensure correctness:
        // https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-an-account-sas
        StringBuilder value = new StringBuilder();

        if (this.blob) {
            value.append('b');
        }
        if (this.queue) {
            value.append('q');
        }
        if (this.table) {
            value.append('t');
        }
        if (this.file) {
            value.append('f');
        }

        return value.toString();
    }
}
