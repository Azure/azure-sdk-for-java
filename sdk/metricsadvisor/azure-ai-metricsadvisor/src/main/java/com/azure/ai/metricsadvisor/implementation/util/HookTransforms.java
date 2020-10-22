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
import com.azure.ai.metricsadvisor.models.EmailHook;
import com.azure.ai.metricsadvisor.models.Hook;
import com.azure.ai.metricsadvisor.models.WebHook;
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

    public static HookInfo toInnerForCreate(ClientLogger logger, Hook hook) {
        if (hook instanceof EmailHook) {
            EmailHook emailHook = (EmailHook) hook;
            if (CoreUtils.isNullOrEmpty(emailHook.getName())) {
                throw logger.logExceptionAsError(new IllegalArgumentException("The hook.name is required."));
            }
            if (emailHook.getEmailsToAlert().isEmpty()) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("At least one email is required in the hook."));
            }
            EmailHookInfo innerEmailHook = new EmailHookInfo();
            innerEmailHook.setHookName(emailHook.getName());
            innerEmailHook.setDescription(emailHook.getDescription());
            innerEmailHook.setExternalLink(emailHook.getExternalLink());
            innerEmailHook.setHookParameter(new EmailHookParameter()
                .setToList(emailHook.getEmailsToAlert()));
            return innerEmailHook;
        } else if (hook instanceof WebHook) {
            WebHook webHook = (WebHook) hook;
            if (CoreUtils.isNullOrEmpty(webHook.getName())) {
                throw logger.logExceptionAsError(new IllegalArgumentException("The hook.name is required."));
            }
            if (CoreUtils.isNullOrEmpty(webHook.getEndpoint())) {
                throw logger.logExceptionAsError(new IllegalArgumentException("The hook.endpoint is required."));
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
                    "The hook type %s not supported", hook.getClass().getCanonicalName())));
        }
    }

    public static HookInfoPatch toInnerForUpdate(ClientLogger logger, Hook hook) {
        if (hook instanceof EmailHook) {
            EmailHook emailHook = (EmailHook) hook;
            EmailHookInfoPatch innerEmailHook = new EmailHookInfoPatch();
            innerEmailHook.setHookName(emailHook.getName());
            innerEmailHook.setDescription(emailHook.getDescription());
            innerEmailHook.setExternalLink(emailHook.getExternalLink());
            innerEmailHook.setHookParameter(new EmailHookParameter()
                .setToList(emailHook.getEmailsToAlert()));
            return innerEmailHook;
        } else if (hook instanceof WebHook) {
            WebHook webHook = (WebHook) hook;
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
                    "The hook type %s not supported", hook.getClass().getCanonicalName())));
        }
    }

    public static Hook fromInner(ClientLogger logger, HookInfo innerHook) {
        if (innerHook instanceof EmailHookInfo) {
            EmailHookInfo innerEmailHook = (EmailHookInfo) innerHook;
            EmailHook emailHook = new EmailHook(innerEmailHook.getHookName(),
                innerEmailHook.getHookParameter().getToList());

            emailHook.setDescription(innerEmailHook.getDescription());
            emailHook.setExternalLink(innerEmailHook.getExternalLink());

            PrivateFieldAccessHelper.set((Hook) emailHook,
                "id",
                innerEmailHook.getHookId().toString());

            List<String> adminList = innerEmailHook.getAdmins();
            if (adminList == null) {
                adminList = new ArrayList<>();
            }
            PrivateFieldAccessHelper.set((Hook) emailHook,
                "admins",
                Collections.unmodifiableList(adminList));

            return emailHook;
        } else if (innerHook instanceof WebhookHookInfo) {
            WebhookHookInfo innerWebHook = (WebhookHookInfo) innerHook;
            WebHook webHook = new WebHook(innerWebHook.getHookName(),
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

            PrivateFieldAccessHelper.set(webHook,
                "id",
                innerWebHook.getHookId().toString());

            List<String> adminList = innerWebHook.getAdmins();
            if (adminList == null) {
                adminList = new ArrayList<>();
            }
            PrivateFieldAccessHelper.set(webHook,
                "admins",
                Collections.unmodifiableList(adminList));

            return webHook;
        } else {
            throw logger.logExceptionAsError(Exceptions.propagate(new RuntimeException(String.format(
                "The hook type %s not supported", innerHook.getClass().getCanonicalName()))));
        }
    }

    public static PagedResponse<Hook> fromInnerPagedResponse(ClientLogger logger,
                                                             PagedResponse<HookInfo> innerResponse) {
        List<Hook> hookList;
        final List<HookInfo> innerHookList = innerResponse.getValue();
        if (innerHookList == null || innerHookList.isEmpty()) {
            hookList = new ArrayList<>();
        } else {
            hookList = innerHookList
                .stream()
                .map(innerAnomaly -> fromInner(logger, innerAnomaly))
                .collect(Collectors.toList());
        }

        final IterableStream<Hook> pageElements
            = new IterableStream<>(hookList);

        return new PagedResponseBase<Void, Hook>(innerResponse.getRequest(),
            innerResponse.getStatusCode(),
            innerResponse.getHeaders(),
            new HookPage(pageElements, innerResponse.getContinuationToken()),
            null);
    }

    private static final class HookPage implements Page<Hook> {
        private final IterableStream<Hook> elements;
        private final String continuationTToken;

        private HookPage(IterableStream<Hook> elements, String continuationTToken) {
            this.elements = elements;
            this.continuationTToken = continuationTToken;
        }

        @Override
        public IterableStream<Hook> getElements() {
            return this.elements;
        }

        @Override
        public String getContinuationToken() {
            return this.continuationTToken;
        }
    }
}
