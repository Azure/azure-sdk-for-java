// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.sas;

import com.azure.core.annotation.Fluent;
import com.azure.data.tables.implementation.StorageConstants;

import java.util.Locale;

/**
 * This is a helper class to construct a string representing the resources accessible by an Account SAS. Setting a value
 * to true means that any SAS which uses these permissions will grant access to that resource type. Once all the values
 * are set, this should be serialized with {@code toString()} and set as the resources field on an
 * {@link TableAccountSasSignatureValues} object. It is possible to construct the resources string without this class,
 * but the order of the resources is particular and this class guarantees correctness.
 */
@Fluent
public final class TableAccountSasResourceType {
    private boolean service;
    private boolean container;
    private boolean object;

    /**
     * Creates an {@link TableAccountSasResourceType} from the specified resource types string. This method will throw an
     * {@link IllegalArgumentException} if it encounters a character that does not correspond to a valid resource type.
     *
     * @param resourceTypesString A {@code String} which represents the
     * {@link TableAccountSasResourceType account resource types}.
     *
     * @return A {@link TableAccountSasResourceType} generated from the given {@code String}.
     *
     * @throws IllegalArgumentException If {@code resourceTypesString} contains a character other than s, c, or o.
     */
    public static TableAccountSasResourceType parse(String resourceTypesString) {
        TableAccountSasResourceType resourceType = new TableAccountSasResourceType();

        for (int i = 0; i < resourceTypesString.length(); i++) {
            char c = resourceTypesString.charAt(i);

            switch (c) {
                case 's':
                    resourceType.service = true;
                    break;
                case 'c':
                    resourceType.container = true;
                    break;
                case 'o':
                    resourceType.object = true;
                    break;
                default:
                    throw new IllegalArgumentException(
                        String.format(Locale.ROOT, StorageConstants.ENUM_COULD_NOT_BE_PARSED_INVALID_VALUE,
                            "Resource Types", resourceTypesString, c));
            }
        }

        return resourceType;
    }

    /**
     * Get the access status for service level APIs.
     *
     * @return The access status for service level APIs.
     */
    public boolean isService() {
        return service;
    }

    /**
     * Sets the access status for service level APIs.
     *
     * @param service The access status to set.
     *
     * @return The updated {@link TableAccountSasResourceType} object.
     */
    public TableAccountSasResourceType setService(boolean service) {
        this.service = service;

        return this;
    }

    /**
     * Gets the access status for container level APIs, this grants access to Blob Containers, Tables, Queues, and
     * File Shares.
     *
     * @return The access status for container level APIs, this grants access to Blob Containers, Tables, Queues, and
     * File Shares.
     */
    public boolean isContainer() {
        return container;
    }

    /**
     * Sets the access status for container level APIs, this grants access to Blob Containers, Tables, Queues, and File
     * Shares.
     *
     * @param container The access status to set.
     *
     * @return The updated {@link TableAccountSasResourceType} object.
     */
    public TableAccountSasResourceType setContainer(boolean container) {
        this.container = container;

        return this;
    }

    /**
     * Get the access status for object level APIs, this grants access to Blobs, Table Entities, Queue Messages, Files.
     *
     * @return The access status for object level APIs, this grants access to Blobs, Table Entities, Queue Messages,
     * Files.
     */
    public boolean isObject() {
        return object;
    }

    /**
     * Sets the access status for object level APIs, this grants access to Blobs, Table Entities, Queue Messages,
     * Files.
     *
     * @param object The access status to set.
     *
     * @return The updated {@link TableAccountSasResourceType} object.
     */
    public TableAccountSasResourceType setObject(boolean object) {
        this.object = object;

        return this;
    }

    /**
     * Converts the given resource types to a {@code String}. Using this method will guarantee the resource types are in
     * an order accepted by the service. If all resource types are set to false, an empty string is returned from this
     * method.
     *
     * @return A {@code String} which represents the {@link TableAccountSasResourceType account resource types}.
     */
    @Override
    public String toString() {
        // The order of the characters should be as specified here to ensure correctness:
        // https://docs.microsoft.com/rest/api/storageservices/constructing-an-account-sas
        StringBuilder builder = new StringBuilder();

        if (this.service) {
            builder.append('s');
        }

        if (this.container) {
            builder.append('c');
        }

        if (this.object) {
            builder.append('o');
        }

        return builder.toString();
    }
}
