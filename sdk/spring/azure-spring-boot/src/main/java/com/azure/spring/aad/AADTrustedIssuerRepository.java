// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad;

import com.azure.spring.autoconfigure.b2c.AADB2CProperties.UserFlows;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

public class AADTrustedIssuerRepository {

    private static final String LOGIN_MICROSOFT_ONLINE_ISSUER = "https://login.microsoftonline.com/";
    private static final String STS_WINDOWS_ISSUER = "https://sts.windows.net/";
    private static final String STS_CHINA_CLOUD_API_ISSUER = "https://sts.chinacloudapi.cn/";

    private final Set<String> trustedIssuers = new HashSet<>();

    public AADTrustedIssuerRepository(String tenantId) {
        this(tenantId, null, null);
    }

    public AADTrustedIssuerRepository(String tenantId, String tenantName, UserFlows userFlows) {

        trustedIssuers.addAll(buildV1Issuer(tenantId));
        trustedIssuers.addAll(buildV2Issuer(tenantId));
        if (userFlows != null && !StringUtils.isEmpty(tenantName)) {
            trustedIssuers.add(buildB2CIssuer(tenantId, tenantName));
            trustedIssuers.addAll(buildB2CPolicyIssuer(tenantId, tenantName, userFlows));
        }
    }

    private Set<String> buildV1Issuer(String tenantId) {
        return Arrays.asList(LOGIN_MICROSOFT_ONLINE_ISSUER, STS_WINDOWS_ISSUER, STS_CHINA_CLOUD_API_ISSUER).stream()
            .map(s -> s + tenantId + "/").collect(Collectors.toSet());
    }

    private Set<String> buildV2Issuer(String tenantId) {
        return Arrays.asList(LOGIN_MICROSOFT_ONLINE_ISSUER, STS_WINDOWS_ISSUER, STS_CHINA_CLOUD_API_ISSUER).stream()
            .map(s -> s + tenantId + "/v2.0").collect(Collectors.toSet());
    }

    private String buildB2CIssuer(String tenantId, String tenantName) {
        return String.format("https://%s.b2clogin.com/%s/v2.0/", tenantName, tenantId);
    }

    private Set<String> buildB2CPolicyIssuer(String tenantId, String tenantName, UserFlows userFlows) {
        Set<String> trustedPolicyIssuers = new HashSet<>();
        trustedPolicyIssuers.add(String
            .format("https://%s.b2clogin.com/tfp/%s/%s/v2.0/", tenantName, tenantId, userFlows.getSignUpOrSignIn()));
        if (!StringUtils.isEmpty(userFlows.getProfileEdit())) {
            trustedPolicyIssuers.add(String
                .format("https://%s.b2clogin.com/tfp/%s/%s/v2.0/", tenantName, tenantId, userFlows.getProfileEdit()));
        }
        if (!StringUtils.isEmpty(userFlows.getPasswordReset())) {
            trustedPolicyIssuers.add(String
                .format("https://%s.b2clogin.com/tfp/%s/%s/v2.0/", tenantName, tenantId, userFlows.getPasswordReset()));
        }
        return trustedPolicyIssuers;
    }

    public Set<String> getTrustedIssuers() {
        return trustedIssuers;
    }
}
