// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DerivedMetricInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterConjunctionGroupInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Arrays.asList;

public class QuickPulseFilteringConfiguration {
    private Set<String> seenMetricIds;

    // key is the telemetry type
    private ConcurrentMap<String, DerivedMetricInfo[]> validDerivedMetricInfos;

    // first key is the telemetry type, second key is the id associated with the document filters
    private ConcurrentMap<String, Map<String, FilterConjunctionGroupInfo[]>> validDocumentFilterConjunctionGroupInfos;

    private volatile String etag;

    public QuickPulseFilteringConfiguration() {
        validDerivedMetricInfos = new ConcurrentHashMap<String, DerivedMetricInfo[]>();
        validDocumentFilterConjunctionGroupInfos = new ConcurrentHashMap<String, Map<String, FilterConjunctionGroupInfo[]>>();
        seenMetricIds = new HashSet<String>();
        etag = "";
    }

    public void updateConfiguration(CollectionConfigurationInfo configuration) {
        this.etag = configuration.getETag();
        parseDocumentFilterConfiguration(configuration);
        parseMetricFilterConfiguration(configuration);
    }

    private void parseDocumentFilterConfiguration(CollectionConfigurationInfo configuration) {

    }

    private void parseMetricFilterConfiguration(CollectionConfigurationInfo configuration) {

    }

    public static class Validator {
        private static final Set<String> knownStringColumns = new HashSet<String>(asList(
            KnownRequestColumns
        ));
    }


}
