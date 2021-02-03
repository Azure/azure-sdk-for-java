// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad;

import com.azure.spring.autoconfigure.b2c.AADB2CProperties.UserFlows;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A tenant id is used to construct the trusted issuer repository.
 */
public class AADTrustedIssuerRepository {

    private static final String LOGIN_MICROSOFT_ONLINE_ISSUER = "https://login.microsoftonline.com/";
    private static final String STS_WINDOWS_ISSUER = "https://sts.windows.net/";
    private static final String STS_CHINA_CLOUD_API_ISSUER = "https://sts.chinacloudapi.cn/";
    private static final String PATH_DELIMITER = "/";
    private static final String PATH_DELIMITER_V2 = "/v2.0";
    private final List<String> trustedIssuers = new ArrayList<>();
    private final String tenantId;

    public AADTrustedIssuerRepository(String tenantId) {
        this.tenantId = tenantId;
        trustedIssuers.addAll(buildAADIssuer(PATH_DELIMITER));
        trustedIssuers.addAll(buildAADIssuer(PATH_DELIMITER_V2));
    }

    private List<String> buildAADIssuer(String delimiter) {
        return Arrays.asList(LOGIN_MICROSOFT_ONLINE_ISSUER, STS_WINDOWS_ISSUER, STS_CHINA_CLOUD_API_ISSUER).stream()
            .map(s -> s + tenantId + delimiter).collect(Collectors.toList());
    }

    public void buildB2CIssuer(String tenantName) {
        Assert.notNull(tenantName, "tenantName cannot be null.");
        trustedIssuers.add(String.format("https://%s.b2clogin.com/%s/v2.0/", tenantName, tenantId));
    }

    /**
     * Only the V2 version of Access Token is supported when using Azure AD B2C user flows.
     */
    public void buildB2CUserFlowsIssuer(String tenantName, UserFlows userFlows) {
        Assert.notNull(tenantName, "tenantName cannot be null.");
        Assert.notNull(userFlows, "userFlows cannot be null.");
        trustedIssuers.add(String
            .format("https://%s.b2clogin.com/tfp/%s/%s/v2.0/", tenantName, tenantId, userFlows.getSignUpOrSignIn()));
        if (!StringUtils.isEmpty(userFlows.getProfileEdit())) {
            trustedIssuers.add(String
                .format("https://%s.b2clogin.com/tfp/%s/%s/v2.0/", tenantName, tenantId, userFlows.getProfileEdit()));
        }
        if (!StringUtils.isEmpty(userFlows.getPasswordReset())) {
            trustedIssuers.add(String
                .format("https://%s.b2clogin.com/tfp/%s/%s/v2.0/", tenantName, tenantId, userFlows.getPasswordReset()));
        }
    }

    public List<String> getTrustedIssuers() {
        return Collections.unmodifiableList(trustedIssuers);
    }

    public boolean addTrustedIssuer(String... issuer) {
        if(ArrayUtils.isEmpty(issuer)){
            return false;
        }
        return trustedIssuers.addAll(Arrays.stream(issuer).collect(Collectors.toSet()));
    }

}
