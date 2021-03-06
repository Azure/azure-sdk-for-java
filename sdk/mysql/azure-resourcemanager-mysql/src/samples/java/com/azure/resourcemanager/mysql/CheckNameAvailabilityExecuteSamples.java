// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.mysql;

import com.azure.core.util.Context;
import com.azure.resourcemanager.mysql.models.NameAvailabilityRequest;

/** Samples for CheckNameAvailability Execute. */
public final class CheckNameAvailabilityExecuteSamples {
    /**
     * Sample code: NameAvailability.
     *
     * @param mySqlManager Entry point to MySqlManager. The Microsoft Azure management API provides create, read,
     *     update, and delete functionality for Azure MySQL resources including servers, databases, firewall rules, VNET
     *     rules, log files and configurations with new business model.
     */
    public static void nameAvailability(com.azure.resourcemanager.mysql.MySqlManager mySqlManager) {
        mySqlManager
            .checkNameAvailabilities()
            .executeWithResponse(
                new NameAvailabilityRequest().withName("name1").withType("Microsoft.DBforMySQL"), Context.NONE);
    }
}
