// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;

/** The maximum limit of the reserved eDTUs value range for a "Basic" edition of an Azure SQL Elastic Pool. */
@Fluent
public enum SqlElasticPoolBasicMaxEDTUs {
    /** Maximum 5 eDTUs available per each database. */
    eDTU_5(5);

    /** The maximum eDTUs available per each database for the SQL Elastic Pool. */
    private int value;

    SqlElasticPoolBasicMaxEDTUs(int eDTU) {
        this.value = eDTU;
    }

    /** @return the maximum eDTUs available per each database for the SQL Elastic Pool */
    public int value() {
        return this.value;
    }
}
