// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.deviceprovisioningservices;

import com.azure.resourcemanager.deviceprovisioningservices.models.AllocationPolicy;
import com.azure.resourcemanager.deviceprovisioningservices.models.IotDpsSku;
import com.azure.resourcemanager.deviceprovisioningservices.models.IotDpsSkuInfo;

import java.util.Arrays;
import java.util.List;

public class Constants
{
    static final String DEFAULT_LOCATION = "WestUS2";
    static final String OWNER_ACCESS_KEY_NAME = "provisioningserviceowner";
    static final String IOTHUB_OWNER_ACCESS_KEY_NAME = "iothubowner";

    public static class DefaultSku
    {
        static final String NAME = "S1";
        static final Long CAPACITY = 1L;
        static IotDpsSkuInfo INSTANCE = new IotDpsSkuInfo()
            .withCapacity(Constants.DefaultSku.CAPACITY)
            .withName(IotDpsSku.fromString(Constants.DefaultSku.NAME));
    }

    public static class Certificate
    {
        static final String CONTENT =
            "MIIBvjCCAWOgAwIBAgIQG6PoBFT6GLJGNKn/EaxltTAKBggqhkjOPQQDAjAcMRow"
            + "GAYDVQQDDBFBenVyZSBJb1QgUm9vdCBDQTAeFw0xNzExMDMyMDUyNDZaFw0xNzEy"
            + "MDMyMTAyNDdaMBwxGjAYBgNVBAMMEUF6dXJlIElvVCBSb290IENBMFkwEwYHKoZI"
            + "zj0CAQYIKoZIzj0DAQcDQgAE+CgpnW3K+KRNIi/U6Zqe/Al9m8PExHX2KgakmGTf"
            + "E04nNBwnSoygWb0ekqpT+Lm+OP56LMMe9ynVNryDEr9OSKOBhjCBgzAOBgNVHQ8B"
            + "Af8EBAMCAgQwHQYDVR0lBBYwFAYIKwYBBQUHAwIGCCsGAQUFBwMBMB8GA1UdEQQY"
            + "MBaCFENOPUF6dXJlIElvVCBSb290IENBMBIGA1UdEwEB/wQIMAYBAf8CAQwwHQYD"
            + "VR0OBBYEFDjiklfHQzw1G0A33BcmRQTjAivTMAoGCCqGSM49BAMCA0kAMEYCIQCt"
            + "jJ4bAvoYuDhwr92Kk+OkvpPF+qBFiRfrA/EC4YGtzQIhAO79WPtbUnBQ5fsQnW2a"
            + "UAT4yJGWL+7l4/qfmqblb96n";

        static final String NAME = "DPStestCert";
        static final String SUBJECT = "Azure IoT Root CA";
        static final String THUMBPRINT = "9F0983E8F2DB2DB3582997FEF331247D872DEE32";
    }

    static final List<AllocationPolicy> ALLOCATION_POLICIES = Arrays.asList(AllocationPolicy.GEO_LATENCY, AllocationPolicy.HASHED, AllocationPolicy.STATIC);
}
