// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.ServiceLimits;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ServiceLimits} and {@link ServiceLimits}.
 */
public final class ServiceLimitsConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.ServiceLimits} to {@link ServiceLimits}.
     */
    public static ServiceLimits map(com.azure.search.documents.indexes.implementation.models.ServiceLimits obj) {
        if (obj == null) {
            return null;
        }
        ServiceLimits serviceLimits = new ServiceLimits();

        Integer maxFieldNestingDepthPerIndex = obj.getMaxFieldNestingDepthPerIndex();
        serviceLimits.setMaxFieldNestingDepthPerIndex(maxFieldNestingDepthPerIndex);

        Integer maxFieldsPerIndex = obj.getMaxFieldsPerIndex();
        serviceLimits.setMaxFieldsPerIndex(maxFieldsPerIndex);

        Integer maxComplexObjectsInCollectionsPerDocument = obj.getMaxComplexObjectsInCollectionsPerDocument();
        serviceLimits.setMaxComplexObjectsInCollectionsPerDocument(maxComplexObjectsInCollectionsPerDocument);

        Integer maxComplexCollectionFieldsPerIndex = obj.getMaxComplexCollectionFieldsPerIndex();
        serviceLimits.setMaxComplexCollectionFieldsPerIndex(maxComplexCollectionFieldsPerIndex);
        return serviceLimits;
    }

    /**
     * Maps from {@link ServiceLimits} to {@link com.azure.search.documents.indexes.implementation.models.ServiceLimits}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ServiceLimits map(ServiceLimits obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.ServiceLimits serviceLimits =
            new com.azure.search.documents.indexes.implementation.models.ServiceLimits();

        Integer maxFieldNestingDepthPerIndex = obj.getMaxFieldNestingDepthPerIndex();
        serviceLimits.setMaxFieldNestingDepthPerIndex(maxFieldNestingDepthPerIndex);

        Integer maxFieldsPerIndex = obj.getMaxFieldsPerIndex();
        serviceLimits.setMaxFieldsPerIndex(maxFieldsPerIndex);

        Integer maxComplexObjectsInCollectionsPerDocument = obj.getMaxComplexObjectsInCollectionsPerDocument();
        serviceLimits.setMaxComplexObjectsInCollectionsPerDocument(maxComplexObjectsInCollectionsPerDocument);

        Integer maxComplexCollectionFieldsPerIndex = obj.getMaxComplexCollectionFieldsPerIndex();
        serviceLimits.setMaxComplexCollectionFieldsPerIndex(maxComplexCollectionFieldsPerIndex);
        return serviceLimits;
    }

    private ServiceLimitsConverter() {
    }
}
