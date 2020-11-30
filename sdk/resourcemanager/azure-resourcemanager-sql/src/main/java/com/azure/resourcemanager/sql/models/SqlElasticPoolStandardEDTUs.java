// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;

/** The reserved eDTUs value range for a "Standard" edition of an Azure SQL Elastic Pool. */
@Fluent
public enum SqlElasticPoolStandardEDTUs {
    /** 50 eDTUs available to the pool. */
    eDTU_50(50),

    /** 100 eDTUs available to the pool. */
    eDTU_100(100),

    /** 200 eDTUs available to the pool. */
    eDTU_200(200),

    /** 300 eDTUs available to the pool. */
    eDTU_300(300),

    /** 400 eDTUs available to the pool. */
    eDTU_400(400),

    /** 800 eDTUs available to the pool. */
    eDTU_800(800),

    /** 1200 eDTUs available to the pool. */
    eDTU_1200(1200),

    /** 1600 eDTUs available to the pool. */
    eDTU_1600(1600),

    /** 2000 eDTUs available to the pool. */
    eDTU_2000(2000),

    /** 2500 eDTUs available to the pool. */
    eDTU_2500(2500),

    /** 3000 eDTUs available to the pool. */
    eDTU_3000(3000);

    /** The reserved eDTU for the SQL Elastic Pool. */
    private int value;

    SqlElasticPoolStandardEDTUs(int eDTU) {
        this.value = eDTU;
    }

    /** @return the reserved eDTU for the SQL Elastic Pool */
    public int value() {
        return this.value;
    }
}
