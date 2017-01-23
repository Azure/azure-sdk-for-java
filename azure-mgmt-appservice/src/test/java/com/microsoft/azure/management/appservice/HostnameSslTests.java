/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.RestClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.Test;

import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

public class HostnameSslTests extends AppServiceTest {
    private static String WEBAPP_NAME = "";
    private static String APP_SERVICE_PLAN_NAME = "";
    private static OkHttpClient httpClient = new OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).build();
    private String DOMAIN = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        super.initializeClients(restClient, defaultSubscription, domain);

        WEBAPP_NAME = generateRandomResourceName("java-webapp-", 20);
        APP_SERVICE_PLAN_NAME = generateRandomResourceName("java-asp-", 20);

        DOMAIN = super.domain.name();
    }

    @Test
    public void canBindHostnameAndSsl() throws Exception {
        // hostname binding
        appServiceManager.webApps().define(WEBAPP_NAME)
                .withNewResourceGroup(RG_NAME)
                .withNewAppServicePlan(APP_SERVICE_PLAN_NAME)
                .withRegion(Region.US_WEST)
                .withPricingTier(AppServicePricingTier.BASIC_B1)
                .defineHostnameBinding()
                    .withAzureManagedDomain(domain)
                    .withSubDomain(WEBAPP_NAME)
                    .withDnsRecordType(CustomHostNameDnsRecordType.CNAME)
                    .attach()
                .create();

        WebApp webApp = appServiceManager.webApps().getByGroup(RG_NAME, WEBAPP_NAME);
        Assert.assertNotNull(webApp);
        if (!IS_MOCKED) {
            Response response = curl("http://" + WEBAPP_NAME + "." + DOMAIN);
            Assert.assertEquals(200, response.code());
            Assert.assertNotNull(response.body().string());
        }
        // hostname binding shortcut
        webApp.update()
                .withManagedHostnameBindings(domain, WEBAPP_NAME + "-1", WEBAPP_NAME + "-2")
                .apply();
        if (!IS_MOCKED) {
            Response response = curl("http://" + WEBAPP_NAME + "-1." + DOMAIN);
            Assert.assertEquals(200, response.code());
            Assert.assertNotNull(response.body().string());
            response = curl("http://" + WEBAPP_NAME + "-2." + DOMAIN);
            Assert.assertEquals(200, response.code());
            Assert.assertNotNull(response.body().string());
        }
        // SSL binding
        webApp.update()
                .defineSslBinding()
                    .forHostname(WEBAPP_NAME + "." + DOMAIN)
                    .withExistingAppServiceCertificateOrder(certificateOrder)
                    .withSniBasedSsl()
                    .attach()
                .apply();
        if (!IS_MOCKED) {
            Response response = null;
            int retryCount = 3;
            while (response == null && retryCount > 0) {
                try {
                    response = curl("https://" + WEBAPP_NAME + "." + DOMAIN);
                } catch (SSLPeerUnverifiedException e) {
                    retryCount--;
                    Thread.sleep(5000);
                }
            }
            if (retryCount == 0) {
                fail();
            }
            Assert.assertEquals(200, response.code());
            Assert.assertNotNull(response.body().string());
        }
    }

    private static Response curl(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        return httpClient.newCall(request).execute();
    }

}