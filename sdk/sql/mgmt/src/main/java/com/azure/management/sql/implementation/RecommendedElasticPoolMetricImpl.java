/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.sql.implementation;

import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.sql.RecommendedElasticPoolMetric;
import com.azure.management.sql.models.RecommendedElasticPoolMetricInner;

import java.time.OffsetDateTime;

/**
 * Implementation for RecommendedElasticPoolMetric interface.
 */
class RecommendedElasticPoolMetricImpl
        extends WrapperImpl<RecommendedElasticPoolMetricInner>
        implements RecommendedElasticPoolMetric {

    protected RecommendedElasticPoolMetricImpl(RecommendedElasticPoolMetricInner innerObject) {
        super(innerObject);
    }

    @Override
    public OffsetDateTime dateTime() {
        return this.inner().dateTime();
    }

    @Override
    public double dtu() {
        return this.inner().dtu();
    }

    @Override
    public double sizeGB() {
        return this.inner().sizeGB();
    }
}
