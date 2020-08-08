// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;

/** The maximum allowed storage capacity for a "Standard" edition of an Azure SQL Elastic Pool. */
@Fluent
public enum SqlDatabaseStandardStorage {
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

    /** 300 GB storage capacity available for the database (only with ServiceObjective "S3" or higher). */
    MAX_300_GB(300 * 1024),

    /** 400 GB storage capacity available for the database (only with ServiceObjective "S3" or higher). */
    MAX_400_GB(400 * 1024),

    /** 500 GB storage capacity available for the database (only with ServiceObjective "S3" or higher). */
    MAX_500_GB(500 * 1024),

    /** 750 GB storage capacity available for the database (only with ServiceObjective "S3" or higher). */
    MAX_750_GB(750 * 1024),

    /** 1 TB storage capacity available for the database (only with ServiceObjective "S3" or higher). */
    MAX_1_TB(1024 * 1024);

    /** The maximum allowed storage capacity in MB for the SQL Database. */
    private long capacityInMB;

    SqlDatabaseStandardStorage(long capacityInMB) {
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
