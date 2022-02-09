// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Defines SQL Elastic Pool Sku. */
@Fluent
public final class ElasticPoolSku {
    private static final ConcurrentMap<String, ElasticPoolSku> VALUES = new ConcurrentHashMap<>();

    /** Standard Edition with StandardPool_50 sku. */
    public static final ElasticPoolSku STANDARD_STANDARDPOOL_50 =
        new ElasticPoolSku("StandardPool", "Standard", null, 50, null);
    /** Standard Edition with StandardPool_100 sku. */
    public static final ElasticPoolSku STANDARD_STANDARDPOOL_100 =
        new ElasticPoolSku("StandardPool", "Standard", null, 100, null);
    /** Standard Edition with StandardPool_200 sku. */
    public static final ElasticPoolSku STANDARD_STANDARDPOOL_200 =
        new ElasticPoolSku("StandardPool", "Standard", null, 200, null);
    /** Standard Edition with StandardPool_300 sku. */
    public static final ElasticPoolSku STANDARD_STANDARDPOOL_300 =
        new ElasticPoolSku("StandardPool", "Standard", null, 300, null);
    /** Standard Edition with StandardPool_400 sku. */
    public static final ElasticPoolSku STANDARD_STANDARDPOOL_400 =
        new ElasticPoolSku("StandardPool", "Standard", null, 400, null);
    /** Standard Edition with StandardPool_800 sku. */
    public static final ElasticPoolSku STANDARD_STANDARDPOOL_800 =
        new ElasticPoolSku("StandardPool", "Standard", null, 800, null);
    /** Standard Edition with StandardPool_1200 sku. */
    public static final ElasticPoolSku STANDARD_STANDARDPOOL_1200 =
        new ElasticPoolSku("StandardPool", "Standard", null, 1200, null);
    /** Standard Edition with StandardPool_1600 sku. */
    public static final ElasticPoolSku STANDARD_STANDARDPOOL_1600 =
        new ElasticPoolSku("StandardPool", "Standard", null, 1600, null);
    /** Standard Edition with StandardPool_2000 sku. */
    public static final ElasticPoolSku STANDARD_STANDARDPOOL_2000 =
        new ElasticPoolSku("StandardPool", "Standard", null, 2000, null);
    /** Standard Edition with StandardPool_2500 sku. */
    public static final ElasticPoolSku STANDARD_STANDARDPOOL_2500 =
        new ElasticPoolSku("StandardPool", "Standard", null, 2500, null);
    /** Standard Edition with StandardPool_3000 sku. */
    public static final ElasticPoolSku STANDARD_STANDARDPOOL_3000 =
        new ElasticPoolSku("StandardPool", "Standard", null, 3000, null);
    /** Premium Edition with PremiumPool_125 sku. */
    public static final ElasticPoolSku PREMIUM_PREMIUMPOOL_125 =
        new ElasticPoolSku("PremiumPool", "Premium", null, 125, null);
    /** Premium Edition with PremiumPool_250 sku. */
    public static final ElasticPoolSku PREMIUM_PREMIUMPOOL_250 =
        new ElasticPoolSku("PremiumPool", "Premium", null, 250, null);
    /** Premium Edition with PremiumPool_500 sku. */
    public static final ElasticPoolSku PREMIUM_PREMIUMPOOL_500 =
        new ElasticPoolSku("PremiumPool", "Premium", null, 500, null);
    /** Premium Edition with PremiumPool_1000 sku. */
    public static final ElasticPoolSku PREMIUM_PREMIUMPOOL_1000 =
        new ElasticPoolSku("PremiumPool", "Premium", null, 1000, null);
    /** Premium Edition with PremiumPool_1500 sku. */
    public static final ElasticPoolSku PREMIUM_PREMIUMPOOL_1500 =
        new ElasticPoolSku("PremiumPool", "Premium", null, 1500, null);
    /** Premium Edition with PremiumPool_2000 sku. */
    public static final ElasticPoolSku PREMIUM_PREMIUMPOOL_2000 =
        new ElasticPoolSku("PremiumPool", "Premium", null, 2000, null);
    /** Premium Edition with PremiumPool_2500 sku. */
    public static final ElasticPoolSku PREMIUM_PREMIUMPOOL_2500 =
        new ElasticPoolSku("PremiumPool", "Premium", null, 2500, null);
    /** Premium Edition with PremiumPool_3000 sku. */
    public static final ElasticPoolSku PREMIUM_PREMIUMPOOL_3000 =
        new ElasticPoolSku("PremiumPool", "Premium", null, 3000, null);
    /** Premium Edition with PremiumPool_3500 sku. */
    public static final ElasticPoolSku PREMIUM_PREMIUMPOOL_3500 =
        new ElasticPoolSku("PremiumPool", "Premium", null, 3500, null);
    /** Premium Edition with PremiumPool_4000 sku. */
    public static final ElasticPoolSku PREMIUM_PREMIUMPOOL_4000 =
        new ElasticPoolSku("PremiumPool", "Premium", null, 4000, null);
    /** Basic Edition with BasicPool_50 sku. */
    public static final ElasticPoolSku BASIC_BASICPOOL_50 =
        new ElasticPoolSku("BasicPool", "Basic", null, 50, null);
    /** Basic Edition with BasicPool_100 sku. */
    public static final ElasticPoolSku BASIC_BASICPOOL_100 =
        new ElasticPoolSku("BasicPool", "Basic", null, 100, null);
    /** Basic Edition with BasicPool_200 sku. */
    public static final ElasticPoolSku BASIC_BASICPOOL_200 =
        new ElasticPoolSku("BasicPool", "Basic", null, 200, null);
    /** Basic Edition with BasicPool_300 sku. */
    public static final ElasticPoolSku BASIC_BASICPOOL_300 =
        new ElasticPoolSku("BasicPool", "Basic", null, 300, null);
    /** Basic Edition with BasicPool_400 sku. */
    public static final ElasticPoolSku BASIC_BASICPOOL_400 =
        new ElasticPoolSku("BasicPool", "Basic", null, 400, null);
    /** Basic Edition with BasicPool_800 sku. */
    public static final ElasticPoolSku BASIC_BASICPOOL_800 =
        new ElasticPoolSku("BasicPool", "Basic", null, 800, null);
    /** Basic Edition with BasicPool_1200 sku. */
    public static final ElasticPoolSku BASIC_BASICPOOL_1200 =
        new ElasticPoolSku("BasicPool", "Basic", null, 1200, null);
    /** Basic Edition with BasicPool_1600 sku. */
    public static final ElasticPoolSku BASIC_BASICPOOL_1600 =
        new ElasticPoolSku("BasicPool", "Basic", null, 1600, null);
    /** GeneralPurpose Edition with GP_Gen5_2 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_GEN5_2 =
        new ElasticPoolSku("GP_Gen5", "GeneralPurpose", "Gen5", 2, null);
    /** GeneralPurpose Edition with GP_Gen5_4 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_GEN5_4 =
        new ElasticPoolSku("GP_Gen5", "GeneralPurpose", "Gen5", 4, null);
    /** GeneralPurpose Edition with GP_Gen5_6 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_GEN5_6 =
        new ElasticPoolSku("GP_Gen5", "GeneralPurpose", "Gen5", 6, null);
    /** GeneralPurpose Edition with GP_Gen5_8 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_GEN5_8 =
        new ElasticPoolSku("GP_Gen5", "GeneralPurpose", "Gen5", 8, null);
    /** GeneralPurpose Edition with GP_Fsv2_8 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_FSV2_8 =
        new ElasticPoolSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 8, null);
    /** GeneralPurpose Edition with GP_Gen5_10 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_GEN5_10 =
        new ElasticPoolSku("GP_Gen5", "GeneralPurpose", "Gen5", 10, null);
    /** GeneralPurpose Edition with GP_Fsv2_10 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_FSV2_10 =
        new ElasticPoolSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 10, null);
    /** GeneralPurpose Edition with GP_Gen5_12 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_GEN5_12 =
        new ElasticPoolSku("GP_Gen5", "GeneralPurpose", "Gen5", 12, null);
    /** GeneralPurpose Edition with GP_Fsv2_12 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_FSV2_12 =
        new ElasticPoolSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 12, null);
    /** GeneralPurpose Edition with GP_Gen5_14 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_GEN5_14 =
        new ElasticPoolSku("GP_Gen5", "GeneralPurpose", "Gen5", 14, null);
    /** GeneralPurpose Edition with GP_Fsv2_14 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_FSV2_14 =
        new ElasticPoolSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 14, null);
    /** GeneralPurpose Edition with GP_Gen5_16 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_GEN5_16 =
        new ElasticPoolSku("GP_Gen5", "GeneralPurpose", "Gen5", 16, null);
    /** GeneralPurpose Edition with GP_Fsv2_16 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_FSV2_16 =
        new ElasticPoolSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 16, null);
    /** GeneralPurpose Edition with GP_Gen5_18 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_GEN5_18 =
        new ElasticPoolSku("GP_Gen5", "GeneralPurpose", "Gen5", 18, null);
    /** GeneralPurpose Edition with GP_Fsv2_18 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_FSV2_18 =
        new ElasticPoolSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 18, null);
    /** GeneralPurpose Edition with GP_Gen5_20 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_GEN5_20 =
        new ElasticPoolSku("GP_Gen5", "GeneralPurpose", "Gen5", 20, null);
    /** GeneralPurpose Edition with GP_Fsv2_20 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_FSV2_20 =
        new ElasticPoolSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 20, null);
    /** GeneralPurpose Edition with GP_Gen5_24 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_GEN5_24 =
        new ElasticPoolSku("GP_Gen5", "GeneralPurpose", "Gen5", 24, null);
    /** GeneralPurpose Edition with GP_Fsv2_24 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_FSV2_24 =
        new ElasticPoolSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 24, null);
    /** GeneralPurpose Edition with GP_Gen5_32 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_GEN5_32 =
        new ElasticPoolSku("GP_Gen5", "GeneralPurpose", "Gen5", 32, null);
    /** GeneralPurpose Edition with GP_Fsv2_32 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_FSV2_32 =
        new ElasticPoolSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 32, null);
    /** GeneralPurpose Edition with GP_Fsv2_36 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_FSV2_36 =
        new ElasticPoolSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 36, null);
    /** GeneralPurpose Edition with GP_Gen5_40 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_GEN5_40 =
        new ElasticPoolSku("GP_Gen5", "GeneralPurpose", "Gen5", 40, null);
    /** GeneralPurpose Edition with GP_Fsv2_72 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_FSV2_72 =
        new ElasticPoolSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 72, null);
    /** GeneralPurpose Edition with GP_Gen5_80 sku. */
    public static final ElasticPoolSku GENERALPURPOSE_GP_GEN5_80 =
        new ElasticPoolSku("GP_Gen5", "GeneralPurpose", "Gen5", 80, null);
    /** BusinessCritical Edition with BC_Gen5_4 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_GEN5_4 =
        new ElasticPoolSku("BC_Gen5", "BusinessCritical", "Gen5", 4, null);
    /** BusinessCritical Edition with BC_Gen5_6 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_GEN5_6 =
        new ElasticPoolSku("BC_Gen5", "BusinessCritical", "Gen5", 6, null);
    /** BusinessCritical Edition with BC_Gen5_8 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_GEN5_8 =
        new ElasticPoolSku("BC_Gen5", "BusinessCritical", "Gen5", 8, null);
    /** BusinessCritical Edition with BC_M_8 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_M_8 =
        new ElasticPoolSku("BC_M", "BusinessCritical", "M", 8, null);
    /** BusinessCritical Edition with BC_Gen5_10 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_GEN5_10 =
        new ElasticPoolSku("BC_Gen5", "BusinessCritical", "Gen5", 10, null);
    /** BusinessCritical Edition with BC_M_10 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_M_10 =
        new ElasticPoolSku("BC_M", "BusinessCritical", "M", 10, null);
    /** BusinessCritical Edition with BC_Gen5_12 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_GEN5_12 =
        new ElasticPoolSku("BC_Gen5", "BusinessCritical", "Gen5", 12, null);
    /** BusinessCritical Edition with BC_M_12 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_M_12 =
        new ElasticPoolSku("BC_M", "BusinessCritical", "M", 12, null);
    /** BusinessCritical Edition with BC_Gen5_14 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_GEN5_14 =
        new ElasticPoolSku("BC_Gen5", "BusinessCritical", "Gen5", 14, null);
    /** BusinessCritical Edition with BC_M_14 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_M_14 =
        new ElasticPoolSku("BC_M", "BusinessCritical", "M", 14, null);
    /** BusinessCritical Edition with BC_Gen5_16 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_GEN5_16 =
        new ElasticPoolSku("BC_Gen5", "BusinessCritical", "Gen5", 16, null);
    /** BusinessCritical Edition with BC_M_16 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_M_16 =
        new ElasticPoolSku("BC_M", "BusinessCritical", "M", 16, null);
    /** BusinessCritical Edition with BC_Gen5_18 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_GEN5_18 =
        new ElasticPoolSku("BC_Gen5", "BusinessCritical", "Gen5", 18, null);
    /** BusinessCritical Edition with BC_M_18 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_M_18 =
        new ElasticPoolSku("BC_M", "BusinessCritical", "M", 18, null);
    /** BusinessCritical Edition with BC_Gen5_20 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_GEN5_20 =
        new ElasticPoolSku("BC_Gen5", "BusinessCritical", "Gen5", 20, null);
    /** BusinessCritical Edition with BC_M_20 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_M_20 =
        new ElasticPoolSku("BC_M", "BusinessCritical", "M", 20, null);
    /** BusinessCritical Edition with BC_Gen5_24 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_GEN5_24 =
        new ElasticPoolSku("BC_Gen5", "BusinessCritical", "Gen5", 24, null);
    /** BusinessCritical Edition with BC_M_24 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_M_24 =
        new ElasticPoolSku("BC_M", "BusinessCritical", "M", 24, null);
    /** BusinessCritical Edition with BC_Gen5_32 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_GEN5_32 =
        new ElasticPoolSku("BC_Gen5", "BusinessCritical", "Gen5", 32, null);
    /** BusinessCritical Edition with BC_M_32 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_M_32 =
        new ElasticPoolSku("BC_M", "BusinessCritical", "M", 32, null);
    /** BusinessCritical Edition with BC_Gen5_40 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_GEN5_40 =
        new ElasticPoolSku("BC_Gen5", "BusinessCritical", "Gen5", 40, null);
    /** BusinessCritical Edition with BC_M_64 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_M_64 =
        new ElasticPoolSku("BC_M", "BusinessCritical", "M", 64, null);
    /** BusinessCritical Edition with BC_Gen5_80 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_GEN5_80 =
        new ElasticPoolSku("BC_Gen5", "BusinessCritical", "Gen5", 80, null);
    /** BusinessCritical Edition with BC_M_128 sku. */
    public static final ElasticPoolSku BUSINESSCRITICAL_BC_M_128 =
        new ElasticPoolSku("BC_M", "BusinessCritical", "M", 128, null);

    private final Sku sku;

    /**
     * Creates a elastic pool sku.
     *
     * @param name the sku name
     * @param tier the sku tier
     * @param family the sku family
     * @param capacity the sku capacity
     * @param size the sku size
     */
    public ElasticPoolSku(String name, String tier, String family, Integer capacity, String size) {
        this.sku = new Sku().withName(name).withTier(tier).withFamily(family).withCapacity(capacity).withSize(size);
        VALUES.putIfAbsent(toString(), this);
    }

    /**
     * Parses a sku to a ElasticPoolSku instance.
     *
     * @param sku the sku.
     * @return the ElasticPoolSku object, or null if sku is null.
     */
    public static ElasticPoolSku fromSku(Sku sku) {
        if (sku == null) {
            return null;
        }
        return new ElasticPoolSku(sku.name(), sku.tier(), sku.family(), sku.capacity(), sku.size());
    }

    /**
     * Lists the pre-defined elastic pool sku.
     *
     * @return immutable collection of the pre-defined elastic pool sku
     */
    public static Collection<ElasticPoolSku> getAll() {
        return VALUES.values();
    }

    @Override
    public String toString() {
        return String.format("%s/%s/%s/%d/%s", sku.name(), sku.tier(), sku.family(), sku.capacity(), sku.size());
    }

    /** @return the underneath sku description */
    @JsonValue
    public Sku toSku() {
        return new Sku()
            .withName(sku.name())
            .withTier(sku.tier())
            .withFamily(sku.family())
            .withCapacity(sku.capacity())
            .withSize(sku.size());
    }

    @Override
    public int hashCode() {
        return sku.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ElasticPoolSku)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        ElasticPoolSku rhs = (ElasticPoolSku) obj;
        return toString().equalsIgnoreCase(rhs.toString());
    }
}
