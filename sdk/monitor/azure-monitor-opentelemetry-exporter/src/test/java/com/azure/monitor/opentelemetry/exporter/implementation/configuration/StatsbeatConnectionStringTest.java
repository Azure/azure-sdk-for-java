// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.configuration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StatsbeatConnectionStringTest {

    @Test
    public void testGetGeoWithoutStampSpecific() {
        String customerIngestionEndpoint = "https://fakehost-1.applicationinsights.azure.com/";
        assertThat(StatsbeatConnectionString.getGeoWithoutStampSpecific(customerIngestionEndpoint))
            .isEqualTo("fakehost");

        customerIngestionEndpoint = "http://fakehost-2.example.com/";
        assertThat(StatsbeatConnectionString.getGeoWithoutStampSpecific(customerIngestionEndpoint))
            .isEqualTo("fakehost");

        customerIngestionEndpoint = "https://fakehost1-3.com/";
        assertThat(StatsbeatConnectionString.getGeoWithoutStampSpecific(customerIngestionEndpoint))
            .isEqualTo("fakehost1");

        customerIngestionEndpoint = "http://fakehost2-4.com/";
        assertThat(StatsbeatConnectionString.getGeoWithoutStampSpecific(customerIngestionEndpoint))
            .isEqualTo("fakehost2");

        customerIngestionEndpoint = "http://www.fakehost3-5.com/";
        assertThat(StatsbeatConnectionString.getGeoWithoutStampSpecific(customerIngestionEndpoint))
            .isEqualTo("fakehost3");

        customerIngestionEndpoint = "https://www.fakehostabc-6.com/";
        assertThat(StatsbeatConnectionString.getGeoWithoutStampSpecific(customerIngestionEndpoint))
            .isEqualTo("fakehostabc");

        customerIngestionEndpoint = "http://fakehostabc-7.example.com/";
        assertThat(StatsbeatConnectionString.getGeoWithoutStampSpecific(customerIngestionEndpoint))
            .isEqualTo("fakehostabc");

        customerIngestionEndpoint = "http://www.fakehostabc-8.example.com/";
        assertThat(StatsbeatConnectionString.getGeoWithoutStampSpecific(customerIngestionEndpoint))
            .isEqualTo("fakehostabc");

        customerIngestionEndpoint = "https://fakehostabc1-9.com/";
        assertThat(StatsbeatConnectionString.getGeoWithoutStampSpecific(customerIngestionEndpoint))
            .isEqualTo("fakehostabc1");

        customerIngestionEndpoint = "https://fakehostabc.com/";
        assertThat(StatsbeatConnectionString.getGeoWithoutStampSpecific(customerIngestionEndpoint))
            .isEqualTo("fakehostabc");

        customerIngestionEndpoint = "https://fakehostabc/v2/track";
        assertThat(StatsbeatConnectionString.getGeoWithoutStampSpecific(customerIngestionEndpoint))
            .isEqualTo("fakehostabc");
    }

    @Test
    public void testUpdateStatsbeatConnectionString() throws Exception {
        // case 1
        // customer ikey is in non-eu
        // Statsbeat config ikey is in eu
        // use Statsbeat config ikey
        ConnectionString connectionString = ConnectionString.parse(
            "InstrumentationKey=00000000-0000-0000-0000-000000000000;IngestionEndpoint=https://westus2-1.example.com/");
        String ikeyConfig = "00000000-0000-0000-0000-000000000001";
        String endpointConfig = "https://westeurope-2.example.com";
        StatsbeatConnectionString statsbeatConnectionString
            = StatsbeatConnectionString.create(connectionString, ikeyConfig, endpointConfig);
        assertThat(statsbeatConnectionString.getInstrumentationKey()).isEqualTo(ikeyConfig);
        assertThat(statsbeatConnectionString.getIngestionEndpoint().toString()).isEqualTo(endpointConfig + "/");

        // case 2
        // customer ikey is in non-eu
        // Statsbeat config ikey is in non-eu
        // use Statsbeat config ikey
        connectionString = ConnectionString.parse(
            "InstrumentationKey=00000000-0000-0000-0000-000000000000;IngestionEndpoint=https://westus2-1.example.com/");
        ikeyConfig = "00000000-0000-0000-0000-000000000002";
        endpointConfig = "https://westus2-2.example.com";
        statsbeatConnectionString = StatsbeatConnectionString.create(connectionString, ikeyConfig, endpointConfig);
        assertThat(statsbeatConnectionString.getInstrumentationKey()).isEqualTo(ikeyConfig);
        assertThat(statsbeatConnectionString.getIngestionEndpoint().toString()).isEqualTo(endpointConfig + "/");

        // case 3
        // customer ikey is in non-eu
        // no Statsbeat config
        // use Statsbeat non-eu
        connectionString = ConnectionString.parse(
            "InstrumentationKey=00000000-0000-0000-0000-000000000000;IngestionEndpoint=https://westus2-1.example.com/");
        statsbeatConnectionString = StatsbeatConnectionString.create(connectionString, null, null);
        assertThat(statsbeatConnectionString.getInstrumentationKey())
            .isEqualTo(StatsbeatConnectionString.NON_EU_REGION_STATSBEAT_IKEY);
        assertThat(statsbeatConnectionString.getIngestionEndpoint().toString())
            .isEqualTo(StatsbeatConnectionString.NON_EU_REGION_STATSBEAT_ENDPOINT);

        // case 4
        // customer is in eu
        // Statsbeat config ikey is in non-eu
        // use Statsbeat config's ikey
        connectionString = ConnectionString.parse(
            "InstrumentationKey=00000000-0000-0000-0000-000000000003;IngestionEndpoint=https://westeurope-1.example.com/");
        ikeyConfig = "00000000-0000-0000-0000-000000000004";
        endpointConfig = "https://westus2-4.example.com";
        statsbeatConnectionString = StatsbeatConnectionString.create(connectionString, ikeyConfig, endpointConfig);
        assertThat(statsbeatConnectionString.getInstrumentationKey()).isEqualTo(ikeyConfig);
        assertThat(statsbeatConnectionString.getIngestionEndpoint().toString()).isEqualTo(endpointConfig + "/");

        // case 5
        // customer is in eu
        // Statsbeat config ikey is in eu
        // use Statsbeat config's ikey
        connectionString = ConnectionString.parse(
            "InstrumentationKey=00000000-0000-0000-0000-000000000003;IngestionEndpoint=https://westeurope-1.example.com/");
        ikeyConfig = "00000000-0000-0000-0000-000000000005";
        endpointConfig = "https://francesouth-1.example.com";
        statsbeatConnectionString = StatsbeatConnectionString.create(connectionString, ikeyConfig, endpointConfig);
        assertThat(statsbeatConnectionString.getInstrumentationKey()).isEqualTo(ikeyConfig);
        assertThat(statsbeatConnectionString.getIngestionEndpoint().toString()).isEqualTo(endpointConfig + "/");

        // case 6
        // customer is in eu
        // no statsbeat config
        // use Statsbeat eu
        connectionString = ConnectionString.parse(
            "InstrumentationKey=00000000-0000-0000-0000-000000000003;IngestionEndpoint=https://westeurope-1.example.com/");
        statsbeatConnectionString = StatsbeatConnectionString.create(connectionString, null, null);
        assertThat(statsbeatConnectionString.getInstrumentationKey())
            .isEqualTo(StatsbeatConnectionString.EU_REGION_STATSBEAT_IKEY);
        assertThat(statsbeatConnectionString.getIngestionEndpoint().toString())
            .isEqualTo(StatsbeatConnectionString.EU_REGION_STATSBEAT_ENDPOINT);
    }

    @Test
    public void testGetInstrumentationKeyAndEndpointPairEuRegion() {
        StatsbeatConnectionString.InstrumentationKeyEndpointPair pair
            = StatsbeatConnectionString.getInstrumentationKeyAndEndpointPair("https://northeurope-2.example.com/");
        assertThat(pair.instrumentationKey).isEqualTo(StatsbeatConnectionString.EU_REGION_STATSBEAT_IKEY);
        assertThat(pair.endpoint).isEqualTo(StatsbeatConnectionString.EU_REGION_STATSBEAT_ENDPOINT);
    }

    @Test
    public void testGetInstrumentationKeyAndEndpointPairNonEuRegion() {
        StatsbeatConnectionString.InstrumentationKeyEndpointPair pair
            = StatsbeatConnectionString.getInstrumentationKeyAndEndpointPair("https://westus2-2.example.com/");
        assertThat(pair.instrumentationKey).isEqualTo(StatsbeatConnectionString.NON_EU_REGION_STATSBEAT_IKEY);
        assertThat(pair.endpoint).isEqualTo(StatsbeatConnectionString.NON_EU_REGION_STATSBEAT_ENDPOINT);
    }
}
