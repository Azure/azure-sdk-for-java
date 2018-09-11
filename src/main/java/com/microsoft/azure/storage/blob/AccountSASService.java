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

import java.util.Locale;

/**
 * This is a helper class to construct a string representing the services accessible by an AccountSAS. Setting a value
 * to true means that any SAS which uses these permissions will grant access to that service. Once all the
 * values are set, this should be serialized with toString and set as the services field on an
 * {@link AccountSASSignatureValues} object. It is possible to construct the services string without this class, but
 * the order of the services is particular and this class guarantees correctness.
 */
public final class AccountSASService {

    private boolean blob;

    private boolean file;

    private boolean queue;

    private boolean table;

    /**
     * Permission to access blob resources granted.
     */
    public boolean blob() {
        return blob;
    }

    /**
     * Permission to access blob resources granted.
     */
    public AccountSASService withBlob(boolean blob) {
        this.blob = blob;
        return this;
    }

    /**
     * Permission to access file resources granted.
     */
    public boolean file() {
        return file;
    }

    /**
     * Permission to access file resources granted.
     */
    public AccountSASService withFile(boolean file) {
        this.file = file;
        return this;
    }

    /**
     * Permission to access queue resources granted.
     */
    public boolean queue() {
        return queue;
    }

    /**
     * Permission to access queue resources granted.
     */
    public AccountSASService withQueue(boolean queue) {
        this.queue = queue;
        return this;
    }

    /**
     * Permission to access table resources granted.
     */
    public boolean table() {
        return table;
    }

    /**
     * Permission to access table resources granted.
     */
    public AccountSASService withTable(boolean table) {
        this.table = table;
        return this;
    }

    /**
     * Initializes an {@code AccountSASService} object with all fields set to false.
     */
    public AccountSASService() {}

    /**
     * Converts the given services to a {@code String}. Using this method will guarantee the services are in an order
     * accepted by the service.
     *
     * @return
     *      A {@code String} which represents the {@code AccountSASServices}.
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

    /**
     * Creates an {@code AccountSASService} from the specified services string. This method will throw an
     * {@code IllegalArgumentException} if it encounters a character that does not correspond to a valid service.
     *
     * @param servicesString
     *      A {@code String} which represents the {@code SharedAccessAccountServices}.
     * @return
     *      A {@code AccountSASService} generated from the given {@code String}.
     */
    public static AccountSASService parse(String servicesString) {
        AccountSASService services = new AccountSASService();

        for (int i=0; i < servicesString.length(); i++) {
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
                        String.format(Locale.ROOT, SR.ENUM_COULD_NOT_BE_PARSED_INVALID_VALUE, "Services",
                                servicesString, c));
            }
        }
        return services;
    }
}
