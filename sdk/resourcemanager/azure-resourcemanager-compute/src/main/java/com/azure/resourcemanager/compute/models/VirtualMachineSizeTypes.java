// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for VirtualMachineSizeTypes. */
public final class VirtualMachineSizeTypes extends ExpandableStringEnum<VirtualMachineSizeTypes> {
    /** Static value Basic_A0 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes BASIC_A0 = fromString("Basic_A0");

    /** Static value Basic_A1 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes BASIC_A1 = fromString("Basic_A1");

    /** Static value Basic_A2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes BASIC_A2 = fromString("Basic_A2");

    /** Static value Basic_A3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes BASIC_A3 = fromString("Basic_A3");

    /** Static value Basic_A4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes BASIC_A4 = fromString("Basic_A4");

    /** Static value Standard_A0 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A0 = fromString("Standard_A0");

    /** Static value Standard_A1 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A1 = fromString("Standard_A1");

    /** Static value Standard_A10 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A10 = fromString("Standard_A10");

    /** Static value Standard_A11 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A11 = fromString("Standard_A11");

    /** Static value Standard_A1_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A1_V2 = fromString("Standard_A1_v2");

    /** Static value Standard_A2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A2 = fromString("Standard_A2");

    /** Static value Standard_A2_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A2_V2 = fromString("Standard_A2_v2");

    /** Static value Standard_A2m_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A2M_V2 = fromString("Standard_A2m_v2");

    /** Static value Standard_A3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A3 = fromString("Standard_A3");

    /** Static value Standard_A4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A4 = fromString("Standard_A4");

    /** Static value Standard_A4_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A4_V2 = fromString("Standard_A4_v2");

    /** Static value Standard_A4m_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A4M_V2 = fromString("Standard_A4m_v2");

    /** Static value Standard_A5 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A5 = fromString("Standard_A5");

    /** Static value Standard_A6 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A6 = fromString("Standard_A6");

    /** Static value Standard_A7 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A7 = fromString("Standard_A7");

    /** Static value Standard_A8 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A8 = fromString("Standard_A8");

    /** Static value Standard_A8_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A8_V2 = fromString("Standard_A8_v2");

    /** Static value Standard_A8m_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A8M_V2 = fromString("Standard_A8m_v2");

    /** Static value Standard_A9 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_A9 = fromString("Standard_A9");

    /** Static value Standard_B12ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_B12MS = fromString("Standard_B12ms");

    /** Static value Standard_B16ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_B16MS = fromString("Standard_B16ms");

    /** Static value Standard_B1ls for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_B1LS = fromString("Standard_B1ls");

    /** Static value Standard_B1ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_B1MS = fromString("Standard_B1ms");

    /** Static value Standard_B1s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_B1S = fromString("Standard_B1s");

    /** Static value Standard_B20ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_B20MS = fromString("Standard_B20ms");

    /** Static value Standard_B2ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_B2MS = fromString("Standard_B2ms");

    /** Static value Standard_B2s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_B2S = fromString("Standard_B2s");

    /** Static value Standard_B4ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_B4MS = fromString("Standard_B4ms");

    /** Static value Standard_B8ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_B8MS = fromString("Standard_B8ms");

    /** Static value Standard_D1 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D1 = fromString("Standard_D1");

    /** Static value Standard_D11 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D11 = fromString("Standard_D11");

    /** Static value Standard_D11_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D11_V2 = fromString("Standard_D11_v2");

    /** Static value Standard_D11_v2_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D11_V2_PROMO = fromString("Standard_D11_v2_Promo");

    /** Static value Standard_D12 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D12 = fromString("Standard_D12");

    /** Static value Standard_D12_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D12_V2 = fromString("Standard_D12_v2");

    /** Static value Standard_D12_v2_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D12_V2_PROMO = fromString("Standard_D12_v2_Promo");

    /** Static value Standard_D13 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D13 = fromString("Standard_D13");

    /** Static value Standard_D13_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D13_V2 = fromString("Standard_D13_v2");

    /** Static value Standard_D13_v2_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D13_V2_PROMO = fromString("Standard_D13_v2_Promo");

    /** Static value Standard_D14 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D14 = fromString("Standard_D14");

    /** Static value Standard_D14_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D14_V2 = fromString("Standard_D14_v2");

    /** Static value Standard_D14_v2_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D14_V2_PROMO = fromString("Standard_D14_v2_Promo");

    /** Static value Standard_D15_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D15_V2 = fromString("Standard_D15_v2");

    /** Static value Standard_D16_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D16_V3 = fromString("Standard_D16_v3");

    /** Static value Standard_D16_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D16_V4 = fromString("Standard_D16_v4");

    /** Static value Standard_D16a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D16A_V4 = fromString("Standard_D16a_v4");

    /** Static value Standard_D16as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D16AS_V4 = fromString("Standard_D16as_v4");

    /** Static value Standard_D16d_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D16D_V4 = fromString("Standard_D16d_v4");

    /** Static value Standard_D16ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D16DS_V4 = fromString("Standard_D16ds_v4");

    /** Static value Standard_D16s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D16S_V3 = fromString("Standard_D16s_v3");

    /** Static value Standard_D16s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D16S_V4 = fromString("Standard_D16s_v4");

    /** Static value Standard_D1_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D1_V2 = fromString("Standard_D1_v2");

    /** Static value Standard_D2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D2 = fromString("Standard_D2");

    /** Static value Standard_D2_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D2_V2 = fromString("Standard_D2_v2");

    /** Static value Standard_D2_v2_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D2_V2_PROMO = fromString("Standard_D2_v2_Promo");

    /** Static value Standard_D2_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D2_V3 = fromString("Standard_D2_v3");

    /** Static value Standard_D2_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D2_V4 = fromString("Standard_D2_v4");

    /** Static value Standard_D2a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D2A_V4 = fromString("Standard_D2a_v4");

    /** Static value Standard_D2as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D2AS_V4 = fromString("Standard_D2as_v4");

    /** Static value Standard_D2d_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D2D_V4 = fromString("Standard_D2d_v4");

    /** Static value Standard_D2ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D2DS_V4 = fromString("Standard_D2ds_v4");

    /** Static value Standard_D2s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D2S_V3 = fromString("Standard_D2s_v3");

    /** Static value Standard_D2s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D2S_V4 = fromString("Standard_D2s_v4");

    /** Static value Standard_D3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D3 = fromString("Standard_D3");

    /** Static value Standard_D32_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D32_V3 = fromString("Standard_D32_v3");

    /** Static value Standard_D32_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D32_V4 = fromString("Standard_D32_v4");

    /** Static value Standard_D32a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D32A_V4 = fromString("Standard_D32a_v4");

    /** Static value Standard_D32as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D32AS_V4 = fromString("Standard_D32as_v4");

    /** Static value Standard_D32d_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D32D_V4 = fromString("Standard_D32d_v4");

    /** Static value Standard_D32ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D32DS_V4 = fromString("Standard_D32ds_v4");

    /** Static value Standard_D32s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D32S_V3 = fromString("Standard_D32s_v3");

    /** Static value Standard_D32s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D32S_V4 = fromString("Standard_D32s_v4");

    /** Static value Standard_D3_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D3_V2 = fromString("Standard_D3_v2");

    /** Static value Standard_D3_v2_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D3_V2_PROMO = fromString("Standard_D3_v2_Promo");

    /** Static value Standard_D4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D4 = fromString("Standard_D4");

    /** Static value Standard_D48_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D48_V3 = fromString("Standard_D48_v3");

    /** Static value Standard_D48_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D48_V4 = fromString("Standard_D48_v4");

    /** Static value Standard_D48a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D48A_V4 = fromString("Standard_D48a_v4");

    /** Static value Standard_D48as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D48AS_V4 = fromString("Standard_D48as_v4");

    /** Static value Standard_D48d_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D48D_V4 = fromString("Standard_D48d_v4");

    /** Static value Standard_D48ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D48DS_V4 = fromString("Standard_D48ds_v4");

    /** Static value Standard_D48s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D48S_V3 = fromString("Standard_D48s_v3");

    /** Static value Standard_D48s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D48S_V4 = fromString("Standard_D48s_v4");

    /** Static value Standard_D4_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D4_V2 = fromString("Standard_D4_v2");

    /** Static value Standard_D4_v2_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D4_V2_PROMO = fromString("Standard_D4_v2_Promo");

    /** Static value Standard_D4_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D4_V3 = fromString("Standard_D4_v3");

    /** Static value Standard_D4_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D4_V4 = fromString("Standard_D4_v4");

    /** Static value Standard_D4a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D4A_V4 = fromString("Standard_D4a_v4");

    /** Static value Standard_D4as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D4AS_V4 = fromString("Standard_D4as_v4");

    /** Static value Standard_D4d_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D4D_V4 = fromString("Standard_D4d_v4");

    /** Static value Standard_D4ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D4DS_V4 = fromString("Standard_D4ds_v4");

    /** Static value Standard_D4s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D4S_V3 = fromString("Standard_D4s_v3");

    /** Static value Standard_D4s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D4S_V4 = fromString("Standard_D4s_v4");

    /** Static value Standard_D5_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D5_V2 = fromString("Standard_D5_v2");

    /** Static value Standard_D5_v2_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D5_V2_PROMO = fromString("Standard_D5_v2_Promo");

    /** Static value Standard_D64_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D64_V3 = fromString("Standard_D64_v3");

    /** Static value Standard_D64_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D64_V4 = fromString("Standard_D64_v4");

    /** Static value Standard_D64a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D64A_V4 = fromString("Standard_D64a_v4");

    /** Static value Standard_D64as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D64AS_V4 = fromString("Standard_D64as_v4");

    /** Static value Standard_D64d_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D64D_V4 = fromString("Standard_D64d_v4");

    /** Static value Standard_D64ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D64DS_V4 = fromString("Standard_D64ds_v4");

    /** Static value Standard_D64s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D64S_V3 = fromString("Standard_D64s_v3");

    /** Static value Standard_D64s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D64S_V4 = fromString("Standard_D64s_v4");

    /** Static value Standard_D8_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D8_V3 = fromString("Standard_D8_v3");

    /** Static value Standard_D8_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D8_V4 = fromString("Standard_D8_v4");

    /** Static value Standard_D8a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D8A_V4 = fromString("Standard_D8a_v4");

    /** Static value Standard_D8as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D8AS_V4 = fromString("Standard_D8as_v4");

    /** Static value Standard_D8d_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D8D_V4 = fromString("Standard_D8d_v4");

    /** Static value Standard_D8ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D8DS_V4 = fromString("Standard_D8ds_v4");

    /** Static value Standard_D8s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D8S_V3 = fromString("Standard_D8s_v3");

    /** Static value Standard_D8s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D8S_V4 = fromString("Standard_D8s_v4");

    /** Static value Standard_D96a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D96A_V4 = fromString("Standard_D96a_v4");

    /** Static value Standard_D96as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_D96AS_V4 = fromString("Standard_D96as_v4");

    /** Static value Standard_DC1s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DC1S_V2 = fromString("Standard_DC1s_v2");

    /** Static value Standard_DC2s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DC2S = fromString("Standard_DC2s");

    /** Static value Standard_DC2s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DC2S_V2 = fromString("Standard_DC2s_v2");

    /** Static value Standard_DC4s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DC4S = fromString("Standard_DC4s");

    /** Static value Standard_DC4s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DC4S_V2 = fromString("Standard_DC4s_v2");

    /** Static value Standard_DC8_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DC8_V2 = fromString("Standard_DC8_v2");

    /** Static value Standard_DS1 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS1 = fromString("Standard_DS1");

    /** Static value Standard_DS11 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS11 = fromString("Standard_DS11");

    /** Static value Standard_DS11-1_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS11_1_V2 = fromString("Standard_DS11-1_v2");

    /** Static value Standard_DS11_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS11_V2 = fromString("Standard_DS11_v2");

    /** Static value Standard_DS11_v2_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS11_V2_PROMO = fromString("Standard_DS11_v2_Promo");

    /** Static value Standard_DS12 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS12 = fromString("Standard_DS12");

    /** Static value Standard_DS12-1_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS12_1_V2 = fromString("Standard_DS12-1_v2");

    /** Static value Standard_DS12-2_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS12_2_V2 = fromString("Standard_DS12-2_v2");

    /** Static value Standard_DS12_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS12_V2 = fromString("Standard_DS12_v2");

    /** Static value Standard_DS12_v2_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS12_V2_PROMO = fromString("Standard_DS12_v2_Promo");

    /** Static value Standard_DS13 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS13 = fromString("Standard_DS13");

    /** Static value Standard_DS13-2_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS13_2_V2 = fromString("Standard_DS13-2_v2");

    /** Static value Standard_DS13-4_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS13_4_V2 = fromString("Standard_DS13-4_v2");

    /** Static value Standard_DS13_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS13_V2 = fromString("Standard_DS13_v2");

    /** Static value Standard_DS13_v2_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS13_V2_PROMO = fromString("Standard_DS13_v2_Promo");

    /** Static value Standard_DS14 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS14 = fromString("Standard_DS14");

    /** Static value Standard_DS14-4_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS14_4_V2 = fromString("Standard_DS14-4_v2");

    /** Static value Standard_DS14-8_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS14_8_V2 = fromString("Standard_DS14-8_v2");

    /** Static value Standard_DS14_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS14_V2 = fromString("Standard_DS14_v2");

    /** Static value Standard_DS14_v2_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS14_V2_PROMO = fromString("Standard_DS14_v2_Promo");

    /** Static value Standard_DS15_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS15_V2 = fromString("Standard_DS15_v2");

    /** Static value Standard_DS1_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS1_V2 = fromString("Standard_DS1_v2");

    /** Static value Standard_DS2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS2 = fromString("Standard_DS2");

    /** Static value Standard_DS2_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS2_V2 = fromString("Standard_DS2_v2");

    /** Static value Standard_DS2_v2_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS2_V2_PROMO = fromString("Standard_DS2_v2_Promo");

    /** Static value Standard_DS3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS3 = fromString("Standard_DS3");

    /** Static value Standard_DS3_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS3_V2 = fromString("Standard_DS3_v2");

    /** Static value Standard_DS3_v2_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS3_V2_PROMO = fromString("Standard_DS3_v2_Promo");

    /** Static value Standard_DS4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS4 = fromString("Standard_DS4");

    /** Static value Standard_DS4_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS4_V2 = fromString("Standard_DS4_v2");

    /** Static value Standard_DS4_v2_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS4_V2_PROMO = fromString("Standard_DS4_v2_Promo");

    /** Static value Standard_DS5_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS5_V2 = fromString("Standard_DS5_v2");

    /** Static value Standard_DS5_v2_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_DS5_V2_PROMO = fromString("Standard_DS5_v2_Promo");

    /** Static value Standard_E16-4ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E16_4DS_V4 = fromString("Standard_E16-4ds_v4");

    /** Static value Standard_E16-4s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E16_4S_V3 = fromString("Standard_E16-4s_v3");

    /** Static value Standard_E16-4s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E16_4S_V4 = fromString("Standard_E16-4s_v4");

    /** Static value Standard_E16-8ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E16_8DS_V4 = fromString("Standard_E16-8ds_v4");

    /** Static value Standard_E16-8s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E16_8S_V3 = fromString("Standard_E16-8s_v3");

    /** Static value Standard_E16-8s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E16_8S_V4 = fromString("Standard_E16-8s_v4");

    /** Static value Standard_E16_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E16_V3 = fromString("Standard_E16_v3");

    /** Static value Standard_E16_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E16_V4 = fromString("Standard_E16_v4");

    /** Static value Standard_E16a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E16A_V4 = fromString("Standard_E16a_v4");

    /** Static value Standard_E16as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E16AS_V4 = fromString("Standard_E16as_v4");

    /** Static value Standard_E16d_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E16D_V4 = fromString("Standard_E16d_v4");

    /** Static value Standard_E16ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E16DS_V4 = fromString("Standard_E16ds_v4");

    /** Static value Standard_E16s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E16S_V3 = fromString("Standard_E16s_v3");

    /** Static value Standard_E16s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E16S_V4 = fromString("Standard_E16s_v4");

    /** Static value Standard_E20_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E20_V3 = fromString("Standard_E20_v3");

    /** Static value Standard_E20_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E20_V4 = fromString("Standard_E20_v4");

    /** Static value Standard_E20a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E20A_V4 = fromString("Standard_E20a_v4");

    /** Static value Standard_E20as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E20AS_V4 = fromString("Standard_E20as_v4");

    /** Static value Standard_E20d_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E20D_V4 = fromString("Standard_E20d_v4");

    /** Static value Standard_E20ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E20DS_V4 = fromString("Standard_E20ds_v4");

    /** Static value Standard_E20s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E20S_V3 = fromString("Standard_E20s_v3");

    /** Static value Standard_E20s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E20S_V4 = fromString("Standard_E20s_v4");

    /** Static value Standard_E2_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E2_V3 = fromString("Standard_E2_v3");

    /** Static value Standard_E2_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E2_V4 = fromString("Standard_E2_v4");

    /** Static value Standard_E2a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E2A_V4 = fromString("Standard_E2a_v4");

    /** Static value Standard_E2as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E2AS_V4 = fromString("Standard_E2as_v4");

    /** Static value Standard_E2d_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E2D_V4 = fromString("Standard_E2d_v4");

    /** Static value Standard_E2ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E2DS_V4 = fromString("Standard_E2ds_v4");

    /** Static value Standard_E2s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E2S_V3 = fromString("Standard_E2s_v3");

    /** Static value Standard_E2s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E2S_V4 = fromString("Standard_E2s_v4");

    /** Static value Standard_E32-16ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E32_16DS_V4 = fromString("Standard_E32-16ds_v4");

    /** Static value Standard_E32-16s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E32_16S_V3 = fromString("Standard_E32-16s_v3");

    /** Static value Standard_E32-16s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E32_16S_V4 = fromString("Standard_E32-16s_v4");

    /** Static value Standard_E32-8ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E32_8DS_V4 = fromString("Standard_E32-8ds_v4");

    /** Static value Standard_E32-8s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E32_8S_V3 = fromString("Standard_E32-8s_v3");

    /** Static value Standard_E32-8s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E32_8S_V4 = fromString("Standard_E32-8s_v4");

    /** Static value Standard_E32_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E32_V3 = fromString("Standard_E32_v3");

    /** Static value Standard_E32_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E32_V4 = fromString("Standard_E32_v4");

    /** Static value Standard_E32a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E32A_V4 = fromString("Standard_E32a_v4");

    /** Static value Standard_E32as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E32AS_V4 = fromString("Standard_E32as_v4");

    /** Static value Standard_E32d_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E32D_V4 = fromString("Standard_E32d_v4");

    /** Static value Standard_E32ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E32DS_V4 = fromString("Standard_E32ds_v4");

    /** Static value Standard_E32s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E32S_V3 = fromString("Standard_E32s_v3");

    /** Static value Standard_E32s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E32S_V4 = fromString("Standard_E32s_v4");

    /** Static value Standard_E4-2ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E4_2DS_V4 = fromString("Standard_E4-2ds_v4");

    /** Static value Standard_E4-2s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E4_2S_V3 = fromString("Standard_E4-2s_v3");

    /** Static value Standard_E4-2s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E4_2S_V4 = fromString("Standard_E4-2s_v4");

    /** Static value Standard_E48_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E48_V3 = fromString("Standard_E48_v3");

    /** Static value Standard_E48_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E48_V4 = fromString("Standard_E48_v4");

    /** Static value Standard_E48a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E48A_V4 = fromString("Standard_E48a_v4");

    /** Static value Standard_E48as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E48AS_V4 = fromString("Standard_E48as_v4");

    /** Static value Standard_E48d_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E48D_V4 = fromString("Standard_E48d_v4");

    /** Static value Standard_E48ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E48DS_V4 = fromString("Standard_E48ds_v4");

    /** Static value Standard_E48s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E48S_V3 = fromString("Standard_E48s_v3");

    /** Static value Standard_E48s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E48S_V4 = fromString("Standard_E48s_v4");

    /** Static value Standard_E4_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E4_V3 = fromString("Standard_E4_v3");

    /** Static value Standard_E4_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E4_V4 = fromString("Standard_E4_v4");

    /** Static value Standard_E4a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E4A_V4 = fromString("Standard_E4a_v4");

    /** Static value Standard_E4as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E4AS_V4 = fromString("Standard_E4as_v4");

    /** Static value Standard_E4d_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E4D_V4 = fromString("Standard_E4d_v4");

    /** Static value Standard_E4ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E4DS_V4 = fromString("Standard_E4ds_v4");

    /** Static value Standard_E4s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E4S_V3 = fromString("Standard_E4s_v3");

    /** Static value Standard_E4s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E4S_V4 = fromString("Standard_E4s_v4");

    /** Static value Standard_E64-16ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E64_16DS_V4 = fromString("Standard_E64-16ds_v4");

    /** Static value Standard_E64-16s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E64_16S_V3 = fromString("Standard_E64-16s_v3");

    /** Static value Standard_E64-16s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E64_16S_V4 = fromString("Standard_E64-16s_v4");

    /** Static value Standard_E64-32ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E64_32DS_V4 = fromString("Standard_E64-32ds_v4");

    /** Static value Standard_E64-32s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E64_32S_V3 = fromString("Standard_E64-32s_v3");

    /** Static value Standard_E64-32s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E64_32S_V4 = fromString("Standard_E64-32s_v4");

    /** Static value Standard_E64_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E64_V3 = fromString("Standard_E64_v3");

    /** Static value Standard_E64_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E64_V4 = fromString("Standard_E64_v4");

    /** Static value Standard_E64a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E64A_V4 = fromString("Standard_E64a_v4");

    /** Static value Standard_E64as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E64AS_V4 = fromString("Standard_E64as_v4");

    /** Static value Standard_E64d_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E64D_V4 = fromString("Standard_E64d_v4");

    /** Static value Standard_E64ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E64DS_V4 = fromString("Standard_E64ds_v4");

    /** Static value Standard_E64i_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E64I_V3 = fromString("Standard_E64i_v3");

    /** Static value Standard_E64is_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E64IS_V3 = fromString("Standard_E64is_v3");

    /** Static value Standard_E64s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E64S_V3 = fromString("Standard_E64s_v3");

    /** Static value Standard_E64s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E64S_V4 = fromString("Standard_E64s_v4");

    /** Static value Standard_E8-2ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E8_2DS_V4 = fromString("Standard_E8-2ds_v4");

    /** Static value Standard_E8-2s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E8_2S_V3 = fromString("Standard_E8-2s_v3");

    /** Static value Standard_E8-2s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E8_2S_V4 = fromString("Standard_E8-2s_v4");

    /** Static value Standard_E8-4ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E8_4DS_V4 = fromString("Standard_E8-4ds_v4");

    /** Static value Standard_E8-4s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E8_4S_V3 = fromString("Standard_E8-4s_v3");

    /** Static value Standard_E8-4s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E8_4S_V4 = fromString("Standard_E8-4s_v4");

    /** Static value Standard_E8_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E8_V3 = fromString("Standard_E8_v3");

    /** Static value Standard_E8_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E8_V4 = fromString("Standard_E8_v4");

    /** Static value Standard_E8a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E8A_V4 = fromString("Standard_E8a_v4");

    /** Static value Standard_E8as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E8AS_V4 = fromString("Standard_E8as_v4");

    /** Static value Standard_E8d_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E8D_V4 = fromString("Standard_E8d_v4");

    /** Static value Standard_E8ds_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E8DS_V4 = fromString("Standard_E8ds_v4");

    /** Static value Standard_E8s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E8S_V3 = fromString("Standard_E8s_v3");

    /** Static value Standard_E8s_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E8S_V4 = fromString("Standard_E8s_v4");

    /** Static value Standard_E96a_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E96A_V4 = fromString("Standard_E96a_v4");

    /** Static value Standard_E96as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_E96AS_V4 = fromString("Standard_E96as_v4");

    /** Static value Standard_F1 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F1 = fromString("Standard_F1");

    /** Static value Standard_F16 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F16 = fromString("Standard_F16");

    /** Static value Standard_F16s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F16S = fromString("Standard_F16s");

    /** Static value Standard_F16s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F16S_V2 = fromString("Standard_F16s_v2");

    /** Static value Standard_F1s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F1S = fromString("Standard_F1s");

    /** Static value Standard_F2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F2 = fromString("Standard_F2");

    /** Static value Standard_F2s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F2S = fromString("Standard_F2s");

    /** Static value Standard_F2s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F2S_V2 = fromString("Standard_F2s_v2");

    /** Static value Standard_F32s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F32S_V2 = fromString("Standard_F32s_v2");

    /** Static value Standard_F4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F4 = fromString("Standard_F4");

    /** Static value Standard_F48s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F48S_V2 = fromString("Standard_F48s_v2");

    /** Static value Standard_F4s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F4S = fromString("Standard_F4s");

    /** Static value Standard_F4s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F4S_V2 = fromString("Standard_F4s_v2");

    /** Static value Standard_F64s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F64S_V2 = fromString("Standard_F64s_v2");

    /** Static value Standard_F72s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F72S_V2 = fromString("Standard_F72s_v2");

    /** Static value Standard_F8 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F8 = fromString("Standard_F8");

    /** Static value Standard_F8s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F8S = fromString("Standard_F8s");

    /** Static value Standard_F8s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_F8S_V2 = fromString("Standard_F8s_v2");

    /** Static value Standard_G1 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_G1 = fromString("Standard_G1");

    /** Static value Standard_G2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_G2 = fromString("Standard_G2");

    /** Static value Standard_G3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_G3 = fromString("Standard_G3");

    /** Static value Standard_G4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_G4 = fromString("Standard_G4");

    /** Static value Standard_G5 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_G5 = fromString("Standard_G5");

    /** Static value Standard_GS1 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_GS1 = fromString("Standard_GS1");

    /** Static value Standard_GS2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_GS2 = fromString("Standard_GS2");

    /** Static value Standard_GS3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_GS3 = fromString("Standard_GS3");

    /** Static value Standard_GS4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_GS4 = fromString("Standard_GS4");

    /** Static value Standard_GS4-4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_GS4_4 = fromString("Standard_GS4-4");

    /** Static value Standard_GS4-8 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_GS4_8 = fromString("Standard_GS4-8");

    /** Static value Standard_GS5 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_GS5 = fromString("Standard_GS5");

    /** Static value Standard_GS5-16 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_GS5_16 = fromString("Standard_GS5-16");

    /** Static value Standard_GS5-8 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_GS5_8 = fromString("Standard_GS5-8");

    /** Static value Standard_H16 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_H16 = fromString("Standard_H16");

    /** Static value Standard_H16_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_H16_PROMO = fromString("Standard_H16_Promo");

    /** Static value Standard_H16m for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_H16M = fromString("Standard_H16m");

    /** Static value Standard_H16m_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_H16M_PROMO = fromString("Standard_H16m_Promo");

    /** Static value Standard_H16mr for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_H16MR = fromString("Standard_H16mr");

    /** Static value Standard_H16mr_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_H16MR_PROMO = fromString("Standard_H16mr_Promo");

    /** Static value Standard_H16r for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_H16R = fromString("Standard_H16r");

    /** Static value Standard_H16r_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_H16R_PROMO = fromString("Standard_H16r_Promo");

    /** Static value Standard_H8 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_H8 = fromString("Standard_H8");

    /** Static value Standard_H8_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_H8_PROMO = fromString("Standard_H8_Promo");

    /** Static value Standard_H8m for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_H8M = fromString("Standard_H8m");

    /** Static value Standard_H8m_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_H8M_PROMO = fromString("Standard_H8m_Promo");

    /** Static value Standard_HB120rs_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_HB120RS_V2 = fromString("Standard_HB120rs_v2");

    /** Static value Standard_HB60rs for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_HB60RS = fromString("Standard_HB60rs");

    /** Static value Standard_HC44rs for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_HC44RS = fromString("Standard_HC44rs");

    /** Static value Standard_L16s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_L16S = fromString("Standard_L16s");

    /** Static value Standard_L16s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_L16S_V2 = fromString("Standard_L16s_v2");

    /** Static value Standard_L32s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_L32S = fromString("Standard_L32s");

    /** Static value Standard_L32s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_L32S_V2 = fromString("Standard_L32s_v2");

    /** Static value Standard_L48s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_L48S_V2 = fromString("Standard_L48s_v2");

    /** Static value Standard_L4s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_L4S = fromString("Standard_L4s");

    /** Static value Standard_L64s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_L64S_V2 = fromString("Standard_L64s_v2");

    /** Static value Standard_L80s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_L80S_V2 = fromString("Standard_L80s_v2");

    /** Static value Standard_L8s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_L8S = fromString("Standard_L8s");

    /** Static value Standard_L8s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_L8S_V2 = fromString("Standard_L8s_v2");

    /** Static value Standard_M128 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M128 = fromString("Standard_M128");

    /** Static value Standard_M128-32ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M128_32MS = fromString("Standard_M128-32ms");

    /** Static value Standard_M128-64ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M128_64MS = fromString("Standard_M128-64ms");

    /** Static value Standard_M128m for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M128M = fromString("Standard_M128m");

    /** Static value Standard_M128ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M128MS = fromString("Standard_M128ms");

    /** Static value Standard_M128s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M128S = fromString("Standard_M128s");

    /** Static value Standard_M16-4ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M16_4MS = fromString("Standard_M16-4ms");

    /** Static value Standard_M16-8ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M16_8MS = fromString("Standard_M16-8ms");

    /** Static value Standard_M16ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M16MS = fromString("Standard_M16ms");

    /** Static value Standard_M208ms_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M208MS_V2 = fromString("Standard_M208ms_v2");

    /** Static value Standard_M208s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M208S_V2 = fromString("Standard_M208s_v2");

    /** Static value Standard_M32-16ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M32_16MS = fromString("Standard_M32-16ms");

    /** Static value Standard_M32-8ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M32_8MS = fromString("Standard_M32-8ms");

    /** Static value Standard_M32ls for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M32LS = fromString("Standard_M32ls");

    /** Static value Standard_M32ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M32MS = fromString("Standard_M32ms");

    /** Static value Standard_M32ts for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M32TS = fromString("Standard_M32ts");

    /** Static value Standard_M416-208ms_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M416_208MS_V2 = fromString("Standard_M416-208ms_v2");

    /** Static value Standard_M416-208s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M416_208S_V2 = fromString("Standard_M416-208s_v2");

    /** Static value Standard_M416ms_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M416MS_V2 = fromString("Standard_M416ms_v2");

    /** Static value Standard_M416s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M416S_V2 = fromString("Standard_M416s_v2");

    /** Static value Standard_M64 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M64 = fromString("Standard_M64");

    /** Static value Standard_M64-16ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M64_16MS = fromString("Standard_M64-16ms");

    /** Static value Standard_M64-32ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M64_32MS = fromString("Standard_M64-32ms");

    /** Static value Standard_M64ls for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M64LS = fromString("Standard_M64ls");

    /** Static value Standard_M64m for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M64M = fromString("Standard_M64m");

    /** Static value Standard_M64ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M64MS = fromString("Standard_M64ms");

    /** Static value Standard_M64s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M64S = fromString("Standard_M64s");

    /** Static value Standard_M8-2ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M8_2MS = fromString("Standard_M8-2ms");

    /** Static value Standard_M8-4ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M8_4MS = fromString("Standard_M8-4ms");

    /** Static value Standard_M8ms for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_M8MS = fromString("Standard_M8ms");

    /** Static value Standard_NC12 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NC12 = fromString("Standard_NC12");

    /** Static value Standard_NC12_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NC12_PROMO = fromString("Standard_NC12_Promo");

    /** Static value Standard_NC12s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NC12S_V2 = fromString("Standard_NC12s_v2");

    /** Static value Standard_NC12s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NC12S_V3 = fromString("Standard_NC12s_v3");

    /** Static value Standard_NC24 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NC24 = fromString("Standard_NC24");

    /** Static value Standard_NC24_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NC24_PROMO = fromString("Standard_NC24_Promo");

    /** Static value Standard_NC24r for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NC24R = fromString("Standard_NC24r");

    /** Static value Standard_NC24r_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NC24R_PROMO = fromString("Standard_NC24r_Promo");

    /** Static value Standard_NC24rs_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NC24RS_V2 = fromString("Standard_NC24rs_v2");

    /** Static value Standard_NC24rs_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NC24RS_V3 = fromString("Standard_NC24rs_v3");

    /** Static value Standard_NC24s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NC24S_V2 = fromString("Standard_NC24s_v2");

    /** Static value Standard_NC24s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NC24S_V3 = fromString("Standard_NC24s_v3");

    /** Static value Standard_NC6 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NC6 = fromString("Standard_NC6");

    /** Static value Standard_NC6_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NC6_PROMO = fromString("Standard_NC6_Promo");

    /** Static value Standard_NC6s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NC6S_V2 = fromString("Standard_NC6s_v2");

    /** Static value Standard_NC6s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NC6S_V3 = fromString("Standard_NC6s_v3");

    /** Static value Standard_ND12s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_ND12S = fromString("Standard_ND12s");

    /** Static value Standard_ND24rs for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_ND24RS = fromString("Standard_ND24rs");

    /** Static value Standard_ND24s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_ND24S = fromString("Standard_ND24s");

    /** Static value Standard_ND40rs_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_ND40RS_V2 = fromString("Standard_ND40rs_v2");

    /** Static value Standard_ND40s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_ND40S_V3 = fromString("Standard_ND40s_v3");

    /** Static value Standard_ND6s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_ND6S = fromString("Standard_ND6s");

    /** Static value Standard_NP10s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NP10S = fromString("Standard_NP10s");

    /** Static value Standard_NP20s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NP20S = fromString("Standard_NP20s");

    /** Static value Standard_NP40s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NP40S = fromString("Standard_NP40s");

    /** Static value Standard_NV12 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NV12 = fromString("Standard_NV12");

    /** Static value Standard_NV12_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NV12_PROMO = fromString("Standard_NV12_Promo");

    /** Static value Standard_NV12s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NV12S_V2 = fromString("Standard_NV12s_v2");

    /** Static value Standard_NV12s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NV12S_V3 = fromString("Standard_NV12s_v3");

    /** Static value Standard_NV16as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NV16AS_V4 = fromString("Standard_NV16as_v4");

    /** Static value Standard_NV24 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NV24 = fromString("Standard_NV24");

    /** Static value Standard_NV24_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NV24_PROMO = fromString("Standard_NV24_Promo");

    /** Static value Standard_NV24s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NV24S_V2 = fromString("Standard_NV24s_v2");

    /** Static value Standard_NV24s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NV24S_V3 = fromString("Standard_NV24s_v3");

    /** Static value Standard_NV32as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NV32AS_V4 = fromString("Standard_NV32as_v4");

    /** Static value Standard_NV48s_v3 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NV48S_V3 = fromString("Standard_NV48s_v3");

    /** Static value Standard_NV4as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NV4AS_V4 = fromString("Standard_NV4as_v4");

    /** Static value Standard_NV6 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NV6 = fromString("Standard_NV6");

    /** Static value Standard_NV6_Promo for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NV6_PROMO = fromString("Standard_NV6_Promo");

    /** Static value Standard_NV6s_v2 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NV6S_V2 = fromString("Standard_NV6s_v2");

    /** Static value Standard_NV8as_v4 for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_NV8AS_V4 = fromString("Standard_NV8as_v4");

    /** Static value Standard_PB6s for VirtualMachineSizeTypes. */
    public static final VirtualMachineSizeTypes STANDARD_PB6S = fromString("Standard_PB6s");


    /**
     * Creates or finds a VirtualMachineSizeTypes from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding VirtualMachineSizeTypes.
     */
    @JsonCreator
    public static VirtualMachineSizeTypes fromString(String name) {
        return fromString(name, VirtualMachineSizeTypes.class);
    }

    /** @return known VirtualMachineSizeTypes values. */
    public static Collection<VirtualMachineSizeTypes> values() {
        return values(VirtualMachineSizeTypes.class);
    }
}
