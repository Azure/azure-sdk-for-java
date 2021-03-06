/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.storage.v2019_04_01;

import com.microsoft.azure.arm.model.HasInner;
import com.microsoft.azure.arm.resources.models.HasManager;
import com.microsoft.azure.management.storage.v2019_04_01.implementation.StorageManager;
import com.microsoft.azure.management.storage.v2019_04_01.implementation.FileShareItemInner;
import org.joda.time.DateTime;
import java.util.Map;

/**
 * Type representing FileShareItem.
 */
public interface FileShareItem extends HasInner<FileShareItemInner>, HasManager<StorageManager> {
    /**
     * @return the etag value.
     */
    String etag();

    /**
     * @return the id value.
     */
    String id();

    /**
     * @return the lastModifiedTime value.
     */
    DateTime lastModifiedTime();

    /**
     * @return the metadata value.
     */
    Map<String, String> metadata();

    /**
     * @return the name value.
     */
    String name();

    /**
     * @return the shareQuota value.
     */
    Integer shareQuota();

    /**
     * @return the type value.
     */
    String type();

}
