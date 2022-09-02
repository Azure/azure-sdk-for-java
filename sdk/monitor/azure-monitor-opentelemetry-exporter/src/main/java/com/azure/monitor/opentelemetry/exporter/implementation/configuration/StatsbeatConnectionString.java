/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.configuration;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StatsbeatConnectionString {

    // visible for testing
    static final String EU_REGION_STATSBEAT_IKEY =
        "7dc56bab-3c0c-4e9f-9ebb-d1acadee8d0f"; // westeu-aistatsbeat
    static final String EU_REGION_STATSBEAT_ENDPOINT =
        "https://westeurope-5.in.applicationinsights.azure.com/";
    static final String NON_EU_REGION_STATSBEAT_IKEY =
        "c4a29126-a7cb-47e5-b348-11414998b11e"; // workspace-aistatsbeat
    static final String NON_EU_REGION_STATSBEAT_ENDPOINT =
        "https://westus-0.in.applicationinsights.azure.com/";

    private static final Pattern pattern = Pattern.compile("^https?://(?:www\\.)?([^/.-]+)");

    private static final Set<String> EU_REGION_GEO_SET = new HashSet<>(10);

    static {
        EU_REGION_GEO_SET.add("westeurope");
        EU_REGION_GEO_SET.add("northeurope");
        EU_REGION_GEO_SET.add("francecentral");
        EU_REGION_GEO_SET.add("francesouth");
        EU_REGION_GEO_SET.add("germanywestcentral");
        EU_REGION_GEO_SET.add("norwayeast");
        EU_REGION_GEO_SET.add("norwaywest");
        EU_REGION_GEO_SET.add("swedencentral");
        EU_REGION_GEO_SET.add("switzerlandnorth");
        EU_REGION_GEO_SET.add("switzerlandwest");
    }

    private final String ingestionEndpoint;
    private final String instrumentationKey;

    private StatsbeatConnectionString(URL ingestionEndpoint, String instrumentationKey) {
        this.ingestionEndpoint = ingestionEndpoint.toExternalForm();
        this.instrumentationKey = instrumentationKey;
    }

    public static StatsbeatConnectionString create(
        ConnectionString connectionString,
        @Nullable String instrumentationKey,
        @Nullable String ingestionEndpoint) {

        // if customer is in EU region and their statsbeat config is not in EU region, customer is
        // responsible for breaking the EU data boundary violation.
        // Statsbeat config setting has the highest precedence.
        if (instrumentationKey == null || instrumentationKey.isEmpty()) {
            InstrumentationKeyEndpointPair pair =
                StatsbeatConnectionString.getInstrumentationKeyAndEndpointPair(
                    connectionString.getIngestionEndpoint().toString());
            instrumentationKey = pair.instrumentationKey;
            ingestionEndpoint = pair.endpoint;
        }

        URL endpointUrl;
        if (!ingestionEndpoint.endsWith("/")) {
            ingestionEndpoint += "/";
        }
        try {
            endpointUrl = new URL(ingestionEndpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("could not construct statsbeat endpoint uri", e);
        }

        return new StatsbeatConnectionString(endpointUrl, instrumentationKey);
    }

    // visible for testing
    static InstrumentationKeyEndpointPair getInstrumentationKeyAndEndpointPair(
        String customerEndpoint) {
        String geo = getGeoWithoutStampSpecific(customerEndpoint);
        if (EU_REGION_GEO_SET.contains(geo.toLowerCase())) {
            return new InstrumentationKeyEndpointPair(
                EU_REGION_STATSBEAT_IKEY, EU_REGION_STATSBEAT_ENDPOINT);
        }

        return new InstrumentationKeyEndpointPair(
            NON_EU_REGION_STATSBEAT_IKEY, NON_EU_REGION_STATSBEAT_ENDPOINT);
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

    public String getIngestionEndpoint() {
        return ingestionEndpoint;
    }

    public String getInstrumentationKey() {
        return instrumentationKey;
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
