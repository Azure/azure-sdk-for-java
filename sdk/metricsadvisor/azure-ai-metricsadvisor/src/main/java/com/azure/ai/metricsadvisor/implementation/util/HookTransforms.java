// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.implementation.models.EmailHookInfo;
import com.azure.ai.metricsadvisor.implementation.models.EmailHookInfoPatch;
import com.azure.ai.metricsadvisor.implementation.models.EmailHookParameter;
import com.azure.ai.metricsadvisor.implementation.models.HookInfo;
import com.azure.ai.metricsadvisor.implementation.models.HookInfoPatch;
import com.azure.ai.metricsadvisor.implementation.models.WebhookHookInfo;
import com.azure.ai.metricsadvisor.implementation.models.WebhookHookInfoPatch;
import com.azure.ai.metricsadvisor.implementation.models.WebhookHookParameter;
import com.azure.ai.metricsadvisor.models.EmailNotificationHook;
import com.azure.ai.metricsadvisor.models.NotificationHook;
import com.azure.ai.metricsadvisor.models.WebNotificationHook;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class HookTransforms {

    private HookTransforms() {
    }

    public static HookInfo toInnerForCreate(ClientLogger logger, NotificationHook notificationHook) {
        if (notificationHook instanceof EmailNotificationHook) {
            EmailNotificationHook emailHook = (EmailNotificationHook) notificationHook;
            if (CoreUtils.isNullOrEmpty(emailHook.getName())) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("The notificationHook.name is required."));
            }
            if (emailHook.getEmailsToAlert().isEmpty()) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("At least one email is required in the notificationHook."));
            }
            EmailHookInfo innerEmailHook = new EmailHookInfo();
            innerEmailHook.setHookName(emailHook.getName());
            innerEmailHook.setDescription(emailHook.getDescription());
            innerEmailHook.setExternalLink(emailHook.getExternalLink());
            innerEmailHook.setHookParameter(new EmailHookParameter()
                .setToList(emailHook.getEmailsToAlert()));
            return innerEmailHook;
        } else if (notificationHook instanceof WebNotificationHook) {
            WebNotificationHook webHook = (WebNotificationHook) notificationHook;
            if (CoreUtils.isNullOrEmpty(webHook.getName())) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("The notificationHook.name is required."));
            }
            if (CoreUtils.isNullOrEmpty(webHook.getEndpoint())) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("The notificationHook.endpoint is required."));
            }
            WebhookHookInfo innerWebHook = new WebhookHookInfo();
            innerWebHook.setHookName(webHook.getName());
            innerWebHook.setDescription(webHook.getDescription());
            innerWebHook.setExternalLink(webHook.getExternalLink());
            innerWebHook.setHookParameter(new WebhookHookParameter()
                .setEndpoint(webHook.getEndpoint())
                .setUsername(webHook.getUsername())
                .setPassword(webHook.getPassword())
                .setCertificateKey(webHook.getClientCertificate())
                .setCertificatePassword(webHook.getClientCertificatePassword())
                .setHeaders(webHook.getHttpHeaders().toMap()));
            return innerWebHook;
        } else {
            throw logger
                .logExceptionAsError(new IllegalArgumentException(String.format(
                    "The notificationHook type %s not supported", notificationHook.getClass().getCanonicalName())));
        }
    }

    public static HookInfoPatch toInnerForUpdate(ClientLogger logger, NotificationHook notificationHook) {
        if (notificationHook instanceof EmailNotificationHook) {
            EmailNotificationHook emailHook = (EmailNotificationHook) notificationHook;
            EmailHookInfoPatch innerEmailHook = new EmailHookInfoPatch();
            innerEmailHook.setHookName(emailHook.getName());
            innerEmailHook.setDescription(emailHook.getDescription());
            innerEmailHook.setExternalLink(emailHook.getExternalLink());
            innerEmailHook.setHookParameter(new EmailHookParameter()
                .setToList(emailHook.getEmailsToAlert()));
            return innerEmailHook;
        } else if (notificationHook instanceof WebNotificationHook) {
            WebNotificationHook webHook = (WebNotificationHook) notificationHook;
            WebhookHookInfoPatch innerWebHook = new WebhookHookInfoPatch();
            innerWebHook.setHookName(webHook.getName());
            innerWebHook.setDescription(webHook.getDescription());
            innerWebHook.setExternalLink(webHook.getExternalLink());
            innerWebHook.setHookParameter(new WebhookHookParameter()
                .setEndpoint(webHook.getEndpoint())
                .setUsername(webHook.getUsername())
                .setPassword(webHook.getPassword())
                .setCertificateKey(webHook.getClientCertificate())
                .setCertificatePassword(webHook.getClientCertificatePassword())
                .setHeaders(webHook.getHttpHeaders().toMap()));
            return innerWebHook;
        } else {
            throw logger
                .logExceptionAsError(new IllegalArgumentException(String.format(
                    "The notificationHook type %s not supported", notificationHook.getClass().getCanonicalName())));
        }
    }

    public static NotificationHook fromInner(ClientLogger logger, HookInfo innerHook) {
        if (innerHook instanceof EmailHookInfo) {
            EmailHookInfo innerEmailHook = (EmailHookInfo) innerHook;
            EmailNotificationHook emailHook = new EmailNotificationHook(innerEmailHook.getHookName(),
                innerEmailHook.getHookParameter().getToList());

            emailHook.setDescription(innerEmailHook.getDescription());
            emailHook.setExternalLink(innerEmailHook.getExternalLink());

            HookHelper.setId(emailHook, innerEmailHook.getHookId().toString());

            List<String> adminList = innerEmailHook.getAdmins();
            if (adminList == null) {
                adminList = new ArrayList<>();
            }
            HookHelper.setAdminEmails(emailHook, Collections.unmodifiableList(adminList));

            return emailHook;
        } else if (innerHook instanceof WebhookHookInfo) {
            WebhookHookInfo innerWebHook = (WebhookHookInfo) innerHook;
            WebNotificationHook webHook = new WebNotificationHook(innerWebHook.getHookName(),
                innerWebHook.getHookParameter().getEndpoint());

            webHook.setDescription(innerWebHook.getDescription());
            webHook.setExternalLink(innerWebHook.getExternalLink());

            webHook.setUserCredentials(innerWebHook.getHookParameter().getUsername(),
                innerWebHook.getHookParameter().getPassword());
            webHook.setClientCertificate(innerWebHook.getHookParameter().getCertificateKey(),
                innerWebHook.getHookParameter().getCertificatePassword());

            Map<String, String> innerHeaders = innerWebHook.getHookParameter().getHeaders();
            if (innerHeaders == null) {
                innerHeaders = new HashMap<>();
            }
            webHook.setHttpHeaders(new HttpHeaders(innerHeaders));

            HookHelper.setId(webHook, innerWebHook.getHookId().toString());

            List<String> adminList = innerWebHook.getAdmins();
            if (adminList == null) {
                adminList = new ArrayList<>();
            }
            HookHelper.setAdminEmails(webHook, Collections.unmodifiableList(adminList));

            return webHook;
        } else {
            throw logger.logExceptionAsError(Exceptions.propagate(new RuntimeException(String.format(
                "The hook type %s not supported", innerHook.getClass().getCanonicalName()))));
        }
    }

    public static PagedResponse<NotificationHook> fromInnerPagedResponse(ClientLogger logger,
                                                             PagedResponse<HookInfo> innerResponse) {
        List<NotificationHook> notificationHookList;
        final List<HookInfo> innerHookList = innerResponse.getValue();
        if (innerHookList == null || innerHookList.isEmpty()) {
            notificationHookList = new ArrayList<>();
        } else {
            notificationHookList = innerHookList
                .stream()
                .map(innerAnomaly -> fromInner(logger, innerAnomaly))
                .collect(Collectors.toList());
        }

        final IterableStream<NotificationHook> pageElements
            = new IterableStream<>(notificationHookList);

        return new PagedResponseBase<Void, NotificationHook>(innerResponse.getRequest(),
            innerResponse.getStatusCode(),
            innerResponse.getHeaders(),
            new HookPage(pageElements, innerResponse.getContinuationToken()),
            null);
    }

    private static final class HookPage implements Page<NotificationHook> {
        private final IterableStream<NotificationHook> elements;
        private final String continuationTToken;

        private HookPage(IterableStream<NotificationHook> elements, String continuationTToken) {
            this.elements = elements;
            this.continuationTToken = continuationTToken;
        }

        @Override
        public IterableStream<NotificationHook> getElements() {
            return this.elements;
        }

        @Override
        public String getContinuationToken() {
            return this.continuationTToken;
        }
    }
}
