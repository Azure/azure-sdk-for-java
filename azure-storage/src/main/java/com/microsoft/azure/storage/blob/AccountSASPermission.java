/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

/**
 * Represents possible permissions to be used for an Account SAS.
 */
public final class AccountSASPermission {
    /**
     * Permission to read resources and list queues and tables granted.
     */
    public boolean read;

    /**
     * Permission to add messages, table entities, and append to blobs granted.
     */
    public boolean add;

    /**
     * Permission to create blobs and files granted.
     */
    public boolean create;

    /**
     * Permission to write resources granted.
     */
    public boolean write;

    /**
     * Permission to delete resources granted.
     */
    public boolean delete;

    /**
     * Permission to list blob containers, blobs, shares, directories, and files granted.
     */
    public boolean list;

    /**
     * Permissions to update messages and table entities granted.
     */
    public boolean update;

    /**
     * Permission to get and delete messages granted.
     */
    public boolean processMessages;

    /**
     * Initializes an {@code AccountSASPermssion} object with all fields set to false.
     */
    public AccountSASPermission() {}

    /**
     * Converts the given permissions to a {@code String}. Using this method will guarantee the permissions are in an
     * order accepted by the service.
     *
     * @return
     *      A {@code String} which represents the {@code AccountSASPermissions}.
     */
    @Override
    public String toString() {
        // The order of the characters should be as specified here to ensure correctness:
        // https://docs.microsoft.com/en-us/rest/api/storageservices/constructing-an-account-sas
        final StringBuilder builder = new StringBuilder();

        if (this.read) {
            builder.append('r');
        }

        if (this.write) {
            builder.append('w');
        }

        if (this.delete) {
            builder.append('d');
        }

        if (this.list) {
            builder.append('l');
        }

        if (this.add) {
            builder.append('a');
        }

        if (this.create) {
            builder.append('c');
        }

        if (this.update) {
            builder.append('u');
        }

        if (this.processMessages) {
            builder.append('p');
        }

        return builder.toString();
    }

    /**
     * Creates an {@code AccountSASPermission} from the specified permissions string. This method will throw an
     * {@code IllegalArgumentException} if it encounters a character that does not correspond to a valid permission.
     *
     * @param permString
     *      A {@code String} which represents the {@code SharedAccessAccountPermissions}.
     * @return
     *      A {@code AccountSASPermission} generated from the given {@code String}.
     */
    public static AccountSASPermission parse(String permString) {
        AccountSASPermission permissions = new AccountSASPermission();

        for(int i=0; i<permString.length(); i++) {
            char c = permString.charAt(i);
            switch (c) {
                case 'r':
                    permissions.read = true;
                    break;
                case 'w':
                    permissions.write = true;
                    break;
                case 'd':
                    permissions.delete = true;
                    break;
                case 'l':
                    permissions.list = true;
                    break;
                case 'a':
                    permissions.add = true;
                    break;
                case 'c':
                    permissions.create = true;
                    break;
                case 'u':
                    permissions.update = true;
                    break;
                case 'p':
                    permissions.processMessages = true;
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format(SR.ENUM_COULD_NOT_BE_PARSED_INVALID_VALUE, "Permissions", permString, c));
            }
        }
        return permissions;
    }
}
