// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;

/** The maximum allowed storage capacity for a "Standard" edition of an Azure SQL Elastic Pool. */
@Fluent
public enum SqlElasticPoolStandardStorage {
    /** 50 GB storage capacity available to the pool. */
    MAX_50_GB(50),

    /** 100 GB storage capacity available to the pool. */
    MAX_100_GB(100),

    /** 150 GB storage capacity available to the pool. */
    MAX_150_GB(150),

    /** 200 GB storage capacity available to the pool. */
    MAX_200_GB(200),

    /** 250 GB storage capacity available to the pool. */
    MAX_250_GB(250),

    /** 300 GB storage capacity available to the pool. */
    MAX_300_GB(300),

    /** 400 GB storage capacity available to the pool. */
    MAX_400_GB(400),

    /** 500 GB storage capacity available to the pool. */
    MAX_500_GB(500),

    /** 750 GB storage capacity available to the pool. */
    MAX_750_GB(750),

    /** 800 GB storage capacity available to the pool. */
    MAX_800_GB(800),

    /** 1024 GB storage capacity available to the pool. */
    MAX_1024_GB(1024),

    /** 1200 GB storage capacity available to the pool. */
    MAX_1200_GB(1200),

    /** 1280 GB storage capacity available to the pool. */
    MAX_1280_GB(1280),

    /** 1536 GB storage capacity available to the pool. */
    MAX_1536_GB(1536),

    /** 1600 GB storage capacity available to the pool. */
    MAX_1600_GB(1600),

    /** 1792 GB storage capacity available to the pool. */
    MAX_1792_GB(1792),

    /** 2000 GB storage capacity available to the pool. */
    MAX_2000_GB(2000),

    /** 2048 GB storage capacity available to the pool. */
    MAX_2048_GB(2048),

    /** 2304 GB storage capacity available to the pool. */
    MAX_2304_GB(2304),

    /** 2500 GB storage capacity available to the pool. */
    MAX_2500_GB(2500),

    /** 2560 GB storage capacity available to the pool. */
    MAX_2560_GB(2560),

    /** 2816 GB storage capacity available to the pool. */
    MAX_2816_GB(2816),

    /** 3000 GB storage capacity available to the pool. */
    MAX_3000_GB(3000),

    /** 3072 GB storage capacity available to the pool. */
    MAX_3072_GB(3072),

    /** 3328 GB storage capacity available to the pool. */
    MAX_3328_GB(3328),

    /** 3584 GB storage capacity available to the pool. */
    MAX_3584_GB(3584),

    /** 3840 GB storage capacity available to the pool. */
    MAX_3840_GB(3840),

    /** 4096 GB storage capacity available to the pool. */
    MAX_4096_GB(4096);

    /** The maximum allowed storage capacity in GB for the SQL Elastic Pool. */
    private int capacityInGB;

    SqlElasticPoolStandardStorage(int capacityInGB) {
        this.capacityInGB = capacityInGB;
    }

    /** @return the maximum allowed storage capacity in MB for the SQL Elastic Pool */
    public int capacityInMB() {
        return this.capacityInGB * 1024;
    }
}
