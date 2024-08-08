// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.configuration;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConnectionStringParsingTests {

    @Test
    void minimalString() throws Exception {
        String ikey = "fake-ikey";
        String cs = "InstrumentationKey=" + ikey;

        ConnectionString parsed = ConnectionString.parse(cs);
        assertThat(parsed.getInstrumentationKey()).isEqualTo(ikey);
        assertThat(parsed.getIngestionEndpoint()).isEqualTo(DefaultEndpoints.INGESTION_ENDPOINT);
        assertThat(parsed.getLiveEndpoint()).isEqualTo(new URL(DefaultEndpoints.LIVE_ENDPOINT));
    }

    @Test
    void ikeyWithSuffix() throws Exception {
        String ikey = "fake-ikey";
        String suffix = "ai.example.com";
        String cs = "InstrumentationKey=" + ikey + ";EndpointSuffix=" + suffix;
        String expectedIngestionEndpoint
            = "https://" + ConnectionStringBuilder.EndpointPrefixes.INGESTION_ENDPOINT_PREFIX + "." + suffix + "/";
        URL expectedLiveEndpoint
            = new URL("https://" + ConnectionStringBuilder.EndpointPrefixes.LIVE_ENDPOINT_PREFIX + "." + suffix + "/");

        ConnectionString parsed = ConnectionString.parse(cs);
        assertThat(parsed.getInstrumentationKey()).isEqualTo(ikey);
        assertThat(parsed.getIngestionEndpoint()).isEqualTo(expectedIngestionEndpoint);
        assertThat(parsed.getLiveEndpoint()).isEqualTo(expectedLiveEndpoint);
    }

    @Test
    void suffixWithPathRetainsThePath() throws Exception {
        String ikey = "fake-ikey";
        String suffix = "ai.example.com/my-proxy-app/doProxy";
        String cs = "InstrumentationKey=" + ikey + ";EndpointSuffix=" + suffix;
        String expectedIngestionEndpoint
            = "https://" + ConnectionStringBuilder.EndpointPrefixes.INGESTION_ENDPOINT_PREFIX + "." + suffix + "/";
        URL expectedLiveEndpoint
            = new URL("https://" + ConnectionStringBuilder.EndpointPrefixes.LIVE_ENDPOINT_PREFIX + "." + suffix + "/");

        ConnectionString parsed = ConnectionString.parse(cs);
        assertThat(parsed.getInstrumentationKey()).isEqualTo(ikey);
        assertThat(parsed.getIngestionEndpoint()).isEqualTo(expectedIngestionEndpoint);
        assertThat(parsed.getLiveEndpoint()).isEqualTo(expectedLiveEndpoint);
    }

    @Test
    void suffixSupportsPort() throws Exception {
        String ikey = "fake-ikey";
        String suffix = "ai.example.com:9999";
        String cs = "InstrumentationKey=" + ikey + ";EndpointSuffix=" + suffix;
        String expectedIngestionEndpoint
            = "https://" + ConnectionStringBuilder.EndpointPrefixes.INGESTION_ENDPOINT_PREFIX + "." + suffix + "/";
        URL expectedLiveEndpoint
            = new URL("https://" + ConnectionStringBuilder.EndpointPrefixes.LIVE_ENDPOINT_PREFIX + "." + suffix + "/");

        ConnectionString parsed = ConnectionString.parse(cs);
        assertThat(parsed.getInstrumentationKey()).isEqualTo(ikey);
        assertThat(parsed.getIngestionEndpoint()).isEqualTo(expectedIngestionEndpoint);
        assertThat(parsed.getLiveEndpoint()).isEqualTo(expectedLiveEndpoint);
    }

    @Test
    void ikeyWithExplicitEndpoints() throws Exception {
        String ikey = "fake-ikey";
        String expectedIngestionEndpoint = "https://ingestion.example.com/";
        String liveHost = "https://live.example.com";
        URL expectedLiveEndpoint = new URL(liveHost + "/");

        String cs = "InstrumentationKey=" + ikey + ";IngestionEndpoint=" + expectedIngestionEndpoint + ";LiveEndpoint="
            + liveHost;

        ConnectionString parsed = ConnectionString.parse(cs);
        assertThat(parsed.getInstrumentationKey()).isEqualTo(ikey);
        assertThat(parsed.getIngestionEndpoint()).isEqualTo(expectedIngestionEndpoint);
        assertThat(parsed.getLiveEndpoint()).isEqualTo(expectedLiveEndpoint);
    }

    @Test
    void explicitEndpointOverridesSuffix() throws Exception {
        String ikey = "fake-ikey";
        String suffix = "ai.example.com";
        String expectedIngestionEndpoint = "https://ingestion.example.com/";
        URL expectedLiveEndpoint
            = new URL("https://" + ConnectionStringBuilder.EndpointPrefixes.LIVE_ENDPOINT_PREFIX + "." + suffix + "/");
        String cs = "InstrumentationKey=" + ikey + ";IngestionEndpoint=" + expectedIngestionEndpoint
            + ";EndpointSuffix=" + suffix;

        ConnectionString parsed = ConnectionString.parse(cs);
        assertThat(parsed.getInstrumentationKey()).isEqualTo(ikey);
        assertThat(parsed.getIngestionEndpoint()).isEqualTo(expectedIngestionEndpoint);
        assertThat(parsed.getLiveEndpoint()).isEqualTo(expectedLiveEndpoint);
    }

    @Test
    void emptyPairIsIgnored() throws MalformedURLException {
        String ikey = "fake-ikey";
        String suffix = "ai.example.com";
        String cs = "InstrumentationKey=" + ikey + ";;EndpointSuffix=" + suffix + ";";
        String expectedIngestionEndpoint
            = "https://" + ConnectionStringBuilder.EndpointPrefixes.INGESTION_ENDPOINT_PREFIX + "." + suffix + "/";
        URL expectedLiveEndpoint
            = new URL("https://" + ConnectionStringBuilder.EndpointPrefixes.LIVE_ENDPOINT_PREFIX + "." + suffix + "/");

        ConnectionString parsed = ConnectionString.parse(cs);
        assertThat(parsed.getInstrumentationKey()).isEqualTo(ikey);
        assertThat(parsed.getIngestionEndpoint()).isEqualTo(expectedIngestionEndpoint);
        assertThat(parsed.getLiveEndpoint()).isEqualTo(expectedLiveEndpoint);
    }

    @Test
    void emptyKeyIsIgnored() throws MalformedURLException {
        String ikey = "fake-ikey";
        String cs = "InstrumentationKey=" + ikey + ";=1234";
        String expectedIngestionEndpoint = DefaultEndpoints.INGESTION_ENDPOINT;
        URL expectedLiveEndpoint = new URL(DefaultEndpoints.LIVE_ENDPOINT);

        ConnectionString parsed = ConnectionString.parse(cs);
        assertThat(parsed.getInstrumentationKey()).isEqualTo(ikey);
        assertThat(parsed.getIngestionEndpoint()).isEqualTo(expectedIngestionEndpoint);
        assertThat(parsed.getLiveEndpoint()).isEqualTo(expectedLiveEndpoint);
    }

    @Test
    void emptyValueIsSameAsUnset() throws Exception {
        String ikey = "fake-ikey";
        String cs = "InstrumentationKey=" + ikey + ";EndpointSuffix=";

        ConnectionString parsed = ConnectionString.parse(cs);
        assertThat(parsed.getInstrumentationKey()).isEqualTo(ikey);
        assertThat(parsed.getIngestionEndpoint()).isEqualTo(DefaultEndpoints.INGESTION_ENDPOINT);
        assertThat(parsed.getLiveEndpoint()).isEqualTo(new URL(DefaultEndpoints.LIVE_ENDPOINT));
    }

    @Test
    void caseInsensitiveParsing() {
        String ikey = "fake-ikey";
        String live = "https://live.something.com";
        String profiler = "https://prof.something.com";
        String cs1 = "InstrumentationKey=" + ikey + ";LiveEndpoint=" + live + ";ProfilerEndpoint=" + profiler;
        String cs2 = "instRUMentationkEY=" + ikey + ";LivEEndPOINT=" + live + ";ProFILErEndPOinT=" + profiler;

        ConnectionString parsed = ConnectionString.parse(cs1);
        ConnectionString parsed2 = ConnectionString.parse(cs2);

        assertThat(parsed2.getInstrumentationKey()).isEqualTo(parsed.getInstrumentationKey());
        assertThat(parsed2.getIngestionEndpoint()).isEqualTo(parsed.getIngestionEndpoint());
        assertThat(parsed2.getLiveEndpoint()).isEqualTo(parsed.getLiveEndpoint());
        assertThat(parsed2.getProfilerEndpoint()).isEqualTo(parsed.getProfilerEndpoint());
    }

    @Test
    void orderDoesNotMatter() {
        String ikey = "fake-ikey";
        String live = "https://live.something.com";
        String profiler = "https://prof.something.com";
        String snapshot = "https://whatever.snappy.com";
        String cs1 = "InstrumentationKey=" + ikey + ";LiveEndpoint=" + live + ";ProfilerEndpoint=" + profiler
            + ";SnapshotEndpoint=" + snapshot;
        String cs2 = "SnapshotEndpoint=" + snapshot + ";ProfilerEndpoint=" + profiler + ";InstrumentationKey=" + ikey
            + ";LiveEndpoint=" + live;

        ConnectionString parsed = ConnectionString.parse(cs1);
        ConnectionString parsed2 = ConnectionString.parse(cs2);

        assertThat(parsed2.getInstrumentationKey()).isEqualTo(parsed.getInstrumentationKey());
        assertThat(parsed2.getIngestionEndpoint()).isEqualTo(parsed.getIngestionEndpoint());
        assertThat(parsed2.getLiveEndpoint()).isEqualTo(parsed.getLiveEndpoint());
        assertThat(parsed2.getProfilerEndpoint()).isEqualTo(parsed.getProfilerEndpoint());
    }

    @Test
    void endpointWithNoSchemeIsInvalid() {
        assertThatThrownBy(
            () -> ConnectionString.parse("InstrumentationKey=fake-ikey;IngestionEndpoint=my-ai.example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("IngestionEndpoint");
    }

    @Test
    void endpointWithPathMissingSchemeIsInvalid() {
        assertThatThrownBy(() -> ConnectionString
            .parse("InstrumentationKey=fake-ikey;IngestionEndpoint=my-ai.example.com/path/prefix"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("IngestionEndpoint");
    }

    @Test
    void endpointWithPortMissingSchemeIsInvalid() {
        assertThatThrownBy(
            () -> ConnectionString.parse("InstrumentationKey=fake-ikey;IngestionEndpoint=my-ai.example.com:9999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("IngestionEndpoint");
    }

    @Test
    void httpEndpointKeepsScheme() throws Exception {
        ConnectionString parsed
            = ConnectionString.parse("InstrumentationKey=fake-ikey;IngestionEndpoint=http://my-ai.example.com");
        assertThat(parsed.getIngestionEndpoint()).isEqualTo("http://my-ai.example.com/");
    }

    @Test
    void emptyIkeyValueIsInvalid() {
        assertThatThrownBy(() -> ConnectionString
            .parse("InstrumentationKey=;IngestionEndpoint=https://ingestion.example.com;EndpointSuffix=ai.example.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nonKeyValueStringIsInvalid() {
        assertThatThrownBy(() -> ConnectionString.parse(UUID.randomUUID().toString()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    // when more Authorization values are available, create a copy of this test. For example,
    // given "Authorization=Xyz", this would fail because the 'Xyz' key/value pair is missing.
    void missingInstrumentationKeyIsInvalid() {
        assertThatThrownBy(() -> ConnectionString.parse("LiveEndpoint=https://live.example.com"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidUrlIsInvalidConnectionString() {
        assertThatThrownBy(() -> ConnectionString.parse("InstrumentationKey=fake-ikey;LiveEndpoint=httpx://host"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasCauseInstanceOf(MalformedURLException.class)
            .hasMessageContaining("LiveEndpoint");
    }

    @Test
    void giantValuesAreNotAllowed() {
        StringBuilder bigIkey = new StringBuilder();
        for (int i = 0; i < ConnectionStringBuilder.CONNECTION_STRING_MAX_LENGTH * 2; i++) {
            bigIkey.append('0');
        }

        assertThatThrownBy(() -> ConnectionString.parse("InstrumentationKey=" + bigIkey))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(Integer.toString(ConnectionStringBuilder.CONNECTION_STRING_MAX_LENGTH));
    }

    @Test
    void resetEndpointUrlTest() {
        String fakeConnectionString
            = "InstrumentationKey=fake-key;IngestionEndpoint=https://ingestion.example.com/;LiveEndpoint=https://live.example.com/";
        ConnectionString parsed = ConnectionString.parse(fakeConnectionString);

        assertThat(parsed.getIngestionEndpoint()).isEqualTo("https://ingestion.example.com/");
        assertThat(parsed.getLiveEndpoint().toExternalForm()).isEqualTo("https://live.example.com/");

        String newFakeConnectionString
            = "InstrumentationKey=new-fake-key;IngestionEndpoint=https://new-ingestion.example.com/;LiveEndpoint=https://new-live.example.com/";
        parsed = ConnectionString.parse(newFakeConnectionString);

        assertThat(parsed.getIngestionEndpoint()).isEqualTo("https://new-ingestion.example.com/");
        assertThat(parsed.getLiveEndpoint().toExternalForm()).isEqualTo("https://new-live.example.com/");

        String newerFakeConnectionString
            = "InstrumentationKey=newer-fake-key;IngestionEndpoint=https://newer-ingestion.example.com/;LiveEndpoint=https://newer-live.example.com/";
        parsed = ConnectionString.parse(newerFakeConnectionString);

        assertThat(parsed.getIngestionEndpoint()).isEqualTo("https://newer-ingestion.example.com/");
        assertThat(parsed.getLiveEndpoint().toExternalForm()).isEqualTo("https://newer-live.example.com/");
    }
}
