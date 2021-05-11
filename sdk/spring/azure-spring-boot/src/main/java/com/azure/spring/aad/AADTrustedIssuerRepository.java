// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad;

import com.azure.spring.autoconfigure.b2c.AADB2CTrustedIssuerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Locale.ROOT;

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

    private final Set<String> trustedIssuers = new HashSet<>();

    /**
     * Place a mapping that cannot access metadata through issuer splicing /.well-known/openid-configuration.
     */
    private final Map<String, String> specialOidcIssuerLocationMap = new HashMap<>();

    protected String tenantId;

    public AADTrustedIssuerRepository(String tenantId) {
        this.tenantId = tenantId;
        trustedIssuers.addAll(buildAADIssuers(PATH_DELIMITER));
        trustedIssuers.addAll(buildAADIssuers(PATH_DELIMITER_V2));
    }

    private List<String> buildAADIssuers(String delimiter) {
        return Stream.of(LOGIN_MICROSOFT_ONLINE_ISSUER, STS_WINDOWS_ISSUER, STS_CHINA_CLOUD_API_ISSUER)
                     .map(s -> s + tenantId + delimiter)
                     .collect(Collectors.toList());
    }

    public Set<String> getTrustedIssuers() {
        return Collections.unmodifiableSet(trustedIssuers);
    }

    public boolean addTrustedIssuer(String... issuers) {
        return trustedIssuers.addAll(Arrays.asList(issuers));
    }

    public boolean addTrustedIssuer(String issuer, String oidcIssuerLocation) {
        specialOidcIssuerLocationMap.put(issuer, oidcIssuerLocation);
        return trustedIssuers.add(issuer);
    }

    public boolean isTrusted(String issuer) {
        return this.trustedIssuers.contains(issuer);
    }

    public boolean hasSpecialOidcIssuerLocation(String issuer) {
        return this.specialOidcIssuerLocationMap.containsKey(issuer);
    }

    public String getSpecialOidcIssuerLocation(String issuer) {
        return this.specialOidcIssuerLocationMap.get(issuer);
    }

    @Deprecated
    public void addB2CIssuer(String baseUri) {
        Assert.notNull(baseUri, "baseUri cannot be null.");
        String resolvedBaseUri = resolveBaseUri(baseUri);
        trustedIssuers.add(String.format("%s/%s/v2.0/", resolvedBaseUri, tenantId));
    }

    /**
     * Only the V2 version of Access Token is supported when using Azure AD B2C user flows.
     *
     * @param baseUri The base uri is the domain part of the endpoint.
     * @param userFlows The all user flows mapping which is created under b2c tenant.
     * @deprecated Is not recommended in {@link AADTrustedIssuerRepository} to add AAD B2C related content. See {@link
     * AADB2CTrustedIssuerRepository}.
     */
    @Deprecated
    public void addB2CUserFlowIssuers(String baseUri, Map<String, String> userFlows) {
        Assert.notNull(userFlows, "userFlows cannot be null.");
        String resolvedBaseUri = resolveBaseUri(baseUri);
        userFlows.keySet().forEach(key -> createB2CUserFlowIssuer(resolvedBaseUri, userFlows.get(key)));
    }

    @Deprecated
    private void createB2CUserFlowIssuer(String resolvedBaseUri, String userFlowName) {
        trustedIssuers.add(String.format("%s/tfp/%s/%s/v2.0/", resolvedBaseUri, tenantId,
            userFlowName.toLowerCase(ROOT)));
    }

    /**
     * Resolve the base uri to get scheme and host.
     *
     * @param baseUri baseUri Base uri in the configuration file.
     * @return the parsed base uri.
     * @throws RuntimeException thrown if the uri is not valid.
     */
    protected String resolveBaseUri(String baseUri) {
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
