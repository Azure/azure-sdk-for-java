// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.broker.shr.resources;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.identity.InteractiveBrowserCredential;
import com.azure.identity.broker.InteractiveBrowserBrokerCredentialBuilder;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PopTokenTest {

    @Test
    @Disabled("Manual Test requires Interactive flow")
    public void testPopTokenAuth() {

        assertDoesNotThrow(() -> {
            WinDef.HWND hwnd = User32.INSTANCE.GetForegroundWindow();
            long hwndValue = com.sun.jna.Pointer.nativeValue(hwnd.getPointer());

            InteractiveBrowserCredential interactiveBrowserCredential
                = new InteractiveBrowserBrokerCredentialBuilder().setWindowHandle(hwndValue).build();

            PopTokenAuthenticationPolicy policy = new PopTokenAuthenticationPolicy(interactiveBrowserCredential,
                "https://graph.microsoft.com/.default");
            HttpPipeline pipeline = new HttpPipelineBuilder().policies(policy).build();
            HttpRequest request = new HttpRequest(HttpMethod.GET, "https://graph.microsoft.com/v1.0/me");
            HttpResponse httpResponse = pipeline.sendSync(request, Context.NONE);
            assertEquals(200, httpResponse.getStatusCode());
        });
    }
}
