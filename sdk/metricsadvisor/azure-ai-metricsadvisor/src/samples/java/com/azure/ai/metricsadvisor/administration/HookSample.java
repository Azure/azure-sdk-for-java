// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.administration.models.EmailNotificationHook;
import com.azure.ai.metricsadvisor.administration.models.NotificationHook;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.ai.metricsadvisor.administration.models.WebNotificationHook;
import com.azure.core.http.rest.PagedIterable;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates how to create, get, update, delete and list hook.
 */
public class HookSample {
    public static void main(String[] args) {
        final MetricsAdvisorAdministrationClient advisorAdministrationClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildClient();

        List<String> emails = new ArrayList<>();
        emails.add("alertme@alertme.com");

        // Create email notificationHook.
        System.out.printf("Creating NotificationHook%n");
        NotificationHook emailNotificationHookToCreate = new EmailNotificationHook("email notification Hook1")
            .setDescription("my email notification Hook")
            .setEmailsToAlert(emails)
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");
        NotificationHook notificationHook = advisorAdministrationClient.createHook(emailNotificationHookToCreate);
        System.out.printf("Created notification Hook: %s%n", notificationHook.getId());

        // Retrieve the notification Hook that just created.
        System.out.printf("Fetching notification Hook: %s%n", notificationHook.getId());
        notificationHook = advisorAdministrationClient.getHook(notificationHook.getId());
        EmailNotificationHook createdEmailHook = (EmailNotificationHook) notificationHook;
        System.out.printf("Email Hook Id: %s%n", createdEmailHook.getId());
        System.out.printf("Email Hook Name: %s%n", createdEmailHook.getName());
        System.out.printf("Email Hook Description: %s%n", createdEmailHook.getDescription());
        System.out.printf("Email Hook External Link: %s%n", createdEmailHook.getExternalLink());
        System.out.printf("Email Hook Emails: %s%n", String.join(",", createdEmailHook.getEmailsToAlert()));

        // Update the notificationHook.
        System.out.printf("Updating notification Hook: %s%n", notificationHook.getId());
        EmailNotificationHook emailHookToUpdate = (EmailNotificationHook) notificationHook;
        final List<String> existingEmails = emailHookToUpdate.getEmailsToAlert();
        final List<String> emailsToUpdate = new ArrayList<>(existingEmails);
        emailsToUpdate.remove("alertme@alertme.com");
        emailsToUpdate.add("alertme2@alertme.com");
        emailsToUpdate.add("alertme3@alertme.com");

        emailHookToUpdate
            .setEmailsToAlert(emailsToUpdate);
        notificationHook = advisorAdministrationClient.updateHook(emailHookToUpdate);
        System.out.printf("Updated notification Hook: %s%n", notificationHook.getId());
        System.out.printf("Updated Email Hook Emails: %s%n",
            String.join(",", ((EmailNotificationHook) notificationHook).getEmailsToAlert()));

        // Delete the notificationHook.
        System.out.printf("Deleting Notification Hook: %s%n", notificationHook.getId());
        advisorAdministrationClient.deleteHook(notificationHook.getId());
        System.out.printf("Deleted Notification Hook%n");

        // Create a web notification Hook
        System.out.printf("Creating web NotificationHook%n");
        NotificationHook webNotificationHookToCreate = new WebNotificationHook("web notification Hook", "https://httpbin.org/post")
            .setDescription("my web notification Hook")
            .setUserCredentials("web-user", "web-user-pwd!")
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");
        advisorAdministrationClient.createHook(webNotificationHookToCreate);
        System.out.printf("Created web notification Hook: %s%n", notificationHook.getId());

        // List hooks.
        System.out.printf("Listing hooks%n");
        PagedIterable<NotificationHook> hooksIterable
            = advisorAdministrationClient.listHooks();
        for (NotificationHook notificationHookItem : hooksIterable) {
            if (notificationHookItem instanceof EmailNotificationHook) {
                EmailNotificationHook emailHook = (EmailNotificationHook) notificationHookItem;
                System.out.printf("Email Hook Id: %s%n", emailHook.getId());
                System.out.printf("Email Hook Name: %s%n", emailHook.getName());
                System.out.printf("Email Hook Description: %s%n", emailHook.getDescription());
                System.out.printf("Email Hook External Link: %s%n", emailHook.getExternalLink());
                System.out.printf("Email Hook Emails: %s%n", String.join(",", emailHook.getEmailsToAlert()));
            } else if (notificationHookItem instanceof WebNotificationHook) {
                WebNotificationHook webHook = (WebNotificationHook) notificationHookItem;
                System.out.printf("Web Hook Id: %s%n", webHook.getId());
                System.out.printf("Web Hook Name: %s%n", webHook.getName());
                System.out.printf("Web Hook Description: %s%n", webHook.getDescription());
                System.out.printf("Web Hook External Link: %s%n", webHook.getExternalLink());
                System.out.printf("Web Hook Endpoint: %s%n", webHook.getEndpoint());
                System.out.printf("Web Hook Headers: %s%n", webHook.getHttpHeaders());
            }
        }
    }
}
