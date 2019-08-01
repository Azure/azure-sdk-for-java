// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import java.util.Locale;

/**
 * This is a helper class to construct a string representing the resources accessible by an AccountSAS. Setting a value
 * to true means that any SAS which uses these permissions will grant access to that resource type. Once all the
 * values are set, this should be serialized with toString and set as the resources field on an
 * {@link AccountSASSignatureValues} object. It is possible to construct the resources string without this class, but
 * the order of the resources is particular and this class guarantees correctness.
 */
public final class AccountSASResourceType {

    private boolean service;

    private boolean container;

    private boolean object;

    /**
     * Initializes an {@code AccountSASResourceType} object with all fields set to false.
     */
    public AccountSASResourceType() {
    }

    /**
     * Creates an {@code AccountSASResourceType} from the specified resource types string. This method will throw an
     * {@code IllegalArgumentException} if it encounters a character that does not correspond to a valid resource type.
     *
     * @param resourceTypesString
     *         A {@code String} which represents the {@code AccountSASResourceTypes}.
     *
     * @return A {@code AccountSASResourceType} generated from the given {@code String}.
     * @throws IllegalArgumentException If {@code resourceTypesString} contains a character other than s, c, or o.
     */
    public static AccountSASResourceType parse(String resourceTypesString) {
        AccountSASResourceType resourceType = new AccountSASResourceType();

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
                            String.format(Locale.ROOT, SR.ENUM_COULD_NOT_BE_PARSED_INVALID_VALUE,
                                    "Resource Types", resourceTypesString, c));
            }
        }
        return resourceType;
    }

    /**
     * @return the access status for service level APIs.
     */
    public boolean service() {
        return service;
    }

    /**
     * Sets the access status for service level APIs.
     *
     * @param service Access status to set
     * @return the updated AccountSASResourceType object.
     */
    public AccountSASResourceType service(boolean service) {
        this.service = service;
        return this;
    }

    /**
     * @return the access status for container level APIs, this grants access to Blob Containers, Tables, Queues,
     * and File Shares.
     */
    public boolean container() {
        return container;
    }

    /**
     * Sets the access status for container level APIs, this grants access to Blob Containers, Tables, Queues, and File Shares.
     *
     * @param container Access status to set
     * @return the updated AccountSASResourceType object.
     */
    public AccountSASResourceType container(boolean container) {
        this.container = container;
        return this;
    }

    /**
     * @return the access status for object level APIs, this grants access to Blobs, Table Entities, Queue Messages,
     * Files.
     */
    public boolean object() {
        return object;
    }

    /**
     * Sets the access status for object level APIs, this grants access to Blobs, Table Entities, Queue Messages, Files.
     *
     * @param object Access status to set
     * @return the updated AccountSASResourceType object.
     */
    public AccountSASResourceType object(boolean object) {
        this.object = object;
        return this;
    }

    /**
     * Converts the given resource types to a {@code String}. Using this method will guarantee the resource types are in
     * an order accepted by the service.
     *
     * @return A {@code String} which represents the {@code AccountSASResourceTypes}.
     */
    @Override
    public String toString() {
        // The order of the characters should be as specified here to ensure correctness:
        // https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-an-account-sas
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
