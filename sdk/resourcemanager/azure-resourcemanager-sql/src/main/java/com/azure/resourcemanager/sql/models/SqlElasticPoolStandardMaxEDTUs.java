// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;

/** The maximum limit of the reserved eDTUs value range for a "Standard" edition of an Azure SQL Elastic Pool. */
@Fluent
public enum SqlElasticPoolStandardMaxEDTUs {
    /** Maximum 10 eDTUs available per each database. */
    eDTU_10(10),

    /** Maximum 20 eDTUs available per each database. */
    eDTU_20(20),

    /** Maximum 50 eDTUs available per each database. */
    eDTU_50(50),

    /** Maximum 100 eDTUs available per each database. */
    eDTU_100(100),

    /** Maximum 200 eDTUs available per each database. */
    eDTU_200(200),

    /** Maximum 300 eDTUs available per each database. */
    eDTU_300(300),

    /** Maximum 400 eDTUs available per each database. */
    eDTU_400(400),

    /** Maximum 800 eDTUs available per each database. */
    eDTU_800(800),

    /** Maximum 1200 eDTUs available per each database. */
    eDTU_1200(1200),

    /** Maximum 1600 eDTUs available per each database. */
    eDTU_1600(1600),

    /** Maximum 2000 eDTUs available per each database. */
    eDTU_2000(2000),

    /** Maximum 2500 eDTUs available per each database. */
    eDTU_2500(2500),

    /** Maximum 3000 eDTUs available per each database. */
    eDTU_3000(3000);

    /** The maximum eDTUs available per each database for the SQL Elastic Pool. */
    private int value;

    SqlElasticPoolStandardMaxEDTUs(int eDTU) {
        this.value = eDTU;
    }

    /** @return the maximum eDTUs available per each database for the SQL Elastic Pool */
    public int value() {
        return this.value;
    }
}
