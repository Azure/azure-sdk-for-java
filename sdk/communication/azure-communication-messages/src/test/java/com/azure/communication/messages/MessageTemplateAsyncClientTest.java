// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.messages;

import com.azure.communication.messages.models.channels.WhatsAppMessageTemplateItem;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTemplateAsyncClientTest extends CommunicationMessagesTestBase {

    MessageTemplateAsyncClient messageTemplateClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldReturnWhatsTemplateListAsync(HttpClient httpClient) {
        messageTemplateClient = buildMessageTemplateAsyncClient(httpClient);

        messageTemplateClient.listTemplates(CHANNEL_REGISTRATION_ID).toStream().forEach(template -> {
            assertNotNull(template.getName());
            assertNotNull(template.getLanguage());
            assertNotNull(template.getStatus());
            assertInstanceOf(WhatsAppMessageTemplateItem.class, template);
            assertNotNull(((WhatsAppMessageTemplateItem) template).getContent());
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldThrowExceptionForInvalidChannelIdAsync(HttpClient httpClient) {
        messageTemplateClient = buildMessageTemplateAsyncClient(httpClient);
        assertThrows(HttpResponseException.class,
            () -> messageTemplateClient.listTemplates("INVALID_CHANNEL_ID").toStream().collect(Collectors.toList()));
    }

    private MessageTemplateAsyncClient buildMessageTemplateAsyncClient(HttpClient httpClient) {
        return getMessageTemplateClientBuilder(httpClient, null).buildAsyncClient();
    }

    private MessageTemplateAsyncClient buildMessageTemplateAsyncClientWithTokenCredential(HttpClient httpClient) {
        TokenCredential tokenCredential;
        if (interceptorManager.isPlaybackMode()) {
            tokenCredential = new MockTokenCredential();
        } else {
            tokenCredential = new DefaultAzureCredentialBuilder().build();
        }

        return getMessageTemplateClientBuilder(httpClient, tokenCredential)
            .addPolicy((context, next) -> logHeaders(next))
            .buildAsyncClient();
    }
}
