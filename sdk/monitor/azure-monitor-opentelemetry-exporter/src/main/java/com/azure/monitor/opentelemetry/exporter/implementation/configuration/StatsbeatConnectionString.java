// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.configuration;

import reactor.util.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public final class StatsbeatConnectionString {

    // visible for testing
    static final String EU_REGION_STATSBEAT_IKEY = "7dc56bab-3c0c-4e9f-9ebb-d1acadee8d0f"; // westeu-aistatsbeat
    static final String EU_REGION_STATSBEAT_ENDPOINT = "https://westeurope-5.in.applicationinsights.azure.com/";
    static final String NON_EU_REGION_STATSBEAT_IKEY = "c4a29126-a7cb-47e5-b348-11414998b11e"; // workspace-aistatsbeat
    static final String NON_EU_REGION_STATSBEAT_ENDPOINT = "https://westus-0.in.applicationinsights.azure.com/";

    private static final Pattern pattern = Pattern.compile("^https?://(?:www\\.)?([^/.-]+)");

    private static final Set<String> EU_REGION_GEO_SET
        = new HashSet<>(asList("westeurope", "northeurope", "francecentral", "francesouth", "germanywestcentral",
            "norwayeast", "norwaywest", "swedencentral", "switzerlandnorth", "switzerlandwest", "uksouth", "ukwest"));

    private static final Set<String> NON_EU_REGION_GEO_SET = new HashSet<>(asList("eastasia", "southeastasia",
        "chinaeast2", "chinaeast3", "chinanorth3", "centralindia", "southindia", "jioindiacentral", "jioindiawest",
        "japaneast", "japanwest", "koreacentral", "koreasouth", "australiacentral", "australiacentral2",
        "australiaeast", "australiasoutheast", "canadacentral", "canadaeast", "qatarcentral", "uaecentral", "uaenorth",
        "southafricanorth", "brazilsouth", "brazilsoutheast", "centralus", "eastus", "eastus2", "northcentralus",
        "southcentralus", "westus", "westus2", "westus3"));

    private final String ingestionEndpoint;
    private final String instrumentationKey;

    @Nullable
    public static StatsbeatConnectionString create(ConnectionString connectionString,
        @Nullable String instrumentationKey, @Nullable String ingestionEndpoint) {

        if (instrumentationKey == null || instrumentationKey.isEmpty()) {
            InstrumentationKeyEndpointPair pair = StatsbeatConnectionString
                .getInstrumentationKeyAndEndpointPair(connectionString.getIngestionEndpoint());

            // Statsbeat will not get collected when customer's stamp specific region is not found in our
            // known non-EU and EU lists
            if (pair == null) {
                return null;
            }

            instrumentationKey = pair.instrumentationKey;
            ingestionEndpoint = pair.endpoint;
        }

        URL endpointUrl;
        if (!ingestionEndpoint.endsWith("/")) {
            ingestionEndpoint += "/";
        }
        try {
            endpointUrl = new URI(ingestionEndpoint).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalArgumentException("could not construct statsbeat endpoint uri", e);
        }

        return new StatsbeatConnectionString(endpointUrl, instrumentationKey);
    }

    private StatsbeatConnectionString(URL ingestionEndpoint, String instrumentationKey) {
        this.ingestionEndpoint = ingestionEndpoint.toExternalForm();
        this.instrumentationKey = instrumentationKey;
    }

    public String getIngestionEndpoint() {
        return ingestionEndpoint;
    }

    public String getInstrumentationKey() {
        return instrumentationKey;
    }

    // visible for testing
    @Nullable
    static InstrumentationKeyEndpointPair getInstrumentationKeyAndEndpointPair(String customerEndpoint) {
        String geo = getGeoWithoutStampSpecific(customerEndpoint);
        if (geo != null && EU_REGION_GEO_SET.contains(geo.toLowerCase(Locale.ROOT))) {
            return new InstrumentationKeyEndpointPair(EU_REGION_STATSBEAT_IKEY, EU_REGION_STATSBEAT_ENDPOINT);
        } else if (geo != null && NON_EU_REGION_GEO_SET.contains(geo.toLowerCase(Locale.ROOT))) {
            return new InstrumentationKeyEndpointPair(NON_EU_REGION_STATSBEAT_IKEY, NON_EU_REGION_STATSBEAT_ENDPOINT);
        }

        return null;
    }

    // visible for testing
    @Nullable
    static String getGeoWithoutStampSpecific(String endpointUrl) {
        Matcher matcher = pattern.matcher(endpointUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    static class InstrumentationKeyEndpointPair {
        public final String instrumentationKey;
        public final String endpoint;

        public InstrumentationKeyEndpointPair(String instrumentationKey, String endpoint) {
            this.instrumentationKey = instrumentationKey;
            this.endpoint = endpoint;
        }
    }
}
