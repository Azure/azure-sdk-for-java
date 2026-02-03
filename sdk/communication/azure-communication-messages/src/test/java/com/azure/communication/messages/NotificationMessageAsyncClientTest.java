// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.messages;

import com.azure.communication.messages.models.*;
import com.azure.communication.messages.models.channels.*;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NotificationMessageAsyncClientTest extends CommunicationMessagesTestBase {

    private NotificationMessagesAsyncClient messagesClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendSimpleTextMessage(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);

        StepVerifier
            .create(messagesClient.send(new TextNotificationContent(CHANNEL_REGISTRATION_ID, recipients, "Hello!")))
            .assertNext(resp -> {
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            })
            .verifyComplete();

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendImageMessage(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);
        StepVerifier.create(messagesClient.send(new ImageNotificationContent(CHANNEL_REGISTRATION_ID, recipients,
            "https://wallpapercave.com/wp/wp2163723.jpg"))).assertNext(resp -> {
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendVideoMessage(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);
        StepVerifier.create(messagesClient.send(new VideoNotificationContent(CHANNEL_REGISTRATION_ID, recipients,
            "https://sample-videos.com/video321/mp4/480/big_buck_bunny_480p_1mb.mp4"))).assertNext(resp -> {
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendAudioMessage(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);
        StepVerifier.create(messagesClient.send(new AudioNotificationContent(CHANNEL_REGISTRATION_ID, recipients,
            "https://sample-videos.com/audio/mp3/wave.mp3"))).assertNext(resp -> {
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendDocumentMessage(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);
        StepVerifier.create(messagesClient.send(new AudioNotificationContent(CHANNEL_REGISTRATION_ID, recipients,
            "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"))).assertNext(resp -> {
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendStickerMessage(HttpClient httpClient) {
        String mediaUrl = "https://www.gstatic.com/webp/gallery/1.sm.webp";
        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);
        StepVerifier
            .create(messagesClient.send(new StickerNotificationContent(CHANNEL_REGISTRATION_ID, recipients, mediaUrl)))
            .assertNext(resp -> {
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendReactionMessage(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);
        StepVerifier
            .create(messagesClient.send(new ReactionNotificationContent(CHANNEL_REGISTRATION_ID, recipients,
                "\uD83D\uDE00", "3b5c2a30-936b-4f26-bd5c-491b22e74853")))
            .assertNext(resp -> {
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendInteractiveMessageWithButtonAction(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);
        List<ButtonContent> buttonActions = new ArrayList<>();
        buttonActions.add(new ButtonContent("no", "No"));
        buttonActions.add(new ButtonContent("yes", "Yes"));
        ButtonSetContent buttonSet = new ButtonSetContent(buttonActions);
        InteractiveMessage interactiveMessage = new InteractiveMessage(
            new TextMessageContent("Do you want to proceed?"), new WhatsAppButtonActionBindings(buttonSet));
        StepVerifier
            .create(messagesClient
                .send(new InteractiveNotificationContent(CHANNEL_REGISTRATION_ID, recipients, interactiveMessage)))
            .assertNext(resp -> {
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendInteractiveMessageWithButtonActionWithImageHeader(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);
        List<ButtonContent> buttonActions = new ArrayList<>();
        buttonActions.add(new ButtonContent("no", "No"));
        buttonActions.add(new ButtonContent("yes", "Yes"));
        ButtonSetContent buttonSet = new ButtonSetContent(buttonActions);
        InteractiveMessage interactiveMessage = new InteractiveMessage(
            new TextMessageContent("Do you want to proceed?"), new WhatsAppButtonActionBindings(buttonSet));
        interactiveMessage.setHeader(new ImageMessageContent("https://wallpapercave.com/wp/wp2163723.jpg"));
        StepVerifier
            .create(messagesClient
                .send(new InteractiveNotificationContent(CHANNEL_REGISTRATION_ID, recipients, interactiveMessage)))
            .assertNext(resp -> {
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendInteractiveMessageWithButtonActionWithDocumentHeader(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);
        List<ButtonContent> buttonActions = new ArrayList<>();
        buttonActions.add(new ButtonContent("no", "No"));
        buttonActions.add(new ButtonContent("yes", "Yes"));
        ButtonSetContent buttonSet = new ButtonSetContent(buttonActions);
        InteractiveMessage interactiveMessage = new InteractiveMessage(
            new TextMessageContent("Do you want to proceed?"), new WhatsAppButtonActionBindings(buttonSet));
        interactiveMessage.setHeader(
            new DocumentMessageContent("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"));
        StepVerifier
            .create(messagesClient
                .send(new InteractiveNotificationContent(CHANNEL_REGISTRATION_ID, recipients, interactiveMessage)))
            .assertNext(resp -> {
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendInteractiveMessageWithButtonActionWithVideoHeader(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);
        List<ButtonContent> buttonActions = new ArrayList<>();
        buttonActions.add(new ButtonContent("no", "No"));
        buttonActions.add(new ButtonContent("yes", "Yes"));
        ButtonSetContent buttonSet = new ButtonSetContent(buttonActions);
        InteractiveMessage interactiveMessage = new InteractiveMessage(new TextMessageContent("Do you like it?"),
            new WhatsAppButtonActionBindings(buttonSet));
        interactiveMessage.setHeader(new VideoMessageContent("https://sample-videos.com/audio/mp3/wave.mp3"));
        StepVerifier
            .create(messagesClient
                .send(new InteractiveNotificationContent(CHANNEL_REGISTRATION_ID, recipients, interactiveMessage)))
            .assertNext(resp -> {
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendInteractiveMessageWithListAction(HttpClient httpClient) {
        List<ActionGroupItem> group1 = new ArrayList<>();
        group1.add(new ActionGroupItem("priority_express", "Priority Mail Express", "Delivered on same day!"));
        group1.add(new ActionGroupItem("priority_mail", "Priority Mail", "Delivered in 1-2 days"));

        List<ActionGroupItem> group2 = new ArrayList<>();
        group2.add(new ActionGroupItem("usps_ground_advantage", "USPS Ground Advantage", "Delivered in 2-5 days"));
        group2.add(new ActionGroupItem("media_mail", "Media Mail", "Delivered in 5-8 days"));

        List<ActionGroup> options = new ArrayList<>();
        options.add(new ActionGroup("Express Delivery", group1));
        options.add(new ActionGroup("Normal Delivery", group2));

        ActionGroupContent actionGroupContent = new ActionGroupContent("Shipping Options", options);
        InteractiveMessage interactiveMessage
            = new InteractiveMessage(new TextMessageContent("Which shipping option do you want?"),
                new WhatsAppListActionBindings(actionGroupContent));
        interactiveMessage.setFooter(new TextMessageContent("Eagle Logistic"));
        interactiveMessage.setHeader(new TextMessageContent("Shipping Options"));

        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);
        StepVerifier
            .create(messagesClient
                .send(new InteractiveNotificationContent(CHANNEL_REGISTRATION_ID, recipients, interactiveMessage)))
            .assertNext(resp -> {
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendInteractiveMessageWithUrlAction(HttpClient httpClient) {
        LinkContent urlAction = new LinkContent("Rocket is the best!", "https://wallpapercave.com/wp/wp2163723.jpg");
        InteractiveMessage interactiveMessage = new InteractiveMessage(
            new TextMessageContent("The best Guardian of Galaxy"), new WhatsAppUrlActionBindings(urlAction));
        interactiveMessage.setFooter(new TextMessageContent("Intergalactic New Ltd"));

        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
        List<String> recipients = new ArrayList<>();
        recipients.add(RECIPIENT_IDENTIFIER);
        StepVerifier
            .create(messagesClient
                .send(new InteractiveNotificationContent(CHANNEL_REGISTRATION_ID, recipients, interactiveMessage)))
            .assertNext(resp -> {
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendMessageTemplateWithImage(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
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

        StepVerifier
            .create(messagesClient.send(new TemplateNotificationContent(CHANNEL_REGISTRATION_ID, recipients, template)))
            .assertNext(resp -> {
                assertNotNull(resp.getReceipts());
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendMessageTemplateWithVideo(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
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

        StepVerifier
            .create(messagesClient.send(new TemplateNotificationContent(CHANNEL_REGISTRATION_ID, recipients, template)))
            .assertNext(resp -> {
                assertNotNull(resp.getReceipts());
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendMessageTemplateWithQuickAction(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
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

        StepVerifier
            .create(messagesClient
                .send(new TemplateNotificationContent(CHANNEL_REGISTRATION_ID, recipients, messageTemplate)))
            .assertNext(resp -> {
                assertNotNull(resp.getReceipts());
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void shouldSendMessageTemplateWithDocument(HttpClient httpClient) {
        messagesClient = buildNotificationMessagesAsyncClient(httpClient);
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

        StepVerifier
            .create(messagesClient.send(new TemplateNotificationContent(CHANNEL_REGISTRATION_ID, recipients, template)))
            .assertNext(resp -> {
                assertNotNull(resp.getReceipts());
                assertEquals(1, resp.getReceipts().size());
                assertNotNull(resp.getReceipts().get(0).getMessageId());
            })
            .verifyComplete();
    }

    private NotificationMessagesAsyncClient buildNotificationMessagesAsyncClient(HttpClient httpClient) {
        return getNotificationMessagesClientBuilder(httpClient, null).buildAsyncClient();
    }

    private NotificationMessagesAsyncClient
        buildNotificationMessagesAsyncClientWithTokenCredential(HttpClient httpClient) {
        TokenCredential tokenCredential;
        if (interceptorManager.isPlaybackMode()) {
            tokenCredential = new MockTokenCredential();
        } else {
            tokenCredential = new DefaultAzureCredentialBuilder().build();
        }
        return getNotificationMessagesClientBuilder(httpClient, tokenCredential).buildAsyncClient();
    }
}
