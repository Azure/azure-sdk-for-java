// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

import com.azure.core.util.ExpandableStringEnum;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Defines the audience for the Azure Table service.
 * <p>
 * This class is used to specify the audience when creating clients.
 * <p>
 * The audience can be one of the following:
 * <ul>
 *     <li>AZURE_STORAGE_PUBLIC_CLOUD</li>
 *     <li>AZURE_STORAGE_CHINAD</li>
 *     <li>AZURE_STORAGE_US_GOVERNMENT</li>
 *     <li>AZURE_COSMOS_PUBLIC_CLOUD</li>
 *     <li>AZURE_COSMOS_CHINA</li>
 *     <li>AZURE_COSMOS_US_GOVERNMENT</li>
 * </ul>
 */
public class TableAudience extends ExpandableStringEnum<TableAudience> {

    /**
     * The audience for the Azure Storage service in the public cloud.
     */
    public static final TableAudience AZURE_STORAGE_PUBLIC_CLOUD = fromString("https://storage.azure.com");

    /**
     * The audience for the Azure Storage service in China.
     */
    public static final TableAudience AZURE_STORAGE_CHINA = fromString("https://storage.azure.cn");

    /**
     * The audience for the Azure Storage service in the US government.
     */
    public static final TableAudience AZURE_STORAGE_US_GOVERNMENT = fromString("https://storage.azure.us");

    /**
     * The audience for the Azure Cosmos service in the public cloud.
     */
    public static final TableAudience AZURE_COSMOS_PUBLIC_CLOUD = fromString("https://cosmos.azure.com");
    /**
     * The audience for the Azure Cosmos service in China.
     */
    public static final TableAudience AZURE_COSMOS_CHINA = fromString("https://cosmos.azure.cn");
    /**
     * The audience for the Azure Cosmos service in the US government.
     */
    public static final TableAudience AZURE_COSMOS_US_GOVERNMENT = fromString("https://cosmos.azure.us");

    /**
     * @deprecated The audience is for the public.
     */
    @Deprecated
    public TableAudience() {
        // This constructor is deprecated and should not be used.
    }

    public String getDefaultScope() {
        try {
            URI uri = new URI(this.toString());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme != null && host != null) {
                return scheme + "://" + host + "/.default";
            } else {
                throw new IllegalArgumentException("Invalid scope: " + this.toString());
            }
        } catch (URISyntaxException e) {
            // Handle the exception
            throw new IllegalArgumentException("Invalid scope: " + this.toString());
        }
    }

    /**
     * Creates a new instance of TableAudience.
     *
     * @param audience The audience string.
     * @return A new instance of TableAudience.
     */
    public static TableAudience fromString(String audience) {
        return fromString(audience, TableAudience.class);
    }

}
