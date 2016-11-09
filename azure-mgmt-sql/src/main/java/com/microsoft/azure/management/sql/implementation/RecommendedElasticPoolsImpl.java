/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.sql.RecommendedElasticPool;
import com.microsoft.azure.management.sql.SqlServer;

/**
 * Implementation of SqlServer.RecommendedElasticPools, which enables the creating the recommended elastic pools from the SQLServer directly.
 */
public class RecommendedElasticPoolsImpl implements SqlServer.RecommendedElasticPools {

    private final String resourceGroupName;
    private final String sqlServerName;
    private final RecommendedElasticPoolsInner recommendedElasticPoolsInner;
    private DatabasesInner databasesInner;

    RecommendedElasticPoolsImpl(RecommendedElasticPoolsInner recommendedElasticPoolsInner,
                                DatabasesInner databasesInner,
                                String resourceGroupName,
                                String sqlServerName) {
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.recommendedElasticPoolsInner = recommendedElasticPoolsInner;
        this.databasesInner = databasesInner;
    }

    @Override
    public RecommendedElasticPool get(String recommendedElasticPoolName) {
        return new RecommendedElasticPoolImpl(
                this.recommendedElasticPoolsInner.get(this.resourceGroupName, this.sqlServerName, recommendedElasticPoolName),
                this.databasesInner,
                this.recommendedElasticPoolsInner);
    }

    @Override
    public PagedList<RecommendedElasticPool> list() {
        final RecommendedElasticPoolsImpl self = this;

        PagedListConverter<RecommendedElasticPoolInner, RecommendedElasticPool> converter = new PagedListConverter<RecommendedElasticPoolInner, RecommendedElasticPool>() {
            @Override
            public RecommendedElasticPool typeConvert(RecommendedElasticPoolInner recommendedElasticPoolInner) {
                return new RecommendedElasticPoolImpl(recommendedElasticPoolInner, self.databasesInner, self.recommendedElasticPoolsInner);
            }
        };
        return converter.convert(Utils.convertToPagedList(
                this.recommendedElasticPoolsInner.list(
                        this.resourceGroupName,
                        this.sqlServerName)));
    }
}
