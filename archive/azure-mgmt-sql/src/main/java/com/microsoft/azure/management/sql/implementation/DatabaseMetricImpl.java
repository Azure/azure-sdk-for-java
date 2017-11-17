/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.sql.DatabaseMetric;
import org.joda.time.DateTime;

/**
 * Implementation for DatabaseMetric interface.
 */
@LangDefinition
class DatabaseMetricImpl
        extends WrapperImpl<DatabaseMetricInner>
        implements DatabaseMetric {

    protected DatabaseMetricImpl(DatabaseMetricInner innerObject) {
        super(innerObject);
    }

    @Override
    public String resourceName() {
        return this.inner().resourceName();
    }

    @Override
    public String displayName() {
        return this.inner().displayName();
    }

    @Override
    public double currentValue() {
        return this.inner().currentValue();
    }

    @Override
    public double limit() {
        return this.inner().limit();
    }

    @Override
    public String unit() {
        return this.inner().unit();
    }

    @Override
    public DateTime nextResetTime() {
        return this.inner().nextResetTime();
    }
}
