// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.resourcemanager.appservice.models.CustomHostnameDnsRecordType;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class HostnameSslTests extends AppServiceTest {
    private String webappName = "";
    private String appServicePlanName = "";
    private String domainName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        super.initializeClients(httpPipeline, profile);

        webappName = generateRandomResourceName("java-webapp-", 20);
        appServicePlanName = generateRandomResourceName("java-asp-", 20);

        domainName = super.domain.name();
    }

    @Test
    @Disabled("Need a domain and a certificate")
    public void canBindHostnameAndSsl() throws Exception {
        // hostname binding
        appServiceManager
            .webApps()
            .define(webappName)
            .withRegion(Region.US_WEST)
            .withNewResourceGroup(rgName)
            .withNewWindowsPlan(PricingTier.BASIC_B1)
            .defineHostnameBinding()
            .withAzureManagedDomain(domain)
            .withSubDomain(webappName)
            .withDnsRecordType(CustomHostnameDnsRecordType.CNAME)
            .attach()
            .create();

        WebApp webApp = appServiceManager.webApps().getByResourceGroup(rgName, webappName);
        Assertions.assertNotNull(webApp);
        if (!isPlaybackMode()) {
            Response<String> response = curl("http://" + webappName + "." + domainName);
            Assertions.assertEquals(200, response.getStatusCode());
            Assertions.assertNotNull(response.getValue());
        }
        // hostname binding shortcut
        webApp.update().withManagedHostnameBindings(domain, webappName + "-1", webappName + "-2").apply();
        if (!isPlaybackMode()) {
            Response<String> response = curl("http://" + webappName + "-1." + domainName);
            Assertions.assertEquals(200, response.getStatusCode());
            Assertions.assertNotNull(response.getValue());
            response = curl("http://" + webappName + "-2." + domainName);
            Assertions.assertEquals(200, response.getStatusCode());
            Assertions.assertNotNull(response.getValue());
        }
        // SSL binding
        webApp
            .update()
            .defineSslBinding()
            .forHostname(webappName + "." + domainName)
            .withExistingAppServiceCertificateOrder(certificateOrder)
            .withSniBasedSsl()
            .attach()
            .apply();
        if (!isPlaybackMode()) {
            Response<String> response = null;
            int retryCount = 3;
            while (response == null && retryCount > 0) {
                // TODO (weidxu) this probably not work after switch from okhttp to azure-core
                try {
                    response = curl("https://" + webappName + "." + domainName);
                } catch (SSLPeerUnverifiedException e) {
                    retryCount--;
                    SdkContext.sleep(5000);
                }
            }
            if (retryCount == 0) {
                Assertions.fail();
            }
            Assertions.assertEquals(200, response.getStatusCode());
            Assertions.assertNotNull(response.getValue());
        }
    }
}
