// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.implementation.model.AccessToken;
import com.azure.security.keyvault.jca.implementation.utils.AccessTokenUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.URLEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The JUnit test for the AuthClient.
 */
@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class AccessTokenUtilTest {

    /**
     * Test getAuthorizationToken method.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testGetAuthorizationToken() throws Exception {
        String tenantId = PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_TENANT_ID");
        String clientId = PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_CLIENT_ID");
        String clientSecret = PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_CLIENT_SECRET");
        String keyVaultEndPointSuffix = PropertyConvertorUtils.getPropertyValue("KEY_VAULT_ENDPOINT_SUFFIX", ".vault.azure.net");
        CloudType cloudType = getCloudTypeByKeyVaultEndPoint(keyVaultEndPointSuffix);
        String resourceUrl = getResourceUrl(cloudType);
        String aadAuthenticationUrl = getAadAuthenticationUrl(cloudType);
        AccessToken result = AccessTokenUtil.getAccessToken(
            resourceUrl,
            aadAuthenticationUrl,
            tenantId,
            clientId,
            URLEncoder.encode(clientSecret, "UTF-8")
        );
        assertNotNull(result);
    }

    private String getResourceUrl(CloudType cloudType) {
        if (CloudType.UsGov.equals(cloudType)) {
            return "https://management.usgovcloudapi.net/";
        } else if (CloudType.China.equals(cloudType)) {
            return "https://management.chinacloudapi.cn/";
        }
        return "https://management.azure.com/";
    }

    private String getAadAuthenticationUrl(CloudType cloudType) {
        if (CloudType.UsGov.equals(cloudType)) {
            return "https://login.microsoftonline.us/";
        } else if (CloudType.China.equals(cloudType)) {
            return "https://login.partner.microsoftonline.cn/";
        }
        return "https://login.microsoftonline.com/";
    }

    private CloudType getCloudTypeByKeyVaultEndPoint(String keyVaultEndPointSuffix) {
        if (".vault.usgovcloudapi.net".equals(keyVaultEndPointSuffix)) {
            return CloudType.UsGov;
        } else if (".vault.azure.cn".equals(keyVaultEndPointSuffix)) {
            return CloudType.China;
        }
        return CloudType.Public;
    }

    private enum CloudType {
        Public,
        UsGov,
        China,
        UNKNOWN
    }
}
