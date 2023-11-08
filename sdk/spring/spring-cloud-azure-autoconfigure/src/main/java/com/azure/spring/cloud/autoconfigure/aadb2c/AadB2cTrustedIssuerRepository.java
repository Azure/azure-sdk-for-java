// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c;

import com.azure.spring.cloud.autoconfigure.aad.AadTrustedIssuerRepository;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AadB2cProperties;
import org.springframework.util.Assert;

import java.util.Map;

import static java.util.Locale.ROOT;

/**
 * Construct a trusted aad b2c issuer repository.
 */
public class AadB2cTrustedIssuerRepository extends AadTrustedIssuerRepository {

    private final String resolvedBaseUri;

    private final Map<String, String> userFlows;

    private final AadB2cProperties aadB2cProperties;

    /**
     * Creates a new instance of {@link AadB2cTrustedIssuerRepository}.
     *
     * @param aadB2cProperties the AAD B2C properties
     */
    public AadB2cTrustedIssuerRepository(AadB2cProperties aadB2cProperties) {
        super(aadB2cProperties.getProfile().getTenantId());
        this.aadB2cProperties = aadB2cProperties;
        this.resolvedBaseUri = resolveBaseUri(this.aadB2cProperties.getBaseUri());
        this.userFlows = this.aadB2cProperties.getUserFlows();
        this.addB2cIssuer();
        this.addB2cUserFlowIssuers();
    }

    private void addB2cIssuer() {
        Assert.notNull(aadB2cProperties, "aadB2cProperties cannot be null.");
        Assert.notNull(resolvedBaseUri, "resolvedBaseUri cannot be null.");
        String issuer = String.format("%s/%s/v2.0/", resolvedBaseUri, tenantId);
        String oidcIssuerLocation = String.format("%s/%s/%s/v2.0/", resolvedBaseUri, tenantId,
            userFlows.get(aadB2cProperties.getLoginFlow()));
        // Adding oidc issuer location is not a consistent mapping with issuer contained in the access token.
        addSpecialOidcIssuerLocationMap(issuer, oidcIssuerLocation);
        addTrustedIssuer(issuer);
    }

    private void addB2cUserFlowIssuers() {
        Assert.notNull(resolvedBaseUri, "resolvedBaseUri cannot be null.");
        Assert.notNull(userFlows, "userFlows cannot be null.");
        userFlows.values()
                 .stream()
                 .map(uf -> String.format("%s/tfp/%s/%s/v2.0/", resolvedBaseUri, tenantId, uf.toLowerCase(ROOT)))
                 .forEach(this::addTrustedIssuer);
    }
}
