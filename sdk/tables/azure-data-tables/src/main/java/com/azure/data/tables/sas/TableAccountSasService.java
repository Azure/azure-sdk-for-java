// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.sas;

import com.azure.core.annotation.Fluent;
import com.azure.data.tables.implementation.StorageConstants;

import java.util.Locale;

/**
 * This is a helper class to construct a string representing the services accessible by an Account SAS. Setting a value
 * to true means that any SAS which uses these permissions will grant access to that service. Once all the values are
 * set, this should be serialized with {@code toString()} and set as the services field on an
 * {@link TableAccountSasSignatureValues} object. It is possible to construct the services string without this class, but
 * the order of the services is particular and this class guarantees correctness.
 */
@Fluent
public final class TableAccountSasService {
    private boolean blob;
    private boolean file;
    private boolean queue;
    private boolean table;

    /**
     * Creates an {@link TableAccountSasService} from the specified services string. This method will throw an
     * {@link IllegalArgumentException} if it encounters a character that does not correspond to a valid service.
     *
     * @param servicesString A {@code String} which represents the {@link TableAccountSasService account services}.
     *
     * @return A {@link TableAccountSasService} generated from the given {@code String}.
     *
     * @throws IllegalArgumentException If {@code servicesString} contains a character other than b, f, q, or t.
     */
    public static TableAccountSasService parse(String servicesString) {
        TableAccountSasService services = new TableAccountSasService();

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
                        String.format(Locale.ROOT, StorageConstants.ENUM_COULD_NOT_BE_PARSED_INVALID_VALUE, "Services",
                            servicesString, c));
            }
        }

        return services;
    }

    /**
     * @return The access status for blob resources.
     */
    public boolean hasBlobAccess() {
        return blob;
    }

    /**
     * Sets the access status for blob resources.
     *
     * @param blob The access status to set.
     *
     * @return The updated {@link TableAccountSasService} object.
     */
    public TableAccountSasService setBlobAccess(boolean blob) {
        this.blob = blob;

        return this;
    }

    /**
     * @return The access status for file resources.
     */
    public boolean hasFileAccess() {
        return file;
    }

    /**
     * Sets the access status for file resources.
     *
     * @param file The access status to set.
     *
     * @return The updated {@link TableAccountSasService} object.
     */
    public TableAccountSasService setFileAccess(boolean file) {
        this.file = file;

        return this;
    }

    /**
     * @return The access status for queue resources.
     */
    public boolean hasQueueAccess() {
        return queue;
    }

    /**
     * Sets the access status for queue resources.
     *
     * @param queue The access status to set.
     *
     * @return The updated {@link TableAccountSasService} object.
     */
    public TableAccountSasService setQueueAccess(boolean queue) {
        this.queue = queue;

        return this;
    }

    /**
     * @return The access status for table resources.
     */
    public boolean hasTableAccess() {
        return table;
    }

    /**
     * Sets the access status for table resources.
     *
     * @param table The access status to set.
     *
     * @return The updated {@link TableAccountSasService} object.
     */
    public TableAccountSasService setTableAccess(boolean table) {
        this.table = table;

        return this;
    }

    /**
     * Converts the given services to a {@code String}. Using this method will guarantee the services are in an order
     * accepted by the service. If all services are set to false, an empty string is returned from this method.
     *
     * @return A {@code String} which represents the {@link TableAccountSasService account services}.
     */
    @Override
    public String toString() {
        // The order of the characters should be as specified here to ensure correctness:
        // https://docs.microsoft.com/rest/api/storageservices/constructing-an-account-sas
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
