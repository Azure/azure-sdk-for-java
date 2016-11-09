/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.sql.ServerUpgradeResult;
import org.joda.time.DateTime;

/**
 * Implementation for Restore point interface.
 */
@LangDefinition
class ServerUpgradeResultImpl
        extends WrapperImpl<ServerUpgradeGetResultInner>
        implements ServerUpgradeResult {
    protected ServerUpgradeResultImpl(ServerUpgradeGetResultInner innerObject) {
        super(innerObject);
    }

    @Override
    public String status() {
        return this.inner().status();
    }

    @Override
    public DateTime scheduleUpgradeAfterTime() {
        return this.inner().scheduleUpgradeAfterTime();
    }
}
