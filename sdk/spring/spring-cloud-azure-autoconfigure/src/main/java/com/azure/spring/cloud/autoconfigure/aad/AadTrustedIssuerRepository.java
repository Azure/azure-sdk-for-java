// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad;

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

/**
 * A tenant id is used to construct the trusted issuer repository.
 */
public class AadTrustedIssuerRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadTrustedIssuerRepository.class);

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

    /**
     * The tenant ID
     */
    protected String tenantId;

    /**
     * Creates a new instance of {@link AadTrustedIssuerRepository}.
     *
     * @param tenantId the tenant ID
     */
    public AadTrustedIssuerRepository(String tenantId) {
        this.tenantId = tenantId;
        trustedIssuers.addAll(buildAadIssuers(PATH_DELIMITER));
        trustedIssuers.addAll(buildAadIssuers(PATH_DELIMITER_V2));
    }

    private List<String> buildAadIssuers(String delimiter) {
        return Stream.of(LOGIN_MICROSOFT_ONLINE_ISSUER, STS_WINDOWS_ISSUER, STS_CHINA_CLOUD_API_ISSUER)
                     .map(s -> s + tenantId + delimiter)
                     .collect(Collectors.toList());
    }

    /**
     * Gets the set of trusted issuers.
     *
     * @return the set of trusted issuers
     */
    public Set<String> getTrustedIssuers() {
        return Collections.unmodifiableSet(trustedIssuers);
    }

    /**
     * Adds trusted issuers.
     *
     * @param issuers the issuers
     * @return whether the issuers were added
     */
    public boolean addTrustedIssuer(String... issuers) {
        return trustedIssuers.addAll(Arrays.asList(issuers));
    }

    /**
     * Adds a trusted issuer.
     *
     * @param issuer the issuer
     * @param oidcIssuerLocation the OIDC issuer location
     */
    public void addSpecialOidcIssuerLocationMap(String issuer, String oidcIssuerLocation) {
        specialOidcIssuerLocationMap.put(issuer, oidcIssuerLocation);
    }

    /**
     * Whether the issuer is trusted.
     *
     * @param issuer the issuer
     * @return whether the issuer is trusted
     */
    public boolean isTrusted(String issuer) {
        return this.trustedIssuers.contains(issuer);
    }

    /**
     * Whether the issuer has a special OIDC issuer location.
     *
     * @param issuer the issuer
     * @return whether the issuer has a special OIDC issuer location
     */
    public boolean hasSpecialOidcIssuerLocation(String issuer) {
        return this.specialOidcIssuerLocationMap.containsKey(issuer);
    }

    /**
     * Gets the issuer's special OIDC issuer location.
     *
     * @param issuer the issuer
     * @return the issuer's special OIDC issuer location
     */
    public String getSpecialOidcIssuerLocation(String issuer) {
        return this.specialOidcIssuerLocationMap.get(issuer);
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
