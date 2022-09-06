// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;


import com.azure.core.management.Region;
import com.azure.core.util.CoreUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * A service tag represents a group of IP address prefixes from a given Azure service.
 * You can use service tags to define network access controls on network security groups, Azure Firewall, and user-defined routes.
 * Use service tags in place of specific IP addresses when you create security rules and routes.
 */
public class ServiceTag {
    private static final Map<String, ServiceTag> VALUES_BY_NAME = new ConcurrentHashMap<>();
    /** Action Group. */
    public static final ServiceTag ACTION_GROUP = new ServiceTag("ActionGroup", true, false, false, false);
    /** Azure Storage.
     * <p>Note: This tag represents the service, but not specific instances of the service.
     * For example, the tag represents the Azure Storage service, but not a specific Azure Storage account.</p>
     */
    public static final ServiceTag STORAGE = new ServiceTag("Storage", false, true, true, true);
    /**
     * The IP address space that's outside the virtual network and reachable by the public internet.
     * The address range includes the <a href="https://www.microsoft.com/en-us/download/details.aspx?id=41653">Azure-owned public IP address space</a>.
     */
    public static final ServiceTag INTERNET = new ServiceTag("Internet", true, true, false, false);
    /**
     * The virtual network address space (all IP address ranges defined for the virtual network),
     * all connected on-premises address spaces, peered virtual networks, virtual networks connected to a virtual network gateway,
     * the virtual IP address of the host, and address prefixes used on user-defined routes.
     * This tag might also contain default routes.
     */
    public static final ServiceTag VIRTUAL_NETWORK = new ServiceTag("VirtualNetwork", true, false, false, false);

    /**
     * Get service tag from name.
     *
     * @param serviceTagName the name of the service tag, can be with or without region
     * @return service tag, null if invalid
     */
    public static ServiceTag fromName(String serviceTagName) {
        if (serviceTagName == null) {
            return null;
        }
        String[] tagParts = serviceTagName.split(Pattern.quote("."));
        if (tagParts.length == 1) { // root Tag
            if (serviceTagName.contains(".")) {
                return null;
            }
            return VALUES_BY_NAME.get(tagParts[0]);
        } else if (tagParts.length == 2) { // Tag.Region
            ServiceTag rootTag = VALUES_BY_NAME.get(tagParts[0]);
            if (rootTag == null || CoreUtils.isNullOrEmpty(tagParts[1])) {
                return null;
            }
            return rootTag.withRegion(Region.fromName(tagParts[1]));
        } else { // unknown
            return null;
        }
    }

    /**
     * Get regional service tag.
     *
     * @param region the region of the regional service tag, will be <strong>omitted</strong> if
     *               <p>It is null.</p>
     *               <p>The root service tag either cannot be regional or is already regional.</p>
     * @return regional service tag
     */
    public ServiceTag withRegion(Region region) {
        if (region == null || !this.regional) {
            return this;
        }
        String tagName = getServiceTagNameWithRegion(this.name, region);
        ServiceTag tag = VALUES_BY_NAME.get(tagName);
        return tag != null ? tag : new ServiceTag(getServiceTagNameWithRegion(tagName, region),
            this.inbound, this.outbound, true, this.firewall);
    }

    /** @return name of the service tag */
    public String name() {
        return name;
    }

    // service tag name
    private final String name;
    // can use in inbound rule
    private final boolean inbound;
    // can use in outbound rule
    private final boolean outbound;
    // can be regional, e.g. Storage.WestUS
    private final boolean regional;
    // can use with Azure Firewall

    private final boolean firewall;

    private ServiceTag(String name, boolean inbound, boolean outbound, boolean regional, boolean firewall) {
        this.name = name;
        this.inbound = inbound;
        this.outbound = outbound;
        this.regional = regional;
        this.firewall = firewall;
        VALUES_BY_NAME.put(name, this);
    }

    private static String getServiceTagNameWithRegion(String serviceTagName, Region region) {
        if (alreadyRegional(serviceTagName)) {
            return serviceTagName;
        }
        return String.format("%s.%s", serviceTagName, region.label().replaceAll(Pattern.quote(" "), ""));
    }

    private static boolean alreadyRegional(String serviceTagName) {
        return serviceTagName.contains(".");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceTag that = (ServiceTag) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
