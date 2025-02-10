// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.messages;

import com.azure.communication.messages.models.MessageTemplateItem;
import com.azure.communication.messages.models.channels.WhatsAppMessageTemplateItem;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class GetTemplateListSample {

    private static final String CONNECTION_STRING = System.getenv("ACS_CONNECTION_STRING");
    private static final String CHANNEL_ID = "77ffd898-ec44-42cd-b560-57a8903d05c7";

    public static void main(String[] args) {
        getMessageTemplateWithTokenCredential();
    }

    public static void getMessageTemplateWithTokenCredential() {
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        String endpoint = System.getenv("ACS_END_POINT");
        MessageTemplateClient templateClient =
            new MessageTemplateClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();

        PagedIterable<MessageTemplateItem> response = templateClient.listTemplates(CHANNEL_ID);

        response.stream().forEach(t -> {
            WhatsAppMessageTemplateItem template = (WhatsAppMessageTemplateItem) t;
            System.out.println("===============================");
            System.out.println("Template Name :: " + template.getName());
            System.out.println("Template Language :: " + template.getLanguage());
            System.out.println("Template Status :: " + template.getStatus());
            System.out.println("Template Content :: " + template.getContent());
            System.out.println("===============================");
        });
    }

    public static void getMessageTemplateWithConnectionString() {
        MessageTemplateClient templateClient =
            new MessageTemplateClientBuilder()
                .connectionString(CONNECTION_STRING)
                .buildClient();

        PagedIterable<MessageTemplateItem> response = templateClient.listTemplates(CHANNEL_ID);

        response.stream().forEach(t -> {
            WhatsAppMessageTemplateItem template = (WhatsAppMessageTemplateItem) t;
            System.out.println("===============================");
            System.out.println("Template Name :: " + template.getName());
            System.out.println("Template Language :: " + template.getLanguage());
            System.out.println("Template Status :: " + template.getStatus());
            System.out.println("Template Content :: " + template.getContent());
            System.out.println("===============================");
        });
    }



}
