// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

/**
 * Known Web Application Gateway managed rule set.
 */
public enum KnownWebApplicationGatewayManagedRuleSet {
    /**
     * Managed Rule Set based off OWASP CRS 3.2 version.
     *
     * @see <a href="https://learn.microsoft.com/en-us/azure/web-application-firewall/ag/application-gateway-crs-rulegroups-rules?tabs=owasp32#owasp-crs-32">
     *     OWASP CRS 3.2
     *     </a>
     */
    OWASP_3_2("OWASP", "3.2"),

    /**
     * Managed Rule Set based off OWASP CRS 3.1 version.
     *
     * @see <a href="https://learn.microsoft.com/en-us/azure/web-application-firewall/ag/application-gateway-crs-rulegroups-rules?tabs=owasp32#owasp-crs-31">
     *     OWASP CRS 3.1
     *     </a>
     */
    OWASP_3_1("OWASP", "3.1"),

    /**
     * Managed Rule Set based off OWASP CRS 3.0 version.
     *
     * @see <a href="https://learn.microsoft.com/en-us/azure/web-application-firewall/ag/application-gateway-crs-rulegroups-rules?tabs=owasp32#owasp-crs-30">
     *     OWASP CRS 3.0
     *     </a>
     */
    OWASP_3_0("OWASP", "3.0"),

    /**
     * The Azure-managed Default Rule Set (DRS) 2.1 version.
     *
     * @see <a href="https://learn.microsoft.com/en-us/azure/web-application-firewall/afds/waf-front-door-drs?tabs=drs20#drs-21">
     *     DRS 2.1
     *     </a>
     */
    MICROSOFT_DEFAULT_RULESET_2_1("Microsoft_DefaultRuleSet", "2.1");

    private final String type;
    private final String version;

    KnownWebApplicationGatewayManagedRuleSet(String type, String version) {
        this.type = type;
        this.version = version;
    }

    /**
     * Gets the type of the Managed Rule Set.
     *
     * @return the type of the Managed Rule Set
     */
    public String type() {
        return type;
    }

    /**
     * Geets the version of the Managed Rule Set.
     *
     * @return the version of the Managed Rule Set
     */
    public String version() {
        return version;
    }
}
