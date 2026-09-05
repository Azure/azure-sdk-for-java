// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.extensions;

import com.azure.core.util.ServiceVersion;
import com.azure.storage.common.test.shared.TestEnvironment;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

/**
 * Extension to mark tests that should only be run with a specific service version.
 */
public class RequiredServiceVersionExtension implements ExecutionCondition {
    private static final Map<Class<? extends Enum<? extends ServiceVersion>>, ServiceVersion[]> ALL_SERVICE_VERSIONS
        = new ConcurrentHashMap<>();
    private static final Map<Class<? extends Enum<? extends ServiceVersion>>, ServiceVersion> LATEST_SERVICE_VERSIONS
        = new ConcurrentHashMap<>();

    static boolean shouldSkip(Class<? extends Enum<? extends ServiceVersion>> targetEnumClass,
        String minServiceVersion, String environmentServiceVersion) {
        ServiceVersion[] serviceVersions = getServiceVersions(targetEnumClass);
        if (environmentServiceVersion == null) {
            // Fall back to "latest" service version if environment variable is not set.
            environmentServiceVersion = getLatestServiceVersion(targetEnumClass).getVersion();
        }

        int minOrdinal = getOrdinal(serviceVersions, minServiceVersion, "minimum");
        int environmentOrdinal = getOrdinal(serviceVersions, environmentServiceVersion, "configured");

        return environmentOrdinal < minOrdinal;
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        // Check for the RequiredServiceVersion annotation on the test method.
        // If it exists, check that the service version configured supports the minimum version required.
        RequiredServiceVersion requiredServiceVersion = findAnnotation(context.getElement(),
            RequiredServiceVersion.class).orElse(null);

        if (requiredServiceVersion == null) {
            return ConditionEvaluationResult.enabled("No service version required");
        }

        if (shouldSkip(requiredServiceVersion.clazz(), requiredServiceVersion.min())) {
            return ConditionEvaluationResult.disabled("Test ignored to run with " + requiredServiceVersion.min()
                + " service version");
        } else {
            return ConditionEvaluationResult.enabled("Test enabled to run with " + requiredServiceVersion.min()
                + " service version");
        }
    }

    private static boolean shouldSkip(Class<? extends Enum<? extends ServiceVersion>> targetEnumClass,
        String minServiceVersion) {
        String environmentServiceVersion = TestEnvironment.getInstance().getServiceVersion();
        return shouldSkip(targetEnumClass, minServiceVersion, environmentServiceVersion);
    }

    private static ServiceVersion[] getServiceVersions(Class<? extends Enum<? extends ServiceVersion>> clazz) {
        return ALL_SERVICE_VERSIONS.computeIfAbsent(clazz, c -> (ServiceVersion[]) clazz.getEnumConstants());
    }

    private static ServiceVersion getLatestServiceVersion(Class<? extends Enum<? extends ServiceVersion>> clazz) {
        return LATEST_SERVICE_VERSIONS.computeIfAbsent(clazz, c -> {
            try {
                return (ServiceVersion) clazz.getDeclaredMethod("getLatest").invoke(null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static int getOrdinal(ServiceVersion[] serviceVersions, String target, String description) {
        for (int i = 0; i < serviceVersions.length; i++) {
            ServiceVersion serviceVersion = serviceVersions[i];
            if (Objects.equals(serviceVersion.getVersion(), target)
                || Objects.equals(((Enum<?>) serviceVersion).name(), target)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown " + description + " service version '" + target + "'.");
    }
}
