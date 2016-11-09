/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.sql.implementation.ServerUpgradeGetResultInner;
import org.joda.time.DateTime;


/**
 * An immutable client-side representation of an Azure SQL upgrade result.
 */
@Fluent
public interface ServerUpgradeResult extends
        Wrapper<ServerUpgradeGetResultInner> {
    /**
     * @return the status of the Azure SQL Server Upgrade
     */
    String status();

    /**
     * @return the schedule time of the Azure SQL Server Upgrade (ISO8601 format).
     */
    DateTime scheduleUpgradeAfterTime();
}

