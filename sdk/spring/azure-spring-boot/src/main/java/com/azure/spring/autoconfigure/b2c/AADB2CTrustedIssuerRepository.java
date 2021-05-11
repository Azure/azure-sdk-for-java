// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import com.azure.spring.aad.AADTrustedIssuerRepository;
import org.springframework.util.Assert;

import java.util.Map;

import static java.util.Locale.ROOT;

/**
 * Construct a trusted aad b2c issuer repository.
 */
public class AADB2CTrustedIssuerRepository extends AADTrustedIssuerRepository {

    private final String resolvedBaseUri;

    private final Map<String, String> userFlows;

    private final AADB2CProperties aadb2CProperties;

    public AADB2CTrustedIssuerRepository(AADB2CProperties aadb2CProperties) {
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
        String b2cIss = String.format("%s/%s/v2.0/", resolvedBaseUri, tenantId);
        String oidcIssuerLocation = String.format("%s/%s/%s/v2.0/", resolvedBaseUri, tenantId,
            userFlows.get(aadb2CProperties.getLoginFlow()));
        // Adding oidc issuer location is not a consistent mapping with issuer contained in the access token.
        addTrustedIssuer(b2cIss, oidcIssuerLocation);
    }

    private void addB2CUserFlowIssuers() {
        Assert.notNull(resolvedBaseUri, "resolvedBaseUri cannot be null.");
        Assert.notNull(userFlows, "userFlows cannot be null.");
        userFlows.keySet()
                 .stream()
                 .map(uf -> String.format("%s/tfp/%s/%s/v2.0/", resolvedBaseUri, tenantId, uf.toLowerCase(ROOT)))
                 .forEach(this::addTrustedIssuer);
    }
}
