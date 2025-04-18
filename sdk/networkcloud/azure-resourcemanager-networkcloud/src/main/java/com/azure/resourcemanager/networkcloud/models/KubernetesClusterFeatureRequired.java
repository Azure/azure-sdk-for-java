// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.networkcloud.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * The indicator of if the feature is required or optional. Optional features may be deleted by the user, while required
 * features are managed with the kubernetes cluster lifecycle.
 */
public final class KubernetesClusterFeatureRequired extends ExpandableStringEnum<KubernetesClusterFeatureRequired> {
    /**
     * Static value True for KubernetesClusterFeatureRequired.
     */
    public static final KubernetesClusterFeatureRequired TRUE = fromString("True");

    /**
     * Static value False for KubernetesClusterFeatureRequired.
     */
    public static final KubernetesClusterFeatureRequired FALSE = fromString("False");

    /**
     * Creates a new instance of KubernetesClusterFeatureRequired value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public KubernetesClusterFeatureRequired() {
    }

    /**
     * Creates or finds a KubernetesClusterFeatureRequired from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding KubernetesClusterFeatureRequired.
     */
    public static KubernetesClusterFeatureRequired fromString(String name) {
        return fromString(name, KubernetesClusterFeatureRequired.class);
    }

    /**
     * Gets known KubernetesClusterFeatureRequired values.
     * 
     * @return known KubernetesClusterFeatureRequired values.
     */
    public static Collection<KubernetesClusterFeatureRequired> values() {
        return values(KubernetesClusterFeatureRequired.class);
    }
}
