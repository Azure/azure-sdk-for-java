// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.datafactory.models.SqlServerBaseLinkedServiceTypeProperties;

public final class SqlServerBaseLinkedServiceTypePropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        SqlServerBaseLinkedServiceTypeProperties model = BinaryData.fromString(
            "{\"server\":\"datanhlvagug\",\"database\":\"datadwtfmfjkrz\",\"encrypt\":\"datahaljomgzorprrapt\",\"trustServerCertificate\":\"datajenylgebrsnvof\",\"hostNameInCertificate\":\"datazvbploazc\",\"applicationIntent\":\"datahgermm\",\"connectTimeout\":\"datakbxui\",\"connectRetryCount\":\"dataoaw\",\"connectRetryInterval\":\"datagpubeqkwxzmuz\",\"loadBalanceTimeout\":\"dataeuyg\",\"commandTimeout\":\"datahauhe\",\"integratedSecurity\":\"dataswe\",\"failoverPartner\":\"datagxxzxwrnqwdjvl\",\"maxPoolSize\":\"datazxyylwsfxqcmej\",\"minPoolSize\":\"datajcbciuagakmxg\",\"multipleActiveResultSets\":\"datamavllp\",\"multiSubnetFailover\":\"dataguzsyfwamhm\",\"packetSize\":\"dataxxb\",\"pooling\":\"datagwnompvy\"}")
            .toObject(SqlServerBaseLinkedServiceTypeProperties.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        SqlServerBaseLinkedServiceTypeProperties model
            = new SqlServerBaseLinkedServiceTypeProperties().withServer("datanhlvagug")
                .withDatabase("datadwtfmfjkrz")
                .withEncrypt("datahaljomgzorprrapt")
                .withTrustServerCertificate("datajenylgebrsnvof")
                .withHostnameInCertificate("datazvbploazc")
                .withApplicationIntent("datahgermm")
                .withConnectTimeout("datakbxui")
                .withConnectRetryCount("dataoaw")
                .withConnectRetryInterval("datagpubeqkwxzmuz")
                .withLoadBalanceTimeout("dataeuyg")
                .withCommandTimeout("datahauhe")
                .withIntegratedSecurity("dataswe")
                .withFailoverPartner("datagxxzxwrnqwdjvl")
                .withMaxPoolSize("datazxyylwsfxqcmej")
                .withMinPoolSize("datajcbciuagakmxg")
                .withMultipleActiveResultSets("datamavllp")
                .withMultiSubnetFailover("dataguzsyfwamhm")
                .withPacketSize("dataxxb")
                .withPooling("datagwnompvy");
        model = BinaryData.fromObject(model).toObject(SqlServerBaseLinkedServiceTypeProperties.class);
    }
}
