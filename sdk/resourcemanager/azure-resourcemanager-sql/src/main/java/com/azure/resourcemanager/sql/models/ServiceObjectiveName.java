// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/** Defines values for ServiceObjectiveName. */
public final class ServiceObjectiveName extends ExpandableStringEnum<ServiceObjectiveName> {
    /** Static value System for ServiceObjectiveName. */
    public static final ServiceObjectiveName SYSTEM = fromString("System");

    /** Static value System0 for ServiceObjectiveName. */
    public static final ServiceObjectiveName SYSTEM0 = fromString("System0");

    /** Static value System1 for ServiceObjectiveName. */
    public static final ServiceObjectiveName SYSTEM1 = fromString("System1");

    /** Static value System2 for ServiceObjectiveName. */
    public static final ServiceObjectiveName SYSTEM2 = fromString("System2");

    /** Static value System3 for ServiceObjectiveName. */
    public static final ServiceObjectiveName SYSTEM3 = fromString("System3");

    /** Static value System4 for ServiceObjectiveName. */
    public static final ServiceObjectiveName SYSTEM4 = fromString("System4");

    /** Static value System2L for ServiceObjectiveName. */
    public static final ServiceObjectiveName SYSTEM2L = fromString("System2L");

    /** Static value System3L for ServiceObjectiveName. */
    public static final ServiceObjectiveName SYSTEM3L = fromString("System3L");

    /** Static value System4L for ServiceObjectiveName. */
    public static final ServiceObjectiveName SYSTEM4L = fromString("System4L");

    /** Static value Free for ServiceObjectiveName. */
    public static final ServiceObjectiveName FREE = fromString("Free");

    /** Static value Basic for ServiceObjectiveName. */
    public static final ServiceObjectiveName BASIC = fromString("Basic");

    /** Static value S0 for ServiceObjectiveName. */
    public static final ServiceObjectiveName S0 = fromString("S0");

    /** Static value S1 for ServiceObjectiveName. */
    public static final ServiceObjectiveName S1 = fromString("S1");

    /** Static value S2 for ServiceObjectiveName. */
    public static final ServiceObjectiveName S2 = fromString("S2");

    /** Static value S3 for ServiceObjectiveName. */
    public static final ServiceObjectiveName S3 = fromString("S3");

    /** Static value S4 for ServiceObjectiveName. */
    public static final ServiceObjectiveName S4 = fromString("S4");

    /** Static value S6 for ServiceObjectiveName. */
    public static final ServiceObjectiveName S6 = fromString("S6");

    /** Static value S7 for ServiceObjectiveName. */
    public static final ServiceObjectiveName S7 = fromString("S7");

    /** Static value S9 for ServiceObjectiveName. */
    public static final ServiceObjectiveName S9 = fromString("S9");

    /** Static value S12 for ServiceObjectiveName. */
    public static final ServiceObjectiveName S12 = fromString("S12");

    /** Static value P1 for ServiceObjectiveName. */
    public static final ServiceObjectiveName P1 = fromString("P1");

    /** Static value P2 for ServiceObjectiveName. */
    public static final ServiceObjectiveName P2 = fromString("P2");

    /** Static value P3 for ServiceObjectiveName. */
    public static final ServiceObjectiveName P3 = fromString("P3");

    /** Static value P4 for ServiceObjectiveName. */
    public static final ServiceObjectiveName P4 = fromString("P4");

    /** Static value P6 for ServiceObjectiveName. */
    public static final ServiceObjectiveName P6 = fromString("P6");

    /** Static value P11 for ServiceObjectiveName. */
    public static final ServiceObjectiveName P11 = fromString("P11");

    /** Static value P15 for ServiceObjectiveName. */
    public static final ServiceObjectiveName P15 = fromString("P15");

    /** Static value PRS1 for ServiceObjectiveName. */
    public static final ServiceObjectiveName PRS1 = fromString("PRS1");

    /** Static value PRS2 for ServiceObjectiveName. */
    public static final ServiceObjectiveName PRS2 = fromString("PRS2");

    /** Static value PRS4 for ServiceObjectiveName. */
    public static final ServiceObjectiveName PRS4 = fromString("PRS4");

    /** Static value PRS6 for ServiceObjectiveName. */
    public static final ServiceObjectiveName PRS6 = fromString("PRS6");

    /** Static value DW100 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW100 = fromString("DW100");

    /** Static value DW200 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW200 = fromString("DW200");

    /** Static value DW300 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW300 = fromString("DW300");

    /** Static value DW400 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW400 = fromString("DW400");

    /** Static value DW500 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW500 = fromString("DW500");

    /** Static value DW600 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW600 = fromString("DW600");

    /** Static value DW1000 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW1000 = fromString("DW1000");

    /** Static value DW1200 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW1200 = fromString("DW1200");

    /** Static value DW1000c for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW1000C = fromString("DW1000c");

    /** Static value DW1500 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW1500 = fromString("DW1500");

    /** Static value DW1500c for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW1500C = fromString("DW1500c");

    /** Static value DW2000 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW2000 = fromString("DW2000");

    /** Static value DW2000c for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW2000C = fromString("DW2000c");

    /** Static value DW3000 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW3000 = fromString("DW3000");

    /** Static value DW2500c for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW2500C = fromString("DW2500c");

    /** Static value DW3000c for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW3000C = fromString("DW3000c");

    /** Static value DW6000 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW6000 = fromString("DW6000");

    /** Static value DW5000c for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW5000C = fromString("DW5000c");

    /** Static value DW6000c for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW6000C = fromString("DW6000c");

    /** Static value DW7500c for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW7500C = fromString("DW7500c");

    /** Static value DW10000c for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW10000C = fromString("DW10000c");

    /** Static value DW15000c for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW15000C = fromString("DW15000c");

    /** Static value DW30000c for ServiceObjectiveName. */
    public static final ServiceObjectiveName DW30000C = fromString("DW30000c");

    /** Static value DS100 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DS100 = fromString("DS100");

    /** Static value DS200 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DS200 = fromString("DS200");

    /** Static value DS300 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DS300 = fromString("DS300");

    /** Static value DS400 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DS400 = fromString("DS400");

    /** Static value DS500 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DS500 = fromString("DS500");

    /** Static value DS600 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DS600 = fromString("DS600");

    /** Static value DS1000 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DS1000 = fromString("DS1000");

    /** Static value DS1200 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DS1200 = fromString("DS1200");

    /** Static value DS1500 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DS1500 = fromString("DS1500");

    /** Static value DS2000 for ServiceObjectiveName. */
    public static final ServiceObjectiveName DS2000 = fromString("DS2000");

    /** Static value ElasticPool for ServiceObjectiveName. */
    public static final ServiceObjectiveName ELASTIC_POOL = fromString("ElasticPool");

    /**
     * Creates a new instance of ServiceObjectiveName value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public ServiceObjectiveName() {
    }

    /**
     * Creates or finds a ServiceObjectiveName from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ServiceObjectiveName.
     */
    public static ServiceObjectiveName fromString(String name) {
        return fromString(name, ServiceObjectiveName.class);
    }

    /**
     * Gets known ServiceObjectiveName values.
     *
     * @return known ServiceObjectiveName values.
     */
    public static Collection<ServiceObjectiveName> values() {
        return values(ServiceObjectiveName.class);
    }
}
