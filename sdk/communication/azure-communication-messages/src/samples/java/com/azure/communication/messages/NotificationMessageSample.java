// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.messages;

import com.azure.communication.messages.models.AudioNotificationContent;
import com.azure.communication.messages.models.DocumentNotificationContent;
import com.azure.communication.messages.models.ImageNotificationContent;
import com.azure.communication.messages.models.MessageTemplate;
import com.azure.communication.messages.models.MessageTemplateBindings;
import com.azure.communication.messages.models.MessageTemplateDocument;
import com.azure.communication.messages.models.MessageTemplateImage;
import com.azure.communication.messages.models.MessageTemplateQuickAction;
import com.azure.communication.messages.models.MessageTemplateText;
import com.azure.communication.messages.models.MessageTemplateValue;
import com.azure.communication.messages.models.MessageTemplateVideo;
import com.azure.communication.messages.models.TextNotificationContent;
import com.azure.communication.messages.models.TemplateNotificationContent;
import com.azure.communication.messages.models.VideoNotificationContent;
import com.azure.communication.messages.models.SendMessageResult;
import com.azure.communication.messages.models.channels.WhatsAppMessageButtonSubType;
import com.azure.communication.messages.models.channels.WhatsAppMessageTemplateBindings;
import com.azure.communication.messages.models.channels.WhatsAppMessageTemplateBindingsButton;
import com.azure.communication.messages.models.channels.WhatsAppMessageTemplateBindingsComponent;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.ArrayList;
import java.util.List;

public class NotificationMessageSample {

    private static final String CONNECTION_STRING = System.getenv("ACS_CONNECTION_STRING");
    private static final String CHANNEL_ID = "77ffd898-ec44-42cd-b560-57a8903d05c7";
    private static final String RECIPIENT_IDENTIFIER = System.getenv("RECIPIENT_IDENTIFIER");
    private static final List<String> TO_LIST = new ArrayList<>();

    public static void main(String[] args) {
        TO_LIST.add(RECIPIENT_IDENTIFIER);
        sendTemplateMessageWithDocument();
    }

    /*
    * This sample shows how to send template message with below details
    * Name: sample_shipping_confirmation, Language: en_US
    *  [
          {
            "type": "BODY",
            "text": "Your package has been shipped. It will be delivered in {{1}} business days."
          },
          {
            "type": "FOOTER",
            "text": "This message is from an unverified business."
          }
        ]
    * */
    private static void sendTemplateMessage() {

        //Update Template Name and language according your template associate to your channel.
        MessageTemplate template = new MessageTemplate("sample_shipping_confirmation", "en_US");

        //Update template parameter type and value
        List<MessageTemplateValue> messageTemplateValues = new ArrayList<>();
        messageTemplateValues.add(new MessageTemplateText("Days", "5"));
        template.setValues(messageTemplateValues);

        //Update template parameter binding
        List<WhatsAppMessageTemplateBindingsComponent> components = new ArrayList<>();
        components.add(new WhatsAppMessageTemplateBindingsComponent("Days"));
        MessageTemplateBindings bindings = new WhatsAppMessageTemplateBindings()
            .setBody(components);
        template.setBindings(bindings);

        NotificationMessagesClient client = createClientWithTokenCredential();
        SendMessageResult result = client.send(
            new TemplateNotificationContent(CHANNEL_ID, TO_LIST, template));

        result.getReceipts().forEach(r -> System.out.println("Message sent to:" + r.getTo() + " and message id:" + r.getMessageId()));
    }

    /*
    * This sample shows how to send template message with below details
    * Name: sample_issue_resolution, Language: en_US
    *  [
          {
            "type": "BODY",
            "text": "Hi {{1}}, were we able to solve the issue that you were facing?"
          },
          {
            "type": "FOOTER",
            "text": "This message is from an unverified business."
          },
          {
            "type": "BUTTONS",
            "buttons": [
              {
                "type": "QUICK_REPLY",
                "text": "Yes"
              },
              {
                "type": "QUICK_REPLY",
                "text": "No"
              }
            ]
          }
        ]
    * */
    private static void sendTextTemplateMessageWithQuickReply() {

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
        templateButtonBindings.add(new WhatsAppMessageTemplateBindingsButton(WhatsAppMessageButtonSubType.QUICK_REPLY, "Yes"));
        templateButtonBindings.add(new WhatsAppMessageTemplateBindingsButton(WhatsAppMessageButtonSubType.QUICK_REPLY, "No"));

        MessageTemplateBindings templateBindings = new WhatsAppMessageTemplateBindings()
            .setBody(templateBodyBindings) // Set the parameter binding for template body
            .setButtons(templateButtonBindings); // Set the parameter binding for template buttons

        MessageTemplate messageTemplate = new MessageTemplate("sample_issue_resolution", "en_US")
            .setBindings(templateBindings)
            .setValues(messageTemplateValues);

        NotificationMessagesClient client = createClientWithConnectionString();
        SendMessageResult result = client.send(
            new TemplateNotificationContent(CHANNEL_ID, TO_LIST, messageTemplate));

        result.getReceipts().forEach(r -> System.out.println("Message sent to:" + r.getTo() + " and message id:" + r.getMessageId()));
    }

    /*
    * This sample shows how to send template message with below details
    * Name: sample_purchase_feedback, Language: en_US
    *  [
          {
            "type": "HEADER",
            "format": "IMAGE"
          },
          {
            "type": "BODY",
            "text": "Thank you for purchasing {{1}}! We value your feedback and would like to learn more about your experience."
          },
          {
            "type": "FOOTER",
            "text": "This message is from an unverified business."
          },
          {
            "type": "BUTTONS",
            "buttons": [
              {
                "type": "URL",
                "text": "Take Survey",
                "url": "https://www.example.com/"
              }
            ]
          }
        ]
    * */
    private static void sendTemplateMessageWithImage() {

        //Update Template Name and language according your template associate to your channel.
        MessageTemplate template = new MessageTemplate("sample_purchase_feedback", "en_US");

        //Add template parameter type with value in a list
        List<MessageTemplateValue> messageTemplateValues = new ArrayList<>();
        messageTemplateValues.add(new MessageTemplateImage("HeaderImage", "https://upload.wikimedia.org/wikipedia/commons/3/30/Building92microsoft.jpg"));
        messageTemplateValues.add(new MessageTemplateText("ProductInfoInBody", "Microsoft Office"));

        // Add parameter binding for template header in a list
        List<WhatsAppMessageTemplateBindingsComponent> templateHeaderBindings = new ArrayList<>();
        templateHeaderBindings.add(new WhatsAppMessageTemplateBindingsComponent("HeaderImage"));

        // Add parameter binding for template body in a list
        List<WhatsAppMessageTemplateBindingsComponent> templateBodyBindings = new ArrayList<>();
        templateBodyBindings.add(new WhatsAppMessageTemplateBindingsComponent("ProductInfoInBody"));

        MessageTemplateBindings templateBindings = new WhatsAppMessageTemplateBindings()
            .setHeaderProperty(templateHeaderBindings) // Set the parameter binding for template header
            .setBody(templateBodyBindings); // Set the parameter binding for template body

        template
            .setBindings(templateBindings)
            .setValues(messageTemplateValues);

        NotificationMessagesClient client = createClientWithConnectionString();
        SendMessageResult result = client.send(
            new TemplateNotificationContent(CHANNEL_ID, TO_LIST, template));

        result.getReceipts().forEach(r -> System.out.println("Message sent to:" + r.getTo() + " and message id:" + r.getMessageId()));
    }

    /*
    * This sample shows how to send template message with below details
    * Name: sample_happy_hour_announcement, Language: en_US
    *  [
          {
            "type": "HEADER",
            "format": "VIDEO"
          },
          {
            "type": "BODY",
            "text": "Happy hour is here! 🍺😀🍸\nPlease be merry and enjoy the day. 🎉\nVenue: {{1}}\nTime: {{2}}"
          },
          {
            "type": "FOOTER",
            "text": "This message is from an unverified business."
          }
        ]
    * */
    private static void sendTemplateMessageWithVideo() {

        //Update Template Name and language according your template associate to your channel.
        MessageTemplate template = new MessageTemplate("sample_happy_hour_announcement", "en_US");

        //Add template parameter type with value in a list
        List<MessageTemplateValue> messageTemplateValues = new ArrayList<>();
        messageTemplateValues.add(new MessageTemplateVideo("HeaderVideo", "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4"));
        messageTemplateValues.add(new MessageTemplateText("VenueInfoInBody", "Starbucks"));
        messageTemplateValues.add(new MessageTemplateText("TimeInfoInBody", "Today 2-4PM"));

        // Add parameter binding for template header in a list
        List<WhatsAppMessageTemplateBindingsComponent> templateHeaderBindings = new ArrayList<>();
        templateHeaderBindings.add(new WhatsAppMessageTemplateBindingsComponent("HeaderVideo"));

        // Add parameter binding for template body in a list
        List<WhatsAppMessageTemplateBindingsComponent> templateBodyBindings = new ArrayList<>();
        templateBodyBindings.add(new WhatsAppMessageTemplateBindingsComponent("VenueInfoInBody"));
        templateBodyBindings.add(new WhatsAppMessageTemplateBindingsComponent("TimeInfoInBody"));

        MessageTemplateBindings templateBindings = new WhatsAppMessageTemplateBindings()
            .setHeaderProperty(templateHeaderBindings) // Set the parameter binding for template header
            .setBody(templateBodyBindings); // Set the parameter binding for template body

        template
            .setBindings(templateBindings)
            .setValues(messageTemplateValues);

        NotificationMessagesClient client = createClientWithConnectionString();
        SendMessageResult result = client.send(
            new TemplateNotificationContent(CHANNEL_ID, TO_LIST, template));

        result.getReceipts().forEach(r -> System.out.println("Message sent to:" + r.getTo() + " and message id:" + r.getMessageId()));
    }

    /*
   * This sample shows how to send template message with below details
   * Name: sample_flight_confirmation, Language: en_US
   *  [
          {
            "type": "HEADER",
            "format": "DOCUMENT"
          },
          {
            "type": "BODY",
            "text": "This is your flight confirmation for {{1}}-{{2}} on {{3}}."
          },
          {
            "type": "FOOTER",
            "text": "This message is from an unverified business."
          }
        ]
   * */
    private static void sendTemplateMessageWithDocument() {

        //Update Template Name and language according your template associate to your channel.
        MessageTemplate template = new MessageTemplate("sample_flight_confirmation", "en_US");

        //Add template parameter type with value in a list
        List<MessageTemplateValue> messageTemplateValues = new ArrayList<>();
        messageTemplateValues.add(new MessageTemplateDocument("HeaderDoc", "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"));
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

        MessageTemplateBindings templateBindings = new WhatsAppMessageTemplateBindings()
            .setHeaderProperty(templateHeaderBindings) // Set the parameter binding for template header
            .setBody(templateBodyBindings); // Set the parameter binding for template body

        template
            .setBindings(templateBindings)
            .setValues(messageTemplateValues);

        NotificationMessagesClient client = createClientWithConnectionString();
        SendMessageResult result = client.send(
            new TemplateNotificationContent(CHANNEL_ID, TO_LIST, template));

        result.getReceipts().forEach(r -> System.out.println("Message sent to:" + r.getTo() + " and message id:" + r.getMessageId()));
    }

    /*
    * This sample shows how to send simple text message with below details
    * Note: Business cannot initiate conversation with text message.
    * */
    private static void sendTextMessage() {
        NotificationMessagesClient client = createClientWithAzureKeyCredential();
        SendMessageResult result = client.send(
            new TextNotificationContent(CHANNEL_ID, TO_LIST, "Hello from ACS messaging"));

        result.getReceipts().forEach(r -> System.out.println("Message sent to:" + r.getTo() + " and message id:" + r.getMessageId()));
    }

    /*
     * This sample shows how to send image message with below details
     * Note: Business cannot initiate conversation with media message.
     * */
    public static void sendImageMessage() {
        //Update the Media URL
        String mediaUrl = "https://wallpapercave.com/wp/wp2163723.jpg";

        NotificationMessagesClient client = createClientWithConnectionString();
        SendMessageResult result = client.send(
            new ImageNotificationContent(CHANNEL_ID, TO_LIST, mediaUrl));

        result.getReceipts().forEach(r -> System.out.println("Message sent to:" + r.getTo() + " and message id:" + r.getMessageId()));
    }

    /*
     * This sample shows how to send video message with below details
     * Supported video - video/3gp (.3gp), video/mp4 (.mp4)
     * Note: Business cannot initiate conversation with media message.
     * */
    public void sendVideoMessage() {
        //Update the Media URL
        String mediaUrl = "https://sample-videos.com/video321/mp4/480/big_buck_bunny_480p_1mb.mp4";
        List<String> recipients = new ArrayList<>();
        recipients.add("<RECIPIENT_IDENTIFIER e.g. PhoneNumber>");
        NotificationMessagesClient client = new NotificationMessagesClientBuilder()
            .connectionString("<CONNECTION_STRING>")
            .buildClient();
        SendMessageResult result = client.send(
            new VideoNotificationContent("<CHANNEL_ID>", recipients, mediaUrl));

        result.getReceipts().forEach(r -> System.out.println("Message sent to:" + r.getTo() + " and message id:" + r.getMessageId()));
    }

    /*
     * This sample shows how to send audio message with below details
     * Supported audio - audio/aac (.aac), audio/amr (.amr), audio/mpeg (.mp3), audio/a4a (.mp4), audio/ogg (.ogg )
     * Note: Business cannot initiate conversation with media message.
     * */
    public void sendAudioMessage() {
        //Update the Media URL
        String mediaUrl = "https://sample-videos.com/audio/mp3/wave.mp3";
        List<String> recipients = new ArrayList<>();
        recipients.add("<RECIPIENT_IDENTIFIER e.g. PhoneNumber>");
        NotificationMessagesClient client = new NotificationMessagesClientBuilder()
            .connectionString("<CONNECTION_STRING>")
            .buildClient();
        SendMessageResult result = client.send(
            new AudioNotificationContent("<CHANNEL_ID>", recipients, mediaUrl));

        result.getReceipts().forEach(r -> System.out.println("Message sent to:" + r.getTo() + " and message id:" + r.getMessageId()));
    }

    /*
     * This sample shows how to send document message with below details
     * Supported Document type - Plain Text (.txt), PDF (.pdf), Microsoft Excel, Word, PowerPoint
     * Note: Business cannot initiate conversation with media message.
     * */
    public void sendDocumentMessage() {
        //Update the Media URL
        String mediaUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf";
        List<String> recipients = new ArrayList<>();
        recipients.add("<RECIPIENT_IDENTIFIER e.g. PhoneNumber>");
        NotificationMessagesClient client = new NotificationMessagesClientBuilder()
            .connectionString("<CONNECTION_STRING>")
            .buildClient();
        SendMessageResult result = client.send(
            new DocumentNotificationContent("<CHANNEL_ID>", recipients, mediaUrl));

        result.getReceipts().forEach(r -> System.out.println("Message sent to:" + r.getTo() + " and message id:" + r.getMessageId()));
    }

    private static NotificationMessagesClient createClientWithConnectionString() {
        return new NotificationMessagesClientBuilder()
            .connectionString(CONNECTION_STRING)
            .buildClient();
    }

    public static NotificationMessagesClient createClientWithTokenCredential() {
        String endpoint = System.getenv("ACS_END_POINT");
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        return new NotificationMessagesClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient();
    }

    public static NotificationMessagesClient createClientWithAzureKeyCredential() {
        String endpoint =  System.getenv("ACS_END_POINT");
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential(System.getenv("ACS_ACCESS_KEY"));
        return new NotificationMessagesClientBuilder()
            .endpoint(endpoint)
            .credential(azureKeyCredential)
            .buildClient();
    }
}
