// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;

/** The minimum limit of the reserved eDTUs value range for a "Premium" edition of an Azure SQL Elastic Pool. */
@Fluent
public enum SqlElasticPoolPremiumMinEDTUs {
    /** 0 reserved minimum eDTUs available for each database in the pool. */
    eDTU_0(0),

    /** 0 reserved minimum eDTUs available for each database in the pool. */
    eDTU_25(25),

    /** 50 reserved minimum eDTUs available for each database in the pool. */
    eDTU_50(50),

    /** 75 reserved minimum eDTUs available for each database in the pool. */
    eDTU_75(75),

    /** 125 reserved minimum eDTUs available for each database in the pool. */
    eDTU_125(125),

    /** 250 reserved minimum eDTUs available for each database in the pool. */
    eDTU_250(250),

    /** 500 reserved minimum eDTUs available for each database in the pool. */
    eDTU_500(500),

    /** 1000 reserved minimum eDTUs available for each database in the pool. */
    eDTU_1000(1000),

    /** 1750 reserved minimum eDTUs available for each database in the pool. */
    eDTU_1750(1750),

    /** 4000 reserved minimum eDTUs available for each database in the pool. */
    eDTU_4000(4000);

    /** The reserved minimum eDTU for each database in the SQL Elastic Pool. */
    private int value;

    SqlElasticPoolPremiumMinEDTUs(int eDTU) {
        this.value = eDTU;
    }

    /** @return the reserved minimum eDTU for each database in the SQL Elastic Pool */
    public int value() {
        return this.value;
    }
}
