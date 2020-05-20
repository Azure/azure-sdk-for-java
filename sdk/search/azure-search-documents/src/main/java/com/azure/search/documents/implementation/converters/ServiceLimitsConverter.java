// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.ServiceLimits;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.ServiceLimits} and {@link ServiceLimits}.
 */
public final class ServiceLimitsConverter {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceLimitsConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.ServiceLimits} to {@link ServiceLimits}.
     */
    public static ServiceLimits map(com.azure.search.documents.implementation.models.ServiceLimits obj) {
        if (obj == null) {
            return null;
        }
        ServiceLimits serviceLimits = new ServiceLimits();

        Integer _maxFieldNestingDepthPerIndex = obj.getMaxFieldNestingDepthPerIndex();
        serviceLimits.setMaxFieldNestingDepthPerIndex(_maxFieldNestingDepthPerIndex);

        Integer _maxFieldsPerIndex = obj.getMaxFieldsPerIndex();
        serviceLimits.setMaxFieldsPerIndex(_maxFieldsPerIndex);

        Integer _maxComplexObjectsInCollectionsPerDocument = obj.getMaxComplexObjectsInCollectionsPerDocument();
        serviceLimits.setMaxComplexObjectsInCollectionsPerDocument(_maxComplexObjectsInCollectionsPerDocument);

        Integer _maxComplexCollectionFieldsPerIndex = obj.getMaxComplexCollectionFieldsPerIndex();
        serviceLimits.setMaxComplexCollectionFieldsPerIndex(_maxComplexCollectionFieldsPerIndex);
        return serviceLimits;
    }

    /**
     * Maps from {@link ServiceLimits} to {@link com.azure.search.documents.implementation.models.ServiceLimits}.
     */
    public static com.azure.search.documents.implementation.models.ServiceLimits map(ServiceLimits obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.ServiceLimits serviceLimits =
            new com.azure.search.documents.implementation.models.ServiceLimits();

        Integer _maxFieldNestingDepthPerIndex = obj.getMaxFieldNestingDepthPerIndex();
        serviceLimits.setMaxFieldNestingDepthPerIndex(_maxFieldNestingDepthPerIndex);

        Integer _maxFieldsPerIndex = obj.getMaxFieldsPerIndex();
        serviceLimits.setMaxFieldsPerIndex(_maxFieldsPerIndex);

        Integer _maxComplexObjectsInCollectionsPerDocument = obj.getMaxComplexObjectsInCollectionsPerDocument();
        serviceLimits.setMaxComplexObjectsInCollectionsPerDocument(_maxComplexObjectsInCollectionsPerDocument);

        Integer _maxComplexCollectionFieldsPerIndex = obj.getMaxComplexCollectionFieldsPerIndex();
        serviceLimits.setMaxComplexCollectionFieldsPerIndex(_maxComplexCollectionFieldsPerIndex);
        return serviceLimits;
    }
}
