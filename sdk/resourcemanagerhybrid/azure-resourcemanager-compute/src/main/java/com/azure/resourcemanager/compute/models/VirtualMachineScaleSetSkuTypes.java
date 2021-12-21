// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Scale set virtual machine SKU types. */
// TODO: This should be called VirtualMachineScaleSetSkuType in the future (compat break from 1.0)
public class VirtualMachineScaleSetSkuTypes {
    // This needs to be at the beginning for the initialization to happen correctly
    private static final Map<String, VirtualMachineScaleSetSkuTypes> VALUES_BY_NAME = new HashMap<>();

    /** Static value Standard_A0 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_A0 =
        new VirtualMachineScaleSetSkuTypes("Standard_A0", "Standard");

    /** Static value Standard_A1 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_A1 =
        new VirtualMachineScaleSetSkuTypes("Standard_A1", "Standard");

    /** Static value Standard_A2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_A2 =
        new VirtualMachineScaleSetSkuTypes("Standard_A2", "Standard");

    /** Static value Standard_A3 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_A3 =
        new VirtualMachineScaleSetSkuTypes("Standard_A3", "Standard");

    /** Static value Standard_A4 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_A4 =
        new VirtualMachineScaleSetSkuTypes("Standard_A4", "Standard");

    /** Static value Standard_A5 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_A5 =
        new VirtualMachineScaleSetSkuTypes("Standard_A5", "Standard");

    /** Static value Standard_A6 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_A6 =
        new VirtualMachineScaleSetSkuTypes("Standard_A6", "Standard");

    /** Static value Standard_A7 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_A7 =
        new VirtualMachineScaleSetSkuTypes("Standard_A7", "Standard");

    /** Static value Standard_A8 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_A8 =
        new VirtualMachineScaleSetSkuTypes("Standard_A8", "Standard");

    /** Static value Standard_A9 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_A9 =
        new VirtualMachineScaleSetSkuTypes("Standard_A9", "Standard");

    /** Static value Standard_A10 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_A10 =
        new VirtualMachineScaleSetSkuTypes("Standard_A10", "Standard");

    /** Static value Standard_A11 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_A11 =
        new VirtualMachineScaleSetSkuTypes("Standard_A11", "Standard");

    /** Static value Standard_D1 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D1 =
        new VirtualMachineScaleSetSkuTypes("Standard_D1", "Standard");

    /** Static value Standard_D2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D2 =
        new VirtualMachineScaleSetSkuTypes("Standard_D2", "Standard");

    /** Static value Standard_D3 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D3 =
        new VirtualMachineScaleSetSkuTypes("Standard_D3", "Standard");

    /** Static value Standard_D4 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D4 =
        new VirtualMachineScaleSetSkuTypes("Standard_D4", "Standard");

    /** Static value Standard_D11 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D11 =
        new VirtualMachineScaleSetSkuTypes("Standard_D11", "Standard");

    /** Static value Standard_D12 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D12 =
        new VirtualMachineScaleSetSkuTypes("Standard_D12", "Standard");

    /** Static value Standard_D13 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D13 =
        new VirtualMachineScaleSetSkuTypes("Standard_D13", "Standard");

    /** Static value Standard_D14 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D14 =
        new VirtualMachineScaleSetSkuTypes("Standard_D14", "Standard");

    /** Static value Standard_D1_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D1_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_D1_v2", "Standard");

    /** Static value Standard_D2_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D2_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_D2_v2", "Standard");

    /** Static value Standard_D3_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D3_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_D3_v2", "Standard");

    /** Static value Standard_D4_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D4_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_D4_v2", "Standard");

    /** Static value Standard_D5_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D5_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_D5_v2", "Standard");

    /** Static value Standard_D11_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D11_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_D11_v2", "Standard");

    /** Static value Standard_D12_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D12_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_D12_v2", "Standard");

    /** Static value Standard_D13_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D13_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_D13_v2", "Standard");

    /** Static value Standard_D14_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D14_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_D14_v2", "Standard");

    /** Static value Standard_D15_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_D15_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_D15_v2", "Standard");

    /** Static value Standard_DS1 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS1 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS1", "Standard");

    /** Static value Standard_DS2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS2 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS2", "Standard");

    /** Static value Standard_DS3 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS3 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS3", "Standard");

    /** Static value Standard_DS4 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS4 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS4", "Standard");

    /** Static value Standard_DS11 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS11 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS11", "Standard");

    /** Static value Standard_DS12 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS12 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS12", "Standard");

    /** Static value Standard_DS13 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS13 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS13", "Standard");

    /** Static value Standard_DS14 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS14 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS14", "Standard");

    /** Static value Standard_DS1_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS1_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS1_v2", "Standard");

    /** Static value Standard_DS2_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS2_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS2_v2", "Standard");

    /** Static value Standard_DS3_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS3_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS3_v2", "Standard");

    /** Static value Standard_DS4_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS4_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS4_v2", "Standard");

    /** Static value Standard_DS5_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS5_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS5_v2", "Standard");

    /** Static value Standard_DS11_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS11_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS11_v2", "Standard");

    /** Static value Standard_DS12_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS12_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS12_v2", "Standard");

    /** Static value Standard_DS13_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS13_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS13_v2", "Standard");

    /** Static value Standard_DS14_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS14_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS14_v2", "Standard");

    /** Static value Standard_DS15_v2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_DS15_V2 =
        new VirtualMachineScaleSetSkuTypes("Standard_DS15_v2", "Standard");

    /** Static value STANDARD_F1S for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_F1S =
        new VirtualMachineScaleSetSkuTypes("STANDARD_F1S", "Standard");

    /** Static value STANDARD_F2S for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_F2S =
        new VirtualMachineScaleSetSkuTypes("STANDARD_F2S", "Standard");

    /** Static value STANDARD_F4S for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_F4S =
        new VirtualMachineScaleSetSkuTypes("STANDARD_F4S", "Standard");

    /** Static value STANDARD_F8S for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_F8S =
        new VirtualMachineScaleSetSkuTypes("STANDARD_F8S", "Standard");

    /** Static value STANDARD_F16S for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_F16S =
        new VirtualMachineScaleSetSkuTypes("STANDARD_F16S", "Standard");

    /** Static value STANDARD_F1 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_F1 =
        new VirtualMachineScaleSetSkuTypes("STANDARD_F1", "Standard");

    /** Static value STANDARD_F2 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_F2 =
        new VirtualMachineScaleSetSkuTypes("STANDARD_F2", "Standard");

    /** Static value STANDARD_F4 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_F4 =
        new VirtualMachineScaleSetSkuTypes("STANDARD_F4", "Standard");

    /** Static value STANDARD_F8 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_F8 =
        new VirtualMachineScaleSetSkuTypes("STANDARD_F8", "Standard");

    /** Static value STANDARD_F16 for VirtualMachineScaleSetSkuTypes. */
    public static final VirtualMachineScaleSetSkuTypes STANDARD_F16 =
        new VirtualMachineScaleSetSkuTypes("STANDARD_F16", "Standard");

    /** the SKU corresponding to this size. */
    private final Sku sku;

    /** The string value of the SKU. */
    private final String value;

    /** @return predefined virtual machine scale set SKU types */
    public static VirtualMachineScaleSetSkuTypes[] values() {
        Collection<VirtualMachineScaleSetSkuTypes> valuesCollection = VALUES_BY_NAME.values();
        return valuesCollection.toArray(new VirtualMachineScaleSetSkuTypes[valuesCollection.size()]);
    }

    /**
     * Creates a custom value for VirtualMachineSizeTypes.
     *
     * @param skuName a SKU name
     * @param skuTier a SKU tier
     */
    public VirtualMachineScaleSetSkuTypes(String skuName, String skuTier) {
        // TODO: This constructor should really be private
        this(new Sku().withName(skuName).withTier(skuTier));
    }

    /**
     * Creates a custom value for VirtualMachineSizeTypes.
     *
     * @param sku the SKU
     */
    public VirtualMachineScaleSetSkuTypes(Sku sku) {
        // TODO: This constructor should really be private
        // Store Sku copy since original user provided sku can be modified
        // by the user.
        //
        this.sku = createCopy(sku);
        if (this.sku.tier() == null) {
            this.value = this.sku.name();
        } else {
            this.value = this.sku.name() + '_' + this.sku.tier();
        }
        VALUES_BY_NAME.put(this.value.toLowerCase(Locale.ROOT), this);
    }

    /**
     * Parses a SKU into a VMSS SKU type and creates a new VirtualMachineScaleSetSkuType instance if not found among the
     * existing ones.
     *
     * @param sku a VMSS SKU
     * @return the parsed or created VMSS SKU type
     */
    public static VirtualMachineScaleSetSkuTypes fromSku(Sku sku) {
        if (sku == null) {
            return null;
        }

        String nameToLookFor = sku.name();
        if (sku.tier() != null) {
            nameToLookFor += '_' + sku.tier();
        }

        VirtualMachineScaleSetSkuTypes result = VALUES_BY_NAME.get(nameToLookFor.toLowerCase(Locale.ROOT));
        if (result != null) {
            return result;
        } else {
            return new VirtualMachineScaleSetSkuTypes(sku);
        }
    }

    /**
     * Parses into a VMSS SKU type and creates a new VMSS SKU type instance if not found among the existing ones.
     *
     * @param skuName a SKU name
     * @param skuTier a SKU tier
     * @return a VMSS SKU type
     */
    public static VirtualMachineScaleSetSkuTypes fromSkuNameAndTier(String skuName, String skuTier) {
        return fromSku(new Sku().withName(skuName).withTier(skuTier));
    }

    /** @return the SKU */
    public Sku sku() {
        // Return copy of sku to guard VirtualMachineScaleSetSkuTypes from ending up with invalid
        // sku in case consumer changes the returned Sku instance.
        //
        return createCopy(this.sku);
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VirtualMachineScaleSetSkuTypes)) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (value == null) {
            return ((VirtualMachineScaleSetSkuTypes) obj).value == null;
        } else {
            return value.equalsIgnoreCase(((VirtualMachineScaleSetSkuTypes) obj).value);
        }
    }

    /**
     * Creates a copy of the given sku.
     *
     * @param sku the sku to create copy of
     * @return the copy
     */
    private static Sku createCopy(Sku sku) {
        return new Sku().withName(sku.name()).withTier(sku.tier()).withCapacity(sku.capacity());
    }
}
