// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;

/** The maximum allowed storage capacity for a "Premium" edition of an Azure SQL Elastic Pool. */
@Fluent
public enum SqlDatabasePremiumStorage {
    /** 100 MB storage capacity available for the database. */
    MAX_100_MB(100),

    /** 100 MB storage capacity available for the database. */
    MAX_500_MB(500),

    /** 1 GB storage capacity available for the database. */
    MAX_1_GB(1024),

    /** 2 GB storage capacity available for the database. */
    MAX_2_GB(2 * 1024),

    /** 5 GB storage capacity available for the database. */
    MAX_5_GB(5 * 1024),

    /** 10 GB storage capacity available for the database. */
    MAX_10_GB(10 * 1024),

    /** 20 GB storage capacity available for the database. */
    MAX_20_GB(20 * 1024),

    /** 30 GB storage capacity available for the database. */
    MAX_30_GB(30 * 1024),

    /** 40 GB storage capacity available for the database. */
    MAX_40_GB(40 * 1024),

    /** 50 GB storage capacity available for the database. */
    MAX_50_GB(50 * 1024),

    /** 100 GB storage capacity available for the database. */
    MAX_100_GB(100 * 1024),

    /** 150 GB storage capacity available for the database. */
    MAX_150_GB(150 * 1024),

    /** 200 GB storage capacity available for the database. */
    MAX_200_GB(200 * 1024),

    /** 250 GB storage capacity available for the database. */
    MAX_250_GB(250 * 1024),

    /** 300 GB storage capacity available for the database. */
    MAX_300_GB(300 * 1024),

    /** 400 GB storage capacity available for the database. */
    MAX_400_GB(400 * 1024),

    /** 500 GB storage capacity available for the database. */
    MAX_500_GB(500 * 1024),

    /** 750 GB storage capacity available for the database. */
    MAX_750_GB(750 * 1024),

    /** 1 TB storage capacity available for the database. */
    MAX_1_TB(1024 * 1024),

    /** 1280 GB storage capacity available for the database (only with ServiceObjective "P9" or higher). */
    MAX_1280_GB(1280 * 1024),

    /** 1536 GB storage capacity available for the database (only with ServiceObjective "P9" or higher). */
    MAX_1536_GB(1536 * 1024),

    /** 1792 GB storage capacity available for the database (only with ServiceObjective "P9" or higher). */
    MAX_1792_GB(1792 * 1024),

    /** 2 TB storage capacity available for the database (only with ServiceObjective "P9" or higher). */
    MAX_2_TB(2 * 1024 * 1024),

    /** 2304 GB storage capacity available for the database (only with ServiceObjective "P9" or higher). */
    MAX_2304_GB(2304 * 1024),

    /** 2560 GB storage capacity available for the database (only with ServiceObjective "P9" or higher). */
    MAX_2560_GB(2560 * 1024),

    /** 2816 GB storage capacity available for the database (only with ServiceObjective "P9" or higher). */
    MAX_2816_GB(2816 * 1024),

    /** 3 TB storage capacity available for the database (only with ServiceObjective "P9" or higher). */
    MAX_3_TB(3 * 1024 * 1024),

    /** 3328 GB storage capacity available for the database (only with ServiceObjective "P9" or higher). */
    MAX_3328_GB(3328 * 1024),

    /** 3584 GB storage capacity available for the database (only with ServiceObjective "P9" or higher). */
    MAX_3584_GB(3584 * 1024),

    /** 3840 GB storage capacity available for the database (only with ServiceObjective "P9" or higher). */
    MAX_3840_GB(3840 * 1024),

    /** 4 TB storage capacity available for the database (only with ServiceObjective "P9" or higher). */
    MAX_4_TB(4 * 1024 * 1024);

    /** The maximum allowed storage capacity in MB for the SQL Database. */
    private long capacityInMB;

    SqlDatabasePremiumStorage(long capacityInMB) {
        this.capacityInMB = capacityInMB;
    }

    /** @return the maximum allowed storage capacity in MB for the SQL Database */
    public long capacityInMB() {
        return this.capacityInMB;
    }

    /** @return the maximum allowed storage capacity in bytes for the SQL Database */
    public long capacity() {
        return this.capacityInMB * 1024 * 1024;
    }
}
