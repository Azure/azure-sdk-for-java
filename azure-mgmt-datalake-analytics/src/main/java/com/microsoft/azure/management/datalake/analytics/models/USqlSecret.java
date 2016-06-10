/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Lake Analytics catalog U-SQL secret item.
 */
public class USqlSecret extends CatalogItem {
    /**
     * the name of the database.
     */
    private String databaseName;

    /**
     * the name of the secret.
     */
    @JsonProperty(value = "secretName")
    private String name;

    /**
     * the creation time of the credential object. This is the only
     * information returned about a secret from a GET.
     */
    private DateTime creationTime;

    /**
     * the URI identifier for the secret in the format
     * &lt;hostname&gt;:&lt;port&gt;.
     */
    private String uri;

    /**
     * the password for the secret to pass in.
     */
    private String password;

    /**
     * Get the databaseName value.
     *
     * @return the databaseName value
     */
    public String databaseName() {
        return this.databaseName;
    }

    /**
     * Set the databaseName value.
     *
     * @param databaseName the databaseName value to set
     * @return the USqlSecret object itself.
     */
    public USqlSecret withDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the USqlSecret object itself.
     */
    public USqlSecret withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the creationTime value.
     *
     * @return the creationTime value
     */
    public DateTime creationTime() {
        return this.creationTime;
    }

    /**
     * Set the creationTime value.
     *
     * @param creationTime the creationTime value to set
     * @return the USqlSecret object itself.
     */
    public USqlSecret withCreationTime(DateTime creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    /**
     * Get the uri value.
     *
     * @return the uri value
     */
    public String uri() {
        return this.uri;
    }

    /**
     * Set the uri value.
     *
     * @param uri the uri value to set
     * @return the USqlSecret object itself.
     */
    public USqlSecret withUri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Get the password value.
     *
     * @return the password value
     */
    public String password() {
        return this.password;
    }

    /**
     * Set the password value.
     *
     * @param password the password value to set
     * @return the USqlSecret object itself.
     */
    public USqlSecret withPassword(String password) {
        this.password = password;
        return this;
    }

}
