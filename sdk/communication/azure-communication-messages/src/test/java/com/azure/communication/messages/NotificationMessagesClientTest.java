// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.messages;

import com.azure.communication.messages.models.*;
import com.azure.communication.messages.models.channels.WhatsAppMessageButtonSubType;
import com.azure.communication.messages.models.channels.WhatsAppMessageTemplateBindings;
import com.azure.communication.messages.models.channels.WhatsAppMessageTemplateBindingsButton;
import com.azure.communication.messages.models.channels.WhatsAppMessageTemplateBindingsComponent;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NotificationMessagesClientTest extends CommunicationMessagesTestBase {

    private NotificationMessagesClient messagesClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendSimpleTextMessage(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);

        SendMessageResult result
            = messagesClient.send(new TextNotificationContent(CHANNEL_REGISTRATION_ID, recipients, "Hello!"));

        assertEquals(1, result.getReceipts().size());
        assertNotNull(result.getReceipts().get(0).getMessageId());

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendImageMessage(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);

        SendMessageResult result = messagesClient.send(new ImageNotificationContent(CHANNEL_REGISTRATION_ID, recipients,
            "https://wallpapercave.com/wp/wp2163723.jpg"));

        assertEquals(1, result.getReceipts().size());
        assertNotNull(result.getReceipts().get(0).getMessageId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendImageMessageWithCaption(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);
        ImageNotificationContent imageMessage = new ImageNotificationContent(CHANNEL_REGISTRATION_ID, recipients,
            "https://wallpapercave.com/wp/wp2163723.jpg");
        imageMessage.setCaption("wow!");

        SendMessageResult result = messagesClient.send(imageMessage);

        assertEquals(1, result.getReceipts().size());
        assertNotNull(result.getReceipts().get(0).getMessageId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendVideoMessage(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);

        SendMessageResult result = messagesClient.send(new VideoNotificationContent(CHANNEL_REGISTRATION_ID, recipients,
            "https://sample-videos.com/video321/mp4/480/big_buck_bunny_480p_1mb.mp4"));

        assertEquals(1, result.getReceipts().size());
        assertNotNull(result.getReceipts().get(0).getMessageId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendAudioMessage(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);

        SendMessageResult result = messagesClient.send(new AudioNotificationContent(CHANNEL_REGISTRATION_ID, recipients,
            "https://sample-videos.com/audio/mp3/wave.mp3"));

        assertEquals(1, result.getReceipts().size());
        assertNotNull(result.getReceipts().get(0).getMessageId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendDocumentMessage(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);

        SendMessageResult result = messagesClient.send(new DocumentNotificationContent(CHANNEL_REGISTRATION_ID,
            recipients, "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"));

        assertEquals(1, result.getReceipts().size());
        assertNotNull(result.getReceipts().get(0).getMessageId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendMessageImageTemplate(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);

        //Update Template Name and language according your template associate to your channel.
        MessageTemplate template = new MessageTemplate("sample_shipping_confirmation", "en_US");

        //Update template parameter type and value
        List<MessageTemplateValue> messageTemplateValues = new ArrayList<>();
        messageTemplateValues.add(new MessageTemplateText("Days", "5"));
        template.setValues(messageTemplateValues);

        //Update template parameter binding
        List<WhatsAppMessageTemplateBindingsComponent> components = new ArrayList<>();
        components.add(new WhatsAppMessageTemplateBindingsComponent("Days"));
        MessageTemplateBindings bindings = new WhatsAppMessageTemplateBindings().setBody(components);
        template.setBindings(bindings);

        SendMessageResult result
            = messagesClient.send(new TemplateNotificationContent(CHANNEL_REGISTRATION_ID, recipients, template));

        assertEquals(1, result.getReceipts().size());
        assertNotNull(result.getReceipts().get(0).getMessageId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendMessageTemplateWithVideo(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);

        //Update Template Name and language according your template associate to your channel.
        MessageTemplate template = new MessageTemplate("sample_happy_hour_announcement", "en_US");

        //Add template parameter type with value in a list
        List<MessageTemplateValue> messageTemplateValues = new ArrayList<>();
        messageTemplateValues.add(new MessageTemplateVideo("HeaderVideo",
            "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4"));
        messageTemplateValues.add(new MessageTemplateText("VenueInfoInBody", "Starbucks"));
        messageTemplateValues.add(new MessageTemplateText("TimeInfoInBody", "Today 2-4PM"));

        // Add parameter binding for template header in a list
        List<WhatsAppMessageTemplateBindingsComponent> templateHeaderBindings = new ArrayList<>();
        templateHeaderBindings.add(new WhatsAppMessageTemplateBindingsComponent("HeaderVideo"));

        // Add parameter binding for template body in a list
        List<WhatsAppMessageTemplateBindingsComponent> templateBodyBindings = new ArrayList<>();
        templateBodyBindings.add(new WhatsAppMessageTemplateBindingsComponent("VenueInfoInBody"));
        templateBodyBindings.add(new WhatsAppMessageTemplateBindingsComponent("TimeInfoInBody"));

        MessageTemplateBindings templateBindings
            = new WhatsAppMessageTemplateBindings().setHeaderProperty(templateHeaderBindings) // Set the parameter binding for template header
                .setBody(templateBodyBindings); // Set the parameter binding for template body

        template.setBindings(templateBindings).setValues(messageTemplateValues);

        SendMessageResult result
            = messagesClient.send(new TemplateNotificationContent(CHANNEL_REGISTRATION_ID, recipients, template));

        assertEquals(1, result.getReceipts().size());
        assertNotNull(result.getReceipts().get(0).getMessageId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendMessageTemplateWithQuickAction(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);

        //Add template parameter type with value in a list
        List<MessageTemplateValue> messageTemplateValues = new ArrayList<>();
        messageTemplateValues.add(new MessageTemplateText("Name", "Arif"));
        messageTemplateValues.add(new MessageTemplateQuickAction("Yes").setPayload("Yes"));
        messageTemplateValues.add(new MessageTemplateQuickAction("No").setPayload("No"));

        // Add parameter binding for template body in a list
        List<WhatsAppMessageTemplateBindingsComponent> templateBodyBindings = new ArrayList<>();
        templateBodyBindings.add(new WhatsAppMessageTemplateBindingsComponent("Name"));

        // Add parameter binding for template buttons in a list
        List<WhatsAppMessageTemplateBindingsButton> templateButtonBindings = new ArrayList<>();
        templateButtonBindings
            .add(new WhatsAppMessageTemplateBindingsButton(WhatsAppMessageButtonSubType.QUICK_REPLY, "Yes"));
        templateButtonBindings
            .add(new WhatsAppMessageTemplateBindingsButton(WhatsAppMessageButtonSubType.QUICK_REPLY, "No"));

        MessageTemplateBindings templateBindings = new WhatsAppMessageTemplateBindings().setBody(templateBodyBindings) // Set the parameter binding for template body
            .setButtons(templateButtonBindings); // Set the parameter binding for template buttons

        MessageTemplate messageTemplate
            = new MessageTemplate("sample_issue_resolution", "en_US").setBindings(templateBindings)
                .setValues(messageTemplateValues);

        SendMessageResult result = messagesClient
            .send(new TemplateNotificationContent(CHANNEL_REGISTRATION_ID, recipients, messageTemplate));

        assertEquals(1, result.getReceipts().size());
        assertNotNull(result.getReceipts().get(0).getMessageId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendMessageTemplateWithDocument(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);

        //Update Template Name and language according your template associate to your channel.
        MessageTemplate template = new MessageTemplate("sample_flight_confirmation", "en_US");

        //Add template parameter type with value in a list
        List<MessageTemplateValue> messageTemplateValues = new ArrayList<>();
        messageTemplateValues.add(new MessageTemplateDocument("HeaderDoc",
            "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"));
        messageTemplateValues.add(new MessageTemplateText("SourceInfoInBody", "RDU"));
        messageTemplateValues.add(new MessageTemplateText("DestinationInfoInBody", "LAX"));
        messageTemplateValues.add(new MessageTemplateText("TimeInfoInBody", "June 4th, 2024 @ 2PM"));

        // Add parameter binding for template header in a list
        List<WhatsAppMessageTemplateBindingsComponent> templateHeaderBindings = new ArrayList<>();
        templateHeaderBindings.add(new WhatsAppMessageTemplateBindingsComponent("HeaderDoc"));

        // Add parameter binding for template body in a list
        List<WhatsAppMessageTemplateBindingsComponent> templateBodyBindings = new ArrayList<>();
        templateBodyBindings.add(new WhatsAppMessageTemplateBindingsComponent("SourceInfoInBody"));
        templateBodyBindings.add(new WhatsAppMessageTemplateBindingsComponent("DestinationInfoInBody"));
        templateBodyBindings.add(new WhatsAppMessageTemplateBindingsComponent("TimeInfoInBody"));

        MessageTemplateBindings templateBindings
            = new WhatsAppMessageTemplateBindings().setHeaderProperty(templateHeaderBindings) // Set the parameter binding for template header
                .setBody(templateBodyBindings); // Set the parameter binding for template body

        template.setBindings(templateBindings).setValues(messageTemplateValues);

        SendMessageResult result
            = messagesClient.send(new TemplateNotificationContent(CHANNEL_REGISTRATION_ID, recipients, template));

        assertEquals(1, result.getReceipts().size());
        assertNotNull(result.getReceipts().get(0).getMessageId());
    }

    private NotificationMessagesClient buildNotificationMessagesClient(HttpClient httpClient) {
        return getNotificationMessagesClientBuilder(httpClient, null).buildClient();
    }

    private NotificationMessagesClient buildNotificationMessagesClientWithTokenCredential(HttpClient httpClient) {
        TokenCredential tokenCredential;
        if (interceptorManager.isPlaybackMode()) {
            tokenCredential = new MockTokenCredential();
        } else {
            tokenCredential = new DefaultAzureCredentialBuilder().build();
        }
        return getNotificationMessagesClientBuilder(httpClient, tokenCredential).buildClient();
    }
}
