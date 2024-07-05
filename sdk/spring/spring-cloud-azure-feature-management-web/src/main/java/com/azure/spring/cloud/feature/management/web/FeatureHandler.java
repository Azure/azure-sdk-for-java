// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.web;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.azure.spring.cloud.feature.management.FeatureManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import reactor.core.publisher.Mono;

/**
 * Interceptor for Requests to check if they should be run.
 */
public class FeatureHandler implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureHandler.class);

    private final FeatureManager featureManager;

    private final FeatureManagerSnapshot featureManagerSnapshot;

    private final DisabledFeaturesHandler disabledFeaturesHandler;

    /**
     * Interceptor for Requests to check if they should be run.
     * @param featureManager App Configuration Feature Manager
     * @param featureManagerSnapshot App Configuration Feature Manager snapshot version
     * @param disabledFeaturesHandler optional handler for dealing with disabled endpoints.
     */
    public FeatureHandler(FeatureManager featureManager, FeatureManagerSnapshot featureManagerSnapshot,
        DisabledFeaturesHandler disabledFeaturesHandler) {
        this.featureManager = featureManager;
        this.featureManagerSnapshot = featureManagerSnapshot;
        this.disabledFeaturesHandler = disabledFeaturesHandler;
    }

    /**
     * Checks if the endpoint being called has the @FeatureOn annotation. Checks if the feature is on. Can redirect if
     * feature is off, or can return the disabled feature handler.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler the handler (or {@link HandlerMethod}) that started asynchronous
     * @return true if the @FeatureOn annotation is on or the feature is enabled. Else, it returns false, or is
     * redirected.
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Method method = null;
        if (handler instanceof HandlerMethod) {
            method = ((HandlerMethod) handler).getMethod();
        }
        if (method != null) {
            FeatureGate featureOn = method.getAnnotation(FeatureGate.class);
            if (featureOn != null) {
                String feature = featureOn.feature();
                boolean snapshot = featureOn.snapshot();
                Mono<Boolean> enabled;

                if (!snapshot) {
                    enabled = featureManager.isEnabledAsync(feature);
                } else {
                    enabled = featureManagerSnapshot.isEnabledAsync(feature);
                }
                boolean isEnabled = false;
                try {
                    isEnabled = Optional.ofNullable(enabled)
                        .map(Mono::block)
                        .orElse(false);
                    if (!isEnabled && !featureOn.fallback().isEmpty()) {
                        response.sendRedirect(featureOn.fallback());
                        return false;
                    }
                } catch (IOException e) {
                    LOGGER.info("Unable to send redirect.");
                    ReflectionUtils.rethrowRuntimeException(e);
                }
                if (!isEnabled && disabledFeaturesHandler != null) {
                    disabledFeaturesHandler.handleDisabledFeatures(request, response);
                } else if (!isEnabled) {
                    try {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    } catch (IOException e) {
                        LOGGER.error("Error thrown while returning 404 on false feature.", e);
                        return false;
                    }
                }
                return isEnabled;
            }
        }
        return true;
    }
}
