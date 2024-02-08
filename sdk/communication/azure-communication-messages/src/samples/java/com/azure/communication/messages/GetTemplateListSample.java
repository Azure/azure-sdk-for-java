package com.azure.communication.messages;

import com.azure.communication.messages.models.MessageTemplateItem;
import com.azure.communication.messages.models.channels.WhatsAppMessageTemplateItem;
import com.azure.core.http.rest.PagedIterable;

public class GetTemplateListSample {

    private static final String connectionString = System.getenv("ACS_CONNECTION_STRING");
    private static final String channelRegistrationId = "77ffd898-ec44-42cd-b560-57a8903d05c7";

    public static void main(String[] args) {
        MessageTemplateClient templateClient =
            new MessageTemplateClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        PagedIterable<MessageTemplateItem> response = templateClient.listTemplates(channelRegistrationId);

        response.stream().forEach(t -> {
            WhatsAppMessageTemplateItem template = (WhatsAppMessageTemplateItem) t ;
            System.out.println("===============================");
            System.out.println("Template Name :: "+template.getName());
            System.out.println("Template Language :: "+template.getLanguage());
            System.out.println("Template Status :: "+template.getStatus());
            System.out.println("Template Content :: "+template.getContent());
            System.out.println("===============================");
        });
    }

}
