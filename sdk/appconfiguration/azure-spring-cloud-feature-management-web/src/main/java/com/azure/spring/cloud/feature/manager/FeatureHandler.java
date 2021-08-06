// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.azure.spring.cloud.feature.manager.FeatureManager;

import reactor.core.publisher.Mono;

/**
 * Intercepter for Requests to check if they should be run.
 */
@Component
public class FeatureHandler extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureHandler.class);

    private FeatureManager featureManager;

    private FeatureManagerSnapshot featureManagerSnapshot;

    private IDisabledFeaturesHandler disabledFeaturesHandler;

    public FeatureHandler(FeatureManager featureManager, FeatureManagerSnapshot featureManagerSnapshot,
        IDisabledFeaturesHandler disabledFeaturesHandler) {
        this.featureManager = featureManager;
        this.featureManagerSnapshot = featureManagerSnapshot;
        this.disabledFeaturesHandler = disabledFeaturesHandler;
    }

    /**
     * Checks if the endpoint being called has the @FeatureOn annotation. Checks if the feature is on. Can redirect if
     * feature is off, or can return the disabled feature handler.
     *
     * @return true if the @FeatureOn annotation is on or the feature is enabled. Else, it returns false, or is
     * redirected.
     */
    @Override
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
