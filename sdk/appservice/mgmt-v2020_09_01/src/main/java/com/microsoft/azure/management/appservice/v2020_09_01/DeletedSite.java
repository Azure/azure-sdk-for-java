/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.appservice.v2020_09_01;

import com.microsoft.azure.arm.model.HasInner;
import com.microsoft.azure.management.appservice.v2020_09_01.implementation.DeletedSiteInner;
import com.microsoft.azure.arm.model.Indexable;
import com.microsoft.azure.arm.model.Refreshable;
import com.microsoft.azure.arm.resources.models.HasManager;
import com.microsoft.azure.management.appservice.v2020_09_01.implementation.AppServiceManager;

/**
 * Type representing DeletedSite.
 */
public interface DeletedSite extends HasInner<DeletedSiteInner>, Indexable, Refreshable<DeletedSite>, HasManager<AppServiceManager> {
    /**
     * @return the deletedSiteId value.
     */
    Integer deletedSiteId();

    /**
     * @return the deletedSiteKind value.
     */
    String deletedSiteKind();

    /**
     * @return the deletedSiteName value.
     */
    String deletedSiteName();

    /**
     * @return the deletedTimestamp value.
     */
    String deletedTimestamp();

    /**
     * @return the geoRegionName value.
     */
    String geoRegionName();

    /**
     * @return the id value.
     */
    String id();

    /**
     * @return the kind value.
     */
    String kind();

    /**
     * @return the name value.
     */
    String name();

    /**
     * @return the resourceGroup value.
     */
    String resourceGroup();

    /**
     * @return the slot value.
     */
    String slot();

    /**
     * @return the subscription value.
     */
    String subscription();

    /**
     * @return the systemData value.
     */
    SystemData systemData();

    /**
     * @return the type value.
     */
    String type();

}
