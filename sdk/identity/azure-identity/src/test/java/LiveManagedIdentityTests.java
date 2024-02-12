// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.TestBase;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javax.security.auth.login.Configuration;

import static org.junit.jupiter.api.Assertions.fail;

public class LiveManagedIdentityTests extends TestBase {

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
    public void testManagedIdentityFuncDeployment() {
        HttpClient client = HttpClient.createDefault();
        String functionUrl = "https://" + System.getenv("IDENTITY_FUNCTION_NAME") + ".azurewebsites.net/api/mitest";
        HttpRequest request = new HttpRequest(HttpMethod.GET, functionUrl);
        try (HttpResponse httpResponse = client.send(request).block()) {
            if (httpResponse.getStatusCode() != 200) {
                fail("Failed to get response from function app");
            }
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
    public void testManagedIdentityWebAppDeployment() {
        HttpClient client = HttpClient.createDefault();
        String functionUrl = "https://" + System.getenv("IDENTITY_WEBAPP_NAME") + ".azurewebsites.net/mitest";
        ClientLogger logger = new ClientLogger(LiveManagedIdentityTests.class);
        logger.log(LogLevel.INFORMATIONAL, () -> "webappURL: " + functionUrl);
        HttpRequest request = new HttpRequest(HttpMethod.GET, functionUrl);
        try (HttpResponse httpResponse = client.send(request).block()) {
            if (httpResponse.getStatusCode() != 200) {
                fail("Failed to get response from web app");
            }
        }
    }

}
