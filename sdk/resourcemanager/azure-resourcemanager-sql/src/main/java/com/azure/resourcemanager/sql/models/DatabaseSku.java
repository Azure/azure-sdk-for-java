// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Defines SQL Database Sku. */
@Fluent
public final class DatabaseSku {
    private static final ConcurrentMap<String, DatabaseSku> VALUES = new ConcurrentHashMap<>();

    /** Free Edition with Free sku. */
    public static final DatabaseSku FREE_FREE =
        new DatabaseSku("Free", "Free", null, 5, null);
    /** Basic Edition with Basic sku. */
    public static final DatabaseSku BASIC_BASIC =
        new DatabaseSku("Basic", "Basic", null, 5, null);
    /** Standard Edition with S0 sku. */
    public static final DatabaseSku STANDARD_S0 =
        new DatabaseSku("Standard", "Standard", null, 10, null);
    /** Standard Edition with S1 sku. */
    public static final DatabaseSku STANDARD_S1 =
        new DatabaseSku("Standard", "Standard", null, 20, null);
    /** Standard Edition with S2 sku. */
    public static final DatabaseSku STANDARD_S2 =
        new DatabaseSku("Standard", "Standard", null, 50, null);
    /** Standard Edition with S3 sku. */
    public static final DatabaseSku STANDARD_S3 =
        new DatabaseSku("Standard", "Standard", null, 100, null);
    /** Standard Edition with S4 sku. */
    public static final DatabaseSku STANDARD_S4 =
        new DatabaseSku("Standard", "Standard", null, 200, null);
    /** Standard Edition with S6 sku. */
    public static final DatabaseSku STANDARD_S6 =
        new DatabaseSku("Standard", "Standard", null, 400, null);
    /** Standard Edition with S7 sku. */
    public static final DatabaseSku STANDARD_S7 =
        new DatabaseSku("Standard", "Standard", null, 800, null);
    /** Standard Edition with S9 sku. */
    public static final DatabaseSku STANDARD_S9 =
        new DatabaseSku("Standard", "Standard", null, 1600, null);
    /** Standard Edition with S12 sku. */
    public static final DatabaseSku STANDARD_S12 =
        new DatabaseSku("Standard", "Standard", null, 3000, null);
    /** Premium Edition with P1 sku. */
    public static final DatabaseSku PREMIUM_P1 =
        new DatabaseSku("Premium", "Premium", null, 125, null);
    /** Premium Edition with P2 sku. */
    public static final DatabaseSku PREMIUM_P2 =
        new DatabaseSku("Premium", "Premium", null, 250, null);
    /** Premium Edition with P4 sku. */
    public static final DatabaseSku PREMIUM_P4 =
        new DatabaseSku("Premium", "Premium", null, 500, null);
    /** Premium Edition with P6 sku. */
    public static final DatabaseSku PREMIUM_P6 =
        new DatabaseSku("Premium", "Premium", null, 1000, null);
    /** Premium Edition with P11 sku. */
    public static final DatabaseSku PREMIUM_P11 =
        new DatabaseSku("Premium", "Premium", null, 1750, null);
    /** Premium Edition with P15 sku. */
    public static final DatabaseSku PREMIUM_P15 =
        new DatabaseSku("Premium", "Premium", null, 4000, null);
    /** DataWarehouse Edition with DW100c sku. */
    public static final DatabaseSku DATAWAREHOUSE_DW100C =
        new DatabaseSku("DataWarehouse", "DataWarehouse", null, 900, null);
    /** DataWarehouse Edition with DW200c sku. */
    public static final DatabaseSku DATAWAREHOUSE_DW200C =
        new DatabaseSku("DataWarehouse", "DataWarehouse", null, 1800, null);
    /** DataWarehouse Edition with DW300c sku. */
    public static final DatabaseSku DATAWAREHOUSE_DW300C =
        new DatabaseSku("DataWarehouse", "DataWarehouse", null, 2700, null);
    /** DataWarehouse Edition with DW400c sku. */
    public static final DatabaseSku DATAWAREHOUSE_DW400C =
        new DatabaseSku("DataWarehouse", "DataWarehouse", null, 3600, null);
    /** DataWarehouse Edition with DW500c sku. */
    public static final DatabaseSku DATAWAREHOUSE_DW500C =
        new DatabaseSku("DataWarehouse", "DataWarehouse", null, 4500, null);
    /** DataWarehouse Edition with DW1000c sku. */
    public static final DatabaseSku DATAWAREHOUSE_DW1000C =
        new DatabaseSku("DataWarehouse", "DataWarehouse", null, 9000, null);
    /** DataWarehouse Edition with DW1500c sku. */
    public static final DatabaseSku DATAWAREHOUSE_DW1500C =
        new DatabaseSku("DataWarehouse", "DataWarehouse", null, 13500, null);
    /** DataWarehouse Edition with DW2000c sku. */
    public static final DatabaseSku DATAWAREHOUSE_DW2000C =
        new DatabaseSku("DataWarehouse", "DataWarehouse", null, 18000, null);
    /** DataWarehouse Edition with DW2500c sku. */
    public static final DatabaseSku DATAWAREHOUSE_DW2500C =
        new DatabaseSku("DataWarehouse", "DataWarehouse", null, 22500, null);
    /** DataWarehouse Edition with DW3000c sku. */
    public static final DatabaseSku DATAWAREHOUSE_DW3000C =
        new DatabaseSku("DataWarehouse", "DataWarehouse", null, 27000, null);
    /** DataWarehouse Edition with DW5000c sku. */
    public static final DatabaseSku DATAWAREHOUSE_DW5000C =
        new DatabaseSku("DataWarehouse", "DataWarehouse", null, 45000, null);
    /** DataWarehouse Edition with DW6000c sku. */
    public static final DatabaseSku DATAWAREHOUSE_DW6000C =
        new DatabaseSku("DataWarehouse", "DataWarehouse", null, 54000, null);
    /** DataWarehouse Edition with DW7500c sku. */
    public static final DatabaseSku DATAWAREHOUSE_DW7500C =
        new DatabaseSku("DataWarehouse", "DataWarehouse", null, 67500, null);
    /** DataWarehouse Edition with DW10000c sku. */
    public static final DatabaseSku DATAWAREHOUSE_DW10000C =
        new DatabaseSku("DataWarehouse", "DataWarehouse", null, 90000, null);
    /** DataWarehouse Edition with DW15000c sku. */
    public static final DatabaseSku DATAWAREHOUSE_DW15000C =
        new DatabaseSku("DataWarehouse", "DataWarehouse", null, 135000, null);
    /** DataWarehouse Edition with DW30000c sku. */
    public static final DatabaseSku DATAWAREHOUSE_DW30000C =
        new DatabaseSku("DataWarehouse", "DataWarehouse", null, 270000, null);
    /** Stretch Edition with DS100 sku. */
    public static final DatabaseSku STRETCH_DS100 =
        new DatabaseSku("Stretch", "Stretch", null, 750, null);
    /** Stretch Edition with DS200 sku. */
    public static final DatabaseSku STRETCH_DS200 =
        new DatabaseSku("Stretch", "Stretch", null, 1500, null);
    /** Stretch Edition with DS300 sku. */
    public static final DatabaseSku STRETCH_DS300 =
        new DatabaseSku("Stretch", "Stretch", null, 2250, null);
    /** Stretch Edition with DS400 sku. */
    public static final DatabaseSku STRETCH_DS400 =
        new DatabaseSku("Stretch", "Stretch", null, 3000, null);
    /** Stretch Edition with DS500 sku. */
    public static final DatabaseSku STRETCH_DS500 =
        new DatabaseSku("Stretch", "Stretch", null, 3750, null);
    /** Stretch Edition with DS600 sku. */
    public static final DatabaseSku STRETCH_DS600 =
        new DatabaseSku("Stretch", "Stretch", null, 4500, null);
    /** Stretch Edition with DS1000 sku. */
    public static final DatabaseSku STRETCH_DS1000 =
        new DatabaseSku("Stretch", "Stretch", null, 7500, null);
    /** Stretch Edition with DS1200 sku. */
    public static final DatabaseSku STRETCH_DS1200 =
        new DatabaseSku("Stretch", "Stretch", null, 9000, null);
    /** Stretch Edition with DS1500 sku. */
    public static final DatabaseSku STRETCH_DS1500 =
        new DatabaseSku("Stretch", "Stretch", null, 11250, null);
    /** Stretch Edition with DS2000 sku. */
    public static final DatabaseSku STRETCH_DS2000 =
        new DatabaseSku("Stretch", "Stretch", null, 15000, null);
    /** GeneralPurpose Edition with GP_S_Gen5_1 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_S_GEN5_1 =
        new DatabaseSku("GP_S_Gen5", "GeneralPurpose", "Gen5", 1, null);
    /** GeneralPurpose Edition with GP_Gen5_2 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_GEN5_2 =
        new DatabaseSku("GP_Gen5", "GeneralPurpose", "Gen5", 2, null);
    /** GeneralPurpose Edition with GP_S_Gen5_2 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_S_GEN5_2 =
        new DatabaseSku("GP_S_Gen5", "GeneralPurpose", "Gen5", 2, null);
    /** GeneralPurpose Edition with GP_Gen5_4 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_GEN5_4 =
        new DatabaseSku("GP_Gen5", "GeneralPurpose", "Gen5", 4, null);
    /** GeneralPurpose Edition with GP_S_Gen5_4 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_S_GEN5_4 =
        new DatabaseSku("GP_S_Gen5", "GeneralPurpose", "Gen5", 4, null);
    /** GeneralPurpose Edition with GP_Gen5_6 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_GEN5_6 =
        new DatabaseSku("GP_Gen5", "GeneralPurpose", "Gen5", 6, null);
    /** GeneralPurpose Edition with GP_S_Gen5_6 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_S_GEN5_6 =
        new DatabaseSku("GP_S_Gen5", "GeneralPurpose", "Gen5", 6, null);
    /** GeneralPurpose Edition with GP_Gen5_8 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_GEN5_8 =
        new DatabaseSku("GP_Gen5", "GeneralPurpose", "Gen5", 8, null);
    /** GeneralPurpose Edition with GP_S_Gen5_8 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_S_GEN5_8 =
        new DatabaseSku("GP_S_Gen5", "GeneralPurpose", "Gen5", 8, null);
    /** GeneralPurpose Edition with GP_Fsv2_8 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_FSV2_8 =
        new DatabaseSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 8, null);
    /** GeneralPurpose Edition with GP_Gen5_10 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_GEN5_10 =
        new DatabaseSku("GP_Gen5", "GeneralPurpose", "Gen5", 10, null);
    /** GeneralPurpose Edition with GP_S_Gen5_10 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_S_GEN5_10 =
        new DatabaseSku("GP_S_Gen5", "GeneralPurpose", "Gen5", 10, null);
    /** GeneralPurpose Edition with GP_Fsv2_10 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_FSV2_10 =
        new DatabaseSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 10, null);
    /** GeneralPurpose Edition with GP_Gen5_12 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_GEN5_12 =
        new DatabaseSku("GP_Gen5", "GeneralPurpose", "Gen5", 12, null);
    /** GeneralPurpose Edition with GP_S_Gen5_12 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_S_GEN5_12 =
        new DatabaseSku("GP_S_Gen5", "GeneralPurpose", "Gen5", 12, null);
    /** GeneralPurpose Edition with GP_Fsv2_12 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_FSV2_12 =
        new DatabaseSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 12, null);
    /** GeneralPurpose Edition with GP_Gen5_14 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_GEN5_14 =
        new DatabaseSku("GP_Gen5", "GeneralPurpose", "Gen5", 14, null);
    /** GeneralPurpose Edition with GP_S_Gen5_14 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_S_GEN5_14 =
        new DatabaseSku("GP_S_Gen5", "GeneralPurpose", "Gen5", 14, null);
    /** GeneralPurpose Edition with GP_Fsv2_14 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_FSV2_14 =
        new DatabaseSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 14, null);
    /** GeneralPurpose Edition with GP_Gen5_16 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_GEN5_16 =
        new DatabaseSku("GP_Gen5", "GeneralPurpose", "Gen5", 16, null);
    /** GeneralPurpose Edition with GP_S_Gen5_16 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_S_GEN5_16 =
        new DatabaseSku("GP_S_Gen5", "GeneralPurpose", "Gen5", 16, null);
    /** GeneralPurpose Edition with GP_Fsv2_16 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_FSV2_16 =
        new DatabaseSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 16, null);
    /** GeneralPurpose Edition with GP_Gen5_18 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_GEN5_18 =
        new DatabaseSku("GP_Gen5", "GeneralPurpose", "Gen5", 18, null);
    /** GeneralPurpose Edition with GP_S_Gen5_18 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_S_GEN5_18 =
        new DatabaseSku("GP_S_Gen5", "GeneralPurpose", "Gen5", 18, null);
    /** GeneralPurpose Edition with GP_Fsv2_18 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_FSV2_18 =
        new DatabaseSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 18, null);
    /** GeneralPurpose Edition with GP_Gen5_20 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_GEN5_20 =
        new DatabaseSku("GP_Gen5", "GeneralPurpose", "Gen5", 20, null);
    /** GeneralPurpose Edition with GP_S_Gen5_20 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_S_GEN5_20 =
        new DatabaseSku("GP_S_Gen5", "GeneralPurpose", "Gen5", 20, null);
    /** GeneralPurpose Edition with GP_Fsv2_20 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_FSV2_20 =
        new DatabaseSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 20, null);
    /** GeneralPurpose Edition with GP_Gen5_24 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_GEN5_24 =
        new DatabaseSku("GP_Gen5", "GeneralPurpose", "Gen5", 24, null);
    /** GeneralPurpose Edition with GP_S_Gen5_24 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_S_GEN5_24 =
        new DatabaseSku("GP_S_Gen5", "GeneralPurpose", "Gen5", 24, null);
    /** GeneralPurpose Edition with GP_Fsv2_24 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_FSV2_24 =
        new DatabaseSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 24, null);
    /** GeneralPurpose Edition with GP_Gen5_32 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_GEN5_32 =
        new DatabaseSku("GP_Gen5", "GeneralPurpose", "Gen5", 32, null);
    /** GeneralPurpose Edition with GP_S_Gen5_32 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_S_GEN5_32 =
        new DatabaseSku("GP_S_Gen5", "GeneralPurpose", "Gen5", 32, null);
    /** GeneralPurpose Edition with GP_Fsv2_32 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_FSV2_32 =
        new DatabaseSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 32, null);
    /** GeneralPurpose Edition with GP_Fsv2_36 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_FSV2_36 =
        new DatabaseSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 36, null);
    /** GeneralPurpose Edition with GP_Gen5_40 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_GEN5_40 =
        new DatabaseSku("GP_Gen5", "GeneralPurpose", "Gen5", 40, null);
    /** GeneralPurpose Edition with GP_S_Gen5_40 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_S_GEN5_40 =
        new DatabaseSku("GP_S_Gen5", "GeneralPurpose", "Gen5", 40, null);
    /** GeneralPurpose Edition with GP_Fsv2_72 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_FSV2_72 =
        new DatabaseSku("GP_Fsv2", "GeneralPurpose", "Fsv2", 72, null);
    /** GeneralPurpose Edition with GP_Gen5_80 sku. */
    public static final DatabaseSku GENERALPURPOSE_GP_GEN5_80 =
        new DatabaseSku("GP_Gen5", "GeneralPurpose", "Gen5", 80, null);
    /** BusinessCritical Edition with BC_Gen5_2 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_GEN5_2 =
        new DatabaseSku("BC_Gen5", "BusinessCritical", "Gen5", 2, null);
    /** BusinessCritical Edition with BC_Gen5_4 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_GEN5_4 =
        new DatabaseSku("BC_Gen5", "BusinessCritical", "Gen5", 4, null);
    /** BusinessCritical Edition with BC_Gen5_6 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_GEN5_6 =
        new DatabaseSku("BC_Gen5", "BusinessCritical", "Gen5", 6, null);
    /** BusinessCritical Edition with BC_Gen5_8 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_GEN5_8 =
        new DatabaseSku("BC_Gen5", "BusinessCritical", "Gen5", 8, null);
    /** BusinessCritical Edition with BC_M_8 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_M_8 =
        new DatabaseSku("BC_M", "BusinessCritical", "M", 8, null);
    /** BusinessCritical Edition with BC_Gen5_10 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_GEN5_10 =
        new DatabaseSku("BC_Gen5", "BusinessCritical", "Gen5", 10, null);
    /** BusinessCritical Edition with BC_M_10 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_M_10 =
        new DatabaseSku("BC_M", "BusinessCritical", "M", 10, null);
    /** BusinessCritical Edition with BC_Gen5_12 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_GEN5_12 =
        new DatabaseSku("BC_Gen5", "BusinessCritical", "Gen5", 12, null);
    /** BusinessCritical Edition with BC_M_12 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_M_12 =
        new DatabaseSku("BC_M", "BusinessCritical", "M", 12, null);
    /** BusinessCritical Edition with BC_Gen5_14 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_GEN5_14 =
        new DatabaseSku("BC_Gen5", "BusinessCritical", "Gen5", 14, null);
    /** BusinessCritical Edition with BC_M_14 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_M_14 =
        new DatabaseSku("BC_M", "BusinessCritical", "M", 14, null);
    /** BusinessCritical Edition with BC_Gen5_16 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_GEN5_16 =
        new DatabaseSku("BC_Gen5", "BusinessCritical", "Gen5", 16, null);
    /** BusinessCritical Edition with BC_M_16 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_M_16 =
        new DatabaseSku("BC_M", "BusinessCritical", "M", 16, null);
    /** BusinessCritical Edition with BC_Gen5_18 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_GEN5_18 =
        new DatabaseSku("BC_Gen5", "BusinessCritical", "Gen5", 18, null);
    /** BusinessCritical Edition with BC_M_18 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_M_18 =
        new DatabaseSku("BC_M", "BusinessCritical", "M", 18, null);
    /** BusinessCritical Edition with BC_Gen5_20 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_GEN5_20 =
        new DatabaseSku("BC_Gen5", "BusinessCritical", "Gen5", 20, null);
    /** BusinessCritical Edition with BC_M_20 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_M_20 =
        new DatabaseSku("BC_M", "BusinessCritical", "M", 20, null);
    /** BusinessCritical Edition with BC_Gen5_24 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_GEN5_24 =
        new DatabaseSku("BC_Gen5", "BusinessCritical", "Gen5", 24, null);
    /** BusinessCritical Edition with BC_M_24 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_M_24 =
        new DatabaseSku("BC_M", "BusinessCritical", "M", 24, null);
    /** BusinessCritical Edition with BC_Gen5_32 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_GEN5_32 =
        new DatabaseSku("BC_Gen5", "BusinessCritical", "Gen5", 32, null);
    /** BusinessCritical Edition with BC_M_32 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_M_32 =
        new DatabaseSku("BC_M", "BusinessCritical", "M", 32, null);
    /** BusinessCritical Edition with BC_Gen5_40 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_GEN5_40 =
        new DatabaseSku("BC_Gen5", "BusinessCritical", "Gen5", 40, null);
    /** BusinessCritical Edition with BC_M_64 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_M_64 =
        new DatabaseSku("BC_M", "BusinessCritical", "M", 64, null);
    /** BusinessCritical Edition with BC_Gen5_80 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_GEN5_80 =
        new DatabaseSku("BC_Gen5", "BusinessCritical", "Gen5", 80, null);
    /** BusinessCritical Edition with BC_M_128 sku. */
    public static final DatabaseSku BUSINESSCRITICAL_BC_M_128 =
        new DatabaseSku("BC_M", "BusinessCritical", "M", 128, null);
    /** Hyperscale Edition with HS_Gen5_2 sku. */
    public static final DatabaseSku HYPERSCALE_HS_GEN5_2 =
        new DatabaseSku("HS_Gen5", "Hyperscale", "Gen5", 2, null);
    /** Hyperscale Edition with HS_Gen5_4 sku. */
    public static final DatabaseSku HYPERSCALE_HS_GEN5_4 =
        new DatabaseSku("HS_Gen5", "Hyperscale", "Gen5", 4, null);
    /** Hyperscale Edition with HS_Gen5_6 sku. */
    public static final DatabaseSku HYPERSCALE_HS_GEN5_6 =
        new DatabaseSku("HS_Gen5", "Hyperscale", "Gen5", 6, null);
    /** Hyperscale Edition with HS_Gen5_8 sku. */
    public static final DatabaseSku HYPERSCALE_HS_GEN5_8 =
        new DatabaseSku("HS_Gen5", "Hyperscale", "Gen5", 8, null);
    /** Hyperscale Edition with HS_Gen5_10 sku. */
    public static final DatabaseSku HYPERSCALE_HS_GEN5_10 =
        new DatabaseSku("HS_Gen5", "Hyperscale", "Gen5", 10, null);
    /** Hyperscale Edition with HS_Gen5_12 sku. */
    public static final DatabaseSku HYPERSCALE_HS_GEN5_12 =
        new DatabaseSku("HS_Gen5", "Hyperscale", "Gen5", 12, null);
    /** Hyperscale Edition with HS_Gen5_14 sku. */
    public static final DatabaseSku HYPERSCALE_HS_GEN5_14 =
        new DatabaseSku("HS_Gen5", "Hyperscale", "Gen5", 14, null);
    /** Hyperscale Edition with HS_Gen5_16 sku. */
    public static final DatabaseSku HYPERSCALE_HS_GEN5_16 =
        new DatabaseSku("HS_Gen5", "Hyperscale", "Gen5", 16, null);
    /** Hyperscale Edition with HS_Gen5_18 sku. */
    public static final DatabaseSku HYPERSCALE_HS_GEN5_18 =
        new DatabaseSku("HS_Gen5", "Hyperscale", "Gen5", 18, null);
    /** Hyperscale Edition with HS_Gen5_20 sku. */
    public static final DatabaseSku HYPERSCALE_HS_GEN5_20 =
        new DatabaseSku("HS_Gen5", "Hyperscale", "Gen5", 20, null);
    /** Hyperscale Edition with HS_Gen5_24 sku. */
    public static final DatabaseSku HYPERSCALE_HS_GEN5_24 =
        new DatabaseSku("HS_Gen5", "Hyperscale", "Gen5", 24, null);
    /** Hyperscale Edition with HS_Gen5_32 sku. */
    public static final DatabaseSku HYPERSCALE_HS_GEN5_32 =
        new DatabaseSku("HS_Gen5", "Hyperscale", "Gen5", 32, null);
    /** Hyperscale Edition with HS_Gen5_40 sku. */
    public static final DatabaseSku HYPERSCALE_HS_GEN5_40 =
        new DatabaseSku("HS_Gen5", "Hyperscale", "Gen5", 40, null);
    /** Hyperscale Edition with HS_Gen5_80 sku. */
    public static final DatabaseSku HYPERSCALE_HS_GEN5_80 =
        new DatabaseSku("HS_Gen5", "Hyperscale", "Gen5", 80, null);

    private final Sku sku;

    /**
     * Creates a database sku.
     *
     * @param name the sku name
     * @param tier the sku tier
     * @param family the sku family
     * @param capacity the sku capacity
     * @param size the sku size
     */
    public DatabaseSku(String name, String tier, String family, Integer capacity, String size) {
        this.sku = new Sku().withName(name).withTier(tier).withFamily(family).withCapacity(capacity).withSize(size);
        VALUES.putIfAbsent(toString(), this);
    }

    /**
     * Parses a sku to a DatabaseSku instance.
     *
     * @param sku the sku.
     * @return the DatabaseSku object, or null if sku is null.
     */
    public static DatabaseSku fromSku(Sku sku) {
        if (sku == null) {
            return null;
        }
        return new DatabaseSku(sku.name(), sku.tier(), sku.family(), sku.capacity(), sku.size());
    }

    /**
     * Lists the pre-defined database sku.
     *
     * @return immutable collection of the pre-defined database sku
     */
    public static Collection<DatabaseSku> getAll() {
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
        if (!(obj instanceof DatabaseSku)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        DatabaseSku rhs = (DatabaseSku) obj;
        return toString().equalsIgnoreCase(rhs.toString());
    }
}
