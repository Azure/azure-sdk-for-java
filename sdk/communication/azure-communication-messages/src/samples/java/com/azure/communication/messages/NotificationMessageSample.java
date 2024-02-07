package com.azure.communication.messages;

import com.azure.communication.messages.models.*;
import com.azure.communication.messages.models.whatsapp.WhatsAppMessageButtonSubType;
import com.azure.communication.messages.models.whatsapp.WhatsAppMessageTemplateBindings;
import com.azure.communication.messages.models.whatsapp.WhatsAppMessageTemplateBindingsButton;
import com.azure.communication.messages.models.whatsapp.WhatsAppMessageTemplateBindingsComponent;

import java.util.List;

public class NotificationMessageSample {

    public static void main(String[] args) {
        sendTextTemplateMessage();
    }

    private static void sendTextTemplateMessage() {
        MessageTemplateValue nameText = new MessageTemplateText("Name", "Arif");
        MessageTemplateValue yesQuickReply = new MessageTemplateQuickAction("Yes").setPayload("Yes");
        MessageTemplateValue noQuickReply = new MessageTemplateQuickAction("No").setPayload("No");

        MessageTemplateBindings templateBindings = new WhatsAppMessageTemplateBindings()
            .setBody(List.of(new WhatsAppMessageTemplateBindingsComponent("name")))
            .setButtons(List.of(
                new WhatsAppMessageTemplateBindingsButton(WhatsAppMessageButtonSubType.QUICK_REPLY, "Yes"),
                new WhatsAppMessageTemplateBindingsButton(WhatsAppMessageButtonSubType.QUICK_REPLY, "No")
                ));

        MessageTemplate messageTemplate = new MessageTemplate("sample_issue_resolution", "en_US")
            .setBindings(templateBindings)
            .setValues(List.of(nameText, yesQuickReply, noQuickReply));

        NotificationMessagesClient client = createClientWithConnectionString();
        SendMessageResult result = client.send(
            new TemplateNotificationContent("channel_id", List.of("phone_num"), messageTemplate));

        result.getReceipts().forEach(r -> System.out.println("Message sent to:"+r.getTo() + " and message id:"+ r.getMessageId()));
    }

    private static NotificationMessagesClient createClientWithConnectionString() {
        String connectionString = "<conn_str>";
        return new NotificationMessagesClientBuilder()
            .connectionString(connectionString)
            .buildClient();
    }
}
