// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.administration.models.EmailNotificationHook;
import com.azure.ai.metricsadvisor.administration.models.NotificationHook;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.ai.metricsadvisor.administration.models.WebNotificationHook;
import com.azure.core.http.rest.PagedFlux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates how to create, get, update, delete and list hook.
 */
public class HookAsyncSample {
    public static void main(String[] args) {
        final MetricsAdvisorAdministrationAsyncClient advisorAdministrationAsyncClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildAsyncClient();

        // Create email hook.
        NotificationHook emailNotificationHookToCreate = new EmailNotificationHook("email hook")
            .setDescription("my email hook")
            .setEmailsToAlert(new ArrayList<String>() {{ add("alertme@alertme.com"); }})
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");

        Mono<NotificationHook> createHookMono
            = advisorAdministrationAsyncClient
            .createHook(emailNotificationHookToCreate);

        createHookMono
            .doOnSubscribe(__ ->
                System.out.printf("Creating NotificationHook%n"))
            .doOnSuccess(hook ->
                System.out.printf("Created hook: %s%n", hook.getId()));

        // Retrieve the hook that just created.
        Mono<NotificationHook> fetchHookMono = createHookMono
            .flatMap(createdHook -> {
                return advisorAdministrationAsyncClient.getHook(
                    createdHook.getId())
                    .doOnSubscribe(__ ->
                        System.out.printf("Fetching NotificationHook: %s%n", createdHook.getId()))
                    .doOnSuccess(config ->
                        System.out.printf("Fetched NotificationHook%n"))
                    .doOnNext(hook -> {
                        EmailNotificationHook createdEmailHook = (EmailNotificationHook) hook;
                        System.out.printf("NotificationHook Id: %s%n", createdEmailHook.getId());
                        System.out.printf("NotificationHook Name: %s%n", createdEmailHook.getName());
                        System.out.printf("NotificationHook Description: %s%n", createdEmailHook.getDescription());
                        System.out.printf("NotificationHook External Link: %s%n", createdEmailHook.getExternalLink());
                        System.out.printf("NotificationHook Emails: %s%n", String.join(",", createdEmailHook.getEmailsToAlert()));
                    });
            });

        // Update the hook.
        Mono<NotificationHook> updateHookMono = fetchHookMono
            .flatMap(hook -> {
                EmailNotificationHook emailHookToUpdate = (EmailNotificationHook) hook;
                final List<String> emailsToUpdate = new ArrayList<>(emailHookToUpdate.getEmailsToAlert());
                emailsToUpdate.remove("alertme@alertme.com");
                emailsToUpdate.add("alertme2@alertme.com");
                emailsToUpdate.add("alertme3@alertme.com");
                emailHookToUpdate
                    .setEmailsToAlert(emailsToUpdate);
                return advisorAdministrationAsyncClient.updateHook(emailHookToUpdate)
                    .doOnSubscribe(__ ->
                        System.out.printf("Updating Notification Hook: %s%n", hook.getId()))
                    .doOnSuccess(notificationHook ->
                        System.out.printf("Updated Notification Hook emails: %s%n",
                            String.join(",", ((EmailNotificationHook) notificationHook).getEmailsToAlert())));
            });

        // Delete the hook.
        Mono<Void> deleteHookMono = updateHookMono.flatMap(hook -> {
            return advisorAdministrationAsyncClient.deleteHook(hook.getId())
                .doOnSubscribe(__ ->
                    System.out.printf("Deleting Notification Hook: %s%n", hook.getId()))
                .doOnSuccess(config ->
                    System.out.printf("Deleted Notification Hook%n"));
        });

        /*
          This will block until all the above CRUD on operation on email hook is completed.
          This is strongly discouraged for use in production as it eliminates the benefits
          of asynchronous IO. It is used here to ensure the sample runs to completion.
         */
        deleteHookMono.block();

        // Create a web hook
        NotificationHook webNotificationHookToCreate = new WebNotificationHook("web hook", "https://httpbin.org/post")
            .setDescription("my web hook")
            .setUserCredentials("web-user", "web-user-pwd!")
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");

        Mono<NotificationHook> createWebHookMono
            = advisorAdministrationAsyncClient.createHook(webNotificationHookToCreate)
            .doOnSubscribe(__ ->
                System.out.printf("Creating NotificationHook%n"))
            .doOnSuccess(hook ->
                System.out.printf("Created hook: %s%n", hook.getId()));

        /*
          This will block until web hook is created.
          This is strongly discouraged for use in production as it eliminates the benefits
          of asynchronous IO. It is used here to ensure the sample runs to completion.
         */
        createWebHookMono.block();

        // List configurations
        System.out.printf("Listing hooks%n");
        PagedFlux<NotificationHook> hooksFlux
            = advisorAdministrationAsyncClient.listHooks();

        hooksFlux.doOnNext(hook -> {
            if (hook instanceof EmailNotificationHook) {
                EmailNotificationHook emailHook = (EmailNotificationHook) hook;
                System.out.printf("NotificationHook Id: %s%n", emailHook.getId());
                System.out.printf("NotificationHook Name: %s%n", emailHook.getName());
                System.out.printf("NotificationHook Description: %s%n", emailHook.getDescription());
                System.out.printf("NotificationHook External Link: %s%n", emailHook.getExternalLink());
                System.out.printf("NotificationHook Emails: %s%n", String.join(",", emailHook.getEmailsToAlert()));
            } else if (hook instanceof WebNotificationHook) {
                WebNotificationHook webHook = (WebNotificationHook) hook;
                System.out.printf("NotificationHook Id: %s%n", webHook.getId());
                System.out.printf("NotificationHook Name: %s%n", webHook.getName());
                System.out.printf("NotificationHook Description: %s%n", webHook.getDescription());
                System.out.printf("NotificationHook External Link: %s%n", webHook.getExternalLink());
                System.out.printf("NotificationHook Endpoint: %s%n", webHook.getEndpoint());
                System.out.printf("NotificationHook Headers: %s%n", webHook.getHttpHeaders());
            }
        }).blockLast();
    }
}
