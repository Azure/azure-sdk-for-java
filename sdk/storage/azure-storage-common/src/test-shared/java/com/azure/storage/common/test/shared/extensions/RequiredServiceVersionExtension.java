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

    static ConditionEvaluationResult evaluateServiceVersion(
        Class<? extends Enum<? extends ServiceVersion>> targetEnumClass, String minServiceVersion,
        String configuredServiceVersion) {
        ServiceVersion[] serviceVersions = getServiceVersions(targetEnumClass);
        int minOrdinal = getRequiredServiceVersionOrdinal(serviceVersions, minServiceVersion);

        if (configuredServiceVersion == null) {
            // Fall back to "latest" service version if environment variable is not set.
            configuredServiceVersion = getLatestServiceVersion(targetEnumClass).getVersion();
        }

        int configuredOrdinal = getConfiguredServiceVersionOrdinal(serviceVersions, configuredServiceVersion,
            targetEnumClass);

        return configuredOrdinal < minOrdinal
            ? ConditionEvaluationResult.disabled(
                String.format("Test requires minimum service version '%s'.", minServiceVersion))
            : ConditionEvaluationResult.enabled(
                String.format("Configured service version meets minimum version '%s'.", minServiceVersion));
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

        String configuredServiceVersion = TestEnvironment.getInstance().getServiceVersion();
        return evaluateServiceVersion(requiredServiceVersion.clazz(), requiredServiceVersion.min(),
            configuredServiceVersion);
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

    /*
     * Resolves the annotation's required minimum version. An unknown minimum indicates an invalid annotation and must
     * fail the test rather than skip it.
     */
    private static int getRequiredServiceVersionOrdinal(ServiceVersion[] serviceVersions, String target) {
        int ordinal = findServiceVersionOrdinal(serviceVersions, target);
        if (ordinal == -1) {
            throw new IllegalArgumentException(
                String.format("Minimum service version '%s' is not supported.", target));
        }
        return ordinal;
    }

    /*
     * Resolves the configured service version. An unknown value indicates invalid test configuration and must fail the
     * test rather than silently reduce coverage.
     */
    private static int getConfiguredServiceVersionOrdinal(ServiceVersion[] serviceVersions, String target,
        Class<? extends Enum<? extends ServiceVersion>> targetEnumClass) {
        int ordinal = findServiceVersionOrdinal(serviceVersions, target);
        if (ordinal == -1) {
            throw new IllegalArgumentException(String.format(
                "AZURE_LIVE_TEST_SERVICE_VERSION '%s' is not supported by %s.", target,
                targetEnumClass.getSimpleName()));
        }
        return ordinal;
    }

    /*
     * Resolves either a service wire value or enum name, returning -1 when the version is absent.
     */
    private static int findServiceVersionOrdinal(ServiceVersion[] serviceVersions, String target) {
        for (int i = 0; i < serviceVersions.length; i++) {
            ServiceVersion serviceVersion = serviceVersions[i];
            Enum<?> serviceVersionEnum = (Enum<?>) serviceVersion;

            if (target.equals(serviceVersion.getVersion()) || target.equals(serviceVersionEnum.name())) {
                return i;
            }
        }
        return -1;
    }
}
