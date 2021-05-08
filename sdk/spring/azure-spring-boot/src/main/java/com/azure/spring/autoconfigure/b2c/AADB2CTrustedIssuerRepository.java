// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import com.azure.spring.aad.webapi.AADTrustedIssuerRepository;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class AADB2CTrustedIssuerRepository extends AADTrustedIssuerRepository {

    private final String resolvedBaseUri;

    private final Map<String, String> userFlows;

    private final AADB2CProperties aadb2CProperties;

    /**
     * Place a mapping that cannot access metadata through issuer splicing /.well-known/openid-configuration.
     */
    private final Map<String, String> specialWellKnownIssMap = new HashMap<>();

    AADB2CTrustedIssuerRepository(AADB2CProperties aadb2CProperties) {
        super(aadb2CProperties.getTenantId());
        this.aadb2CProperties = aadb2CProperties;
        this.resolvedBaseUri = resolveBaseUri(aadb2CProperties.getBaseUri());
        this.userFlows = aadb2CProperties.getUserFlows();
        this.addB2CIssuer();
        this.addB2CUserFlowIssuers();
    }

    private void addB2CIssuer() {
        Assert.notNull(aadb2CProperties, "aadb2CProperties cannot be null.");
        Assert.notNull(resolvedBaseUri, "resolvedBaseUri cannot be null.");
        String b2cIss = String.format(resolveBaseUri(resolvedBaseUri) + "/%s/v2.0/", tenantId);
        trustedIssuers.add(b2cIss);
        specialWellKnownIssMap.put(b2cIss, String.format(resolveBaseUri(resolvedBaseUri) + "/%s/%s/v2.0/", tenantId,
            userFlows.get(aadb2CProperties.getLoginFlow())));
    }

    private void addB2CUserFlowIssuers() {
        Assert.notNull(resolvedBaseUri, "resolvedBaseUri cannot be null.");
        Assert.notNull(userFlows, "userFlows cannot be null.");
        String resolvedBaseUri = resolveBaseUri(this.resolvedBaseUri);
        userFlows.keySet().forEach(key -> createB2CUserFlowIssuer(resolvedBaseUri, userFlows.get(key)));
    }

    private void createB2CUserFlowIssuer(String resolveBaseUri, String userFlowName) {
        trustedIssuers.add(String.format(resolveBaseUri + "/tfp/%s/%s/v2.0/", tenantId,
            userFlowName.toLowerCase(Locale.ROOT)));
    }

    public Map<String, String> getSpecialWellKnownIssMap() {
        return specialWellKnownIssMap;
    }
}
