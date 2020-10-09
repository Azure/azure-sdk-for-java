// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.models.EmailHook;
import com.azure.ai.metricsadvisor.models.Hook;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.ai.metricsadvisor.models.WebHook;
import com.azure.core.http.rest.PagedFlux;
import reactor.core.publisher.Mono;

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
        Hook emailHookToCreate = new EmailHook("email hook")
            .setDescription("my email hook")
            .addEmailToAlert("alertme@alertme.com")
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");

        Mono<Hook> createHookMono
            = advisorAdministrationAsyncClient
            .createHook(emailHookToCreate);

        createHookMono
            .doOnSubscribe(__ ->
                System.out.printf("Creating Hook%n"))
            .doOnSuccess(hook ->
                System.out.printf("Created hook: %s%n", hook.getId()));

        // Retrieve the hook that just created.
        Mono<Hook> fetchHookMono = createHookMono
            .flatMap(createdHook -> {
                return advisorAdministrationAsyncClient.getHook(
                    createdHook.getId())
                    .doOnSubscribe(__ ->
                        System.out.printf("Fetching Hook: %s%n", createdHook.getId()))
                    .doOnSuccess(config ->
                        System.out.printf("Fetched Hook%n"))
                    .doOnNext(hook -> {
                        EmailHook createdEmailHook = (EmailHook) hook;
                        System.out.printf("Hook Id: %s%n", createdEmailHook.getId());
                        System.out.printf("Hook Name: %s%n", createdEmailHook.getName());
                        System.out.printf("Hook Description: %s%n", createdEmailHook.getDescription());
                        System.out.printf("Hook External Link: %s%n", createdEmailHook.getExternalLink());
                        System.out.printf("Hook Emails: %s%n", String.join(",", createdEmailHook.getEmailsToAlert()));
                    });
            });

        // Update the hook.
        Mono<Hook> updateHookMono = fetchHookMono
            .flatMap(hook -> {
                EmailHook emailHookToUpdate = (EmailHook) hook;
                emailHookToUpdate
                    .removeEmailToAlert("alertme@alertme.com")
                    .addEmailToAlert("alertme2@alertme.com")
                    .addEmailToAlert("alertme3@alertme.com");
                return advisorAdministrationAsyncClient.updateHook(emailHookToUpdate)
                    .doOnSubscribe(__ ->
                        System.out.printf("Updating Hook: %s%n", hook.getId()))
                    .doOnSuccess(config ->
                        System.out.printf("Updated Hook%n"));
            });

        // Delete the hook.
        Mono<Void> deleteHookMono = updateHookMono.flatMap(hook -> {
            return advisorAdministrationAsyncClient.deleteHook(hook.getId())
                .doOnSubscribe(__ ->
                    System.out.printf("Deleting Hook: %s%n", hook.getId()))
                .doOnSuccess(config ->
                    System.out.printf("Deleted Hook%n"));
        });

        /*
          This will block until all the above CRUD on operation on email hook is completed.
          This is strongly discouraged for use in production as it eliminates the benefits
          of asynchronous IO. It is used here to ensure the sample runs to completion.
         */
        deleteHookMono.block();

        // Create a web hook
        Hook webHookToCreate = new WebHook("web hook", "https://httpbin.org/post")
            .setDescription("my web hook")
            .setUserCredentials("web-user", "web-user-pwd!")
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");

        Mono<Hook> createWebHookMono
            = advisorAdministrationAsyncClient.createHook(webHookToCreate)
            .doOnSubscribe(__ ->
                System.out.printf("Creating Hook%n"))
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
        PagedFlux<Hook> hooksFlux
            = advisorAdministrationAsyncClient.listHooks();

        hooksFlux.doOnNext(hook -> {
            if (hook instanceof EmailHook) {
                EmailHook emailHook = (EmailHook) hook;
                System.out.printf("Hook Id: %s%n", emailHook.getId());
                System.out.printf("Hook Name: %s%n", emailHook.getName());
                System.out.printf("Hook Description: %s%n", emailHook.getDescription());
                System.out.printf("Hook External Link: %s%n", emailHook.getExternalLink());
                System.out.printf("Hook Emails: %s%n", String.join(",", emailHook.getEmailsToAlert()));
            } else if (hook instanceof WebHook) {
                WebHook webHook = (WebHook) hook;
                System.out.printf("Hook Id: %s%n", webHook.getId());
                System.out.printf("Hook Name: %s%n", webHook.getName());
                System.out.printf("Hook Description: %s%n", webHook.getDescription());
                System.out.printf("Hook External Link: %s%n", webHook.getExternalLink());
                System.out.printf("Hook Endpoint: %s%n", webHook.getEndpoint());
                System.out.printf("Hook Headers: %s%n", webHook.getHttpHeaders());
            }
        }).blockLast();
    }
}
