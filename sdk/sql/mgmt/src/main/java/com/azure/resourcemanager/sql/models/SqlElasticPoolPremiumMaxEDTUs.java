// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;

/** The maximum limit of the reserved eDTUs value range for a "Premium" edition of an Azure SQL Elastic Pool. */
@Fluent
public enum SqlElasticPoolPremiumMaxEDTUs {
    /** Maximum 25 eDTUs available per each database. */
    eDTU_25(25),

    /** Maximum 50 eDTUs available per each database. */
    eDTU_50(50),

    /** Maximum 75 eDTUs available per each database. */
    eDTU_75(75),

    /** Maximum 125 eDTUs available per each database. */
    eDTU_125(125),

    /** Maximum 250 eDTUs available per each database. */
    eDTU_250(250),

    /** Maximum 500 eDTUs available per each database. */
    eDTU_500(500),

    /** Maximum 1000 eDTUs available per each database. */
    eDTU_1000(1000),

    /** Maximum 1750 eDTUs available per each database. */
    eDTU_1750(1750),

    /** Maximum 4000 eDTUs available per each database. */
    eDTU_4000(4000);

    /** The maximum eDTUs available per each database for the SQL Elastic Pool. */
    private int value;

    SqlElasticPoolPremiumMaxEDTUs(int eDTU) {
        this.value = eDTU;
    }

    /** @return the maximum eDTUs available per each database for the SQL Elastic Pool */
    public int value() {
        return this.value;
    }
}
