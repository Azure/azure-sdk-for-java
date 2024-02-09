package com.azure.communication.messages;

import com.azure.communication.messages.models.MessageTemplateItem;
import com.azure.communication.messages.models.channels.WhatsAppMessageTemplateItem;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTemplateAsyncClientTest extends CommunicationMessagesTestBase{

    MessageTemplateAsyncClient messageTemplateClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldReturnWhatsTemplateListAsync(HttpClient httpClient){
        messageTemplateClient = buildMessageTemplateAsyncClient(httpClient);

        messageTemplateClient.listTemplates(CHANNEL_REGISTRATION_ID)
            .toStream().
            forEach(template -> {
                assertNotNull(template.getName());
                assertNotNull(template.getLanguage());
                assertNotNull(template.getStatus());
                assertInstanceOf(WhatsAppMessageTemplateItem.class, template);
                assertNotNull(((WhatsAppMessageTemplateItem)template).getContent());
            });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldReturnWhatsTemplateListWithTokenCredentialAsyncClient(HttpClient httpClient) {
        messageTemplateClient = buildMessageTemplateAsyncClientWithTokenCredential(httpClient);

        messageTemplateClient.listTemplates(CHANNEL_REGISTRATION_ID)
            .toStream().
            forEach(template -> {
                assertNotNull(template.getName());
                assertNotNull(template.getLanguage());
                assertNotNull(template.getStatus());
                assertInstanceOf(WhatsAppMessageTemplateItem.class, template);
                assertNotNull(((WhatsAppMessageTemplateItem)template).getContent());
            });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldThrowExceptionForInvalidChannelIdAsync(HttpClient httpClient) {
        messageTemplateClient = buildMessageTemplateAsyncClient(httpClient);
        assertThrows(HttpResponseException.class,
            () -> messageTemplateClient.listTemplates("INVALID_CHANNEL_ID")
                .toStream()
                .collect(Collectors.toList()));
    }
}
