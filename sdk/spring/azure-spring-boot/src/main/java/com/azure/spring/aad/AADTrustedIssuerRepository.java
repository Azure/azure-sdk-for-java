// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A tenant id is used to construct the trusted issuer repository.
 */
public class AADTrustedIssuerRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADTrustedIssuerRepository.class);

    private static final String LOGIN_MICROSOFT_ONLINE_ISSUER = "https://login.microsoftonline.com/";

    private static final String STS_WINDOWS_ISSUER = "https://sts.windows.net/";

    private static final String STS_CHINA_CLOUD_API_ISSUER = "https://sts.chinacloudapi.cn/";

    private static final String PATH_DELIMITER = "/";

    private static final String PATH_DELIMITER_V2 = "/v2.0";

    private final String tenantId;

    /**
     * In the trustedIssuersMap, the key stores the trusted Issuer, and the value stores the corresponding well known
     * base uri.
     */
    private final Map<String, String> trustedIssuersMap = new HashMap<>();

    public AADTrustedIssuerRepository(String tenantId) {
        this.tenantId = tenantId;
        trustedIssuersMap.putAll(buildAADIssuers(PATH_DELIMITER));
        trustedIssuersMap.putAll(buildAADIssuers(PATH_DELIMITER_V2));
    }

    private Map<String, String> buildAADIssuers(String delimiter) {
        return Arrays.asList(LOGIN_MICROSOFT_ONLINE_ISSUER, STS_WINDOWS_ISSUER,
            STS_CHINA_CLOUD_API_ISSUER)
                     .stream()
                     .map(s -> s + tenantId + delimiter)
                     .collect(Collectors.toMap(String::toString, String::toString));
    }

    public void addB2CUserFlowIssuers(String baseUri, Map<String, String> userFlows) {
        Assert.notNull(baseUri, "baseUri cannot be null.");
        Assert.notNull(userFlows, "userFlows cannot be null.");
        userFlows.keySet().forEach(key -> createB2CIssuer(resolveBaseUri(baseUri), userFlows.get(key)));
    }

    private void createB2CIssuer(String resolveBaseUri, String userFlowName) {
        trustedIssuersMap.put(String.format(resolveBaseUri + "/%s/v2.0/", tenantId), String.format(resolveBaseUri +
            "/%s/%s/v2.0/", tenantId, userFlowName).toLowerCase());
    }

    /**
     * Only the V2 version of Access Token is supported when using Azure AD B2C user flows.
     *
     * @param baseUri The base uri is the domain part of the endpoint.
     * @param userFlows The all user flows mapping which is created under b2c tenant.
     */
    public void addB2CUserFlowTfpIssuers(String baseUri, Map<String, String> userFlows) {
        Assert.notNull(baseUri, "baseUri cannot be null.");
        Assert.notNull(userFlows, "userFlows cannot be null.");
        String resolvedBaseUri = resolveBaseUri(baseUri);
        userFlows.keySet().forEach(key -> createB2CUserFlowIssuer(resolvedBaseUri, userFlows.get(key)));
    }

    private void createB2CUserFlowIssuer(String resolveBaseUri, String userFlowName) {
        String userFlowIssuer =
            String.format(resolveBaseUri + "/tfp/%s/%s/v2.0/", tenantId, userFlowName).toLowerCase();
        trustedIssuersMap.put(userFlowIssuer, userFlowIssuer);
    }

    public Set<String> getTrustedIssuers() {
        return Collections.unmodifiableSet(trustedIssuersMap.keySet());
    }

    public String getWellKnownBaseUri(String issuer) {
        return trustedIssuersMap.getOrDefault(issuer, issuer);
    }

    public void addTrustedIssuer(String... customIssuers) {
        for (String customIssuer : customIssuers) {
            trustedIssuersMap.put(customIssuer, customIssuer);
        }
    }

    public void addTrustedIssuer(String customIssuer, String wellKnownBaseUri) {
        trustedIssuersMap.put(customIssuer, wellKnownBaseUri);
    }

    /**
     * Resolve the base uri to get scheme and host.
     *
     * @param baseUri Base uri in the configuration file.
     */
    private String resolveBaseUri(String baseUri) {
        Assert.notNull(baseUri, "baseUri cannot be null");
        try {
            URI uri = new URI(baseUri);
            return uri.getScheme() + "://" + uri.getHost();
        } catch (URISyntaxException e) {
            LOGGER.error("Resolve the base uri exception.");
            throw new RuntimeException("Resolve the base uri:'" + baseUri + "' exception.");
        }
    }
}
