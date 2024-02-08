package com.azure.communication.messages;

import com.azure.communication.messages.models.channels.WhatsAppMessageTemplateItem;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTemplateClientTest extends CommunicationMessagesTestBase {

    MessageTemplateClient messageTemplateClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldReturnWhatsTemplateList(HttpClient httpClient){
        messageTemplateClient = buildMessageTemplateClient(httpClient);

        messageTemplateClient.listTemplates(CHANNEL_REGISTRATION_ID)
            .iterableByPage()
                .forEach(resp -> {
                    assertEquals(200, resp.getStatusCode());
                    resp.getValue()
                        .forEach(template -> {
                            assertNotNull(template.getName());
                            assertNotNull(template.getLanguage());
                            assertNotNull(template.getStatus());
                            assertInstanceOf(WhatsAppMessageTemplateItem.class, template);
                            assertNotNull(((WhatsAppMessageTemplateItem)template).getContent());
                        });
                });

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldReturnWhatsTemplateListWithTokenCredentialClient(HttpClient httpClient) {
        messageTemplateClient = buildMessageTemplateClientWithTokenCredential(httpClient);

        messageTemplateClient.listTemplates(CHANNEL_REGISTRATION_ID)
            .stream()
            .forEach(template -> {
                assertNotNull(template.getName());
                assertNotNull(template.getLanguage());
                assertNotNull(template.getStatus());
                assertInstanceOf(WhatsAppMessageTemplateItem.class, template);
                assertNotNull(((WhatsAppMessageTemplateItem)template).getContent());
            });
    }
}
