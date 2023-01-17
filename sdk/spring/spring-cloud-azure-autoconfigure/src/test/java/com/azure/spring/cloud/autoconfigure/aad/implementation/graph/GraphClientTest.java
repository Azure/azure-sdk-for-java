package com.azure.spring.cloud.autoconfigure.aad.implementation.graph;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({ OutputCaptureExtension.class})
class GraphClientTest {

    @Test
    void testGetGroupInformationWithRestClientException(CapturedOutput capturedOutput) {
        AadAuthenticationProperties properties = mock(AadAuthenticationProperties.class);
        when(properties.getGraphMembershipUri()).thenReturn("https://graph.microsoft.com/v1.0/me/memberOf");
        GraphClient graphClient = new GraphClient(properties, new RestTemplateBuilder());
        graphClient.getGroupInformation("fake-accesstoken");
        String allOutput = capturedOutput.getAll();
        assertTrue(allOutput.contains("Can not get group information from graph server."));
    }
}