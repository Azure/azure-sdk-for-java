// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.messages;

import com.azure.communication.messages.models.channels.WhatsAppMessageTemplateItem;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTemplateClientTest extends CommunicationMessagesTestBase {

    MessageTemplateClient messageTemplateClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldReturnWhatsTemplateList(HttpClient httpClient) {
        messageTemplateClient = buildMessageTemplateClient(httpClient);

        messageTemplateClient.listTemplates(CHANNEL_REGISTRATION_ID).iterableByPage().forEach(resp -> {
            assertEquals(200, resp.getStatusCode());
            resp.getValue().forEach(template -> {
                assertNotNull(template.getName());
                assertNotNull(template.getLanguage());
                assertNotNull(template.getStatus());
                assertInstanceOf(WhatsAppMessageTemplateItem.class, template);
                assertNotNull(((WhatsAppMessageTemplateItem) template).getContent());
            });
        });

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldReturnWhatsTemplateListInSinglePage(HttpClient httpClient) {
        messageTemplateClient = buildMessageTemplateClient(httpClient);

        messageTemplateClient.listTemplates(CHANNEL_REGISTRATION_ID).stream().forEach(template -> {
            assertNotNull(template.getName());
            assertNotNull(template.getLanguage());
            assertNotNull(template.getStatus());
            assertInstanceOf(WhatsAppMessageTemplateItem.class, template);
            assertNotNull(((WhatsAppMessageTemplateItem) template).getContent());
        });
    }

    private MessageTemplateClient buildMessageTemplateClient(HttpClient httpClient) {
        return getMessageTemplateClientBuilder(httpClient, null).buildClient();
    }

    private MessageTemplateClient buildMessageTemplateClientWithTokenCredential(HttpClient httpClient) {
        TokenCredential tokenCredential;
        if (interceptorManager.isPlaybackMode()) {
            tokenCredential = new MockTokenCredential();
        } else {
            tokenCredential = new DefaultAzureCredentialBuilder().build();
        }
        return getMessageTemplateClientBuilder(httpClient, tokenCredential)
            .addPolicy((context, next) -> logHeaders(next))
            .buildClient();
    }
}
