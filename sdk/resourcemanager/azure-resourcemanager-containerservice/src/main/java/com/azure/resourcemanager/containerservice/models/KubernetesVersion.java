// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for Kubernetes versions. */
@Fluent()
public final class KubernetesVersion extends ExpandableStringEnum<KubernetesVersion> {
    /** Static value Kubernetes version 1.5.8. */
    public static final KubernetesVersion KUBERNETES_1_5_8 = fromString("1.5.8");

    /** Static value Kubernetes version 1.6.6. */
    public static final KubernetesVersion KUBERNETES_1_6_6 = fromString("1.6.6");

    /** Static value Kubernetes version 1.6.9. */
    public static final KubernetesVersion KUBERNETES_1_6_9 = fromString("1.6.9");

    /** Static value Kubernetes version 1.6.11. */
    public static final KubernetesVersion KUBERNETES_1_6_11 = fromString("1.6.11");

    /** Static value Kubernetes version 1.6.12. */
    public static final KubernetesVersion KUBERNETES_1_6_12 = fromString("1.6.12");

    /** Static value Kubernetes version 1.6.13. */
    public static final KubernetesVersion KUBERNETES_1_6_13 = fromString("1.6.13");

    /** Static value Kubernetes version 1.7.7. */
    public static final KubernetesVersion KUBERNETES_1_7_7 = fromString("1.7.7");

    /** Static value Kubernetes version 1.7.9. */
    public static final KubernetesVersion KUBERNETES_1_7_9 = fromString("1.7.9");

    /** Static value Kubernetes version 1.7.10. */
    public static final KubernetesVersion KUBERNETES_1_7_10 = fromString("1.7.10");

    /** Static value Kubernetes version 1.7.12. */
    public static final KubernetesVersion KUBERNETES_1_7_12 = fromString("1.7.12");

    /** Static value Kubernetes version 1.8.0. */
    public static final KubernetesVersion KUBERNETES_1_8_0 = fromString("1.8.0");

    /** Static value Kubernetes version 1.8.1. */
    public static final KubernetesVersion KUBERNETES_1_8_1 = fromString("1.8.1");

    /** Static value Kubernetes version 1.8.2. */
    public static final KubernetesVersion KUBERNETES_1_8_2 = fromString("1.8.2");

    /** Static value Kubernetes version 1.8.4. */
    public static final KubernetesVersion KUBERNETES_1_8_4 = fromString("1.8.4");

    /** Static value Kubernetes version 1.8.6. */
    public static final KubernetesVersion KUBERNETES_1_8_6 = fromString("1.8.6");

    /** Static value Kubernetes version 1.8.7. */
    public static final KubernetesVersion KUBERNETES_1_8_7 = fromString("1.8.7");

    /** Static value Kubernetes version 1.8.10. */
    public static final KubernetesVersion KUBERNETES_1_8_10 = fromString("1.8.10");

    /** Static value Kubernetes version 1.8.11. */
    public static final KubernetesVersion KUBERNETES_1_8_11 = fromString("1.8.11");

    /** Static value Kubernetes version 1.9.1. */
    public static final KubernetesVersion KUBERNETES_1_9_1 = fromString("1.9.1");

    /** Static value Kubernetes version 1.9.2. */
    public static final KubernetesVersion KUBERNETES_1_9_2 = fromString("1.9.2");

    /** Static value Kubernetes version 1.9.6. */
    public static final KubernetesVersion KUBERNETES_1_9_6 = fromString("1.9.6");

    /** Static value Kubernetes version 1.9.9. */
    public static final KubernetesVersion KUBERNETES_1_9_9 = fromString("1.9.9");

    /**
     * Creates or finds a Kubernetes version from its string representation.
     *
     * @param name a name to look for
     * @return the corresponding Kubernetes version
     */
    @JsonCreator
    public static KubernetesVersion fromString(String name) {
        return fromString(name, KubernetesVersion.class);
    }

    /** @return known Kubernetes version values */
    public static Collection<KubernetesVersion> values() {
        return values(KubernetesVersion.class);
    }
}
