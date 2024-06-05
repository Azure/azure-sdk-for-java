// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.revapi.transforms;

import org.revapi.Difference;
import org.revapi.Element;
import org.revapi.TransformationResult;
import org.revapi.base.BaseDifferenceTransform;
import org.revapi.java.spi.JavaTypeElement;

import javax.annotation.Nullable;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import java.util.regex.Pattern;

public final class AzureSdkAllowedExternalApis<E extends Element<E>> extends BaseDifferenceTransform<E> {
    private static final Pattern DIFFERENCE_CODE_PATTERN = Pattern.compile("java.class.externalClassExposedInAPI",
        Pattern.LITERAL);

    @Override
    public Pattern[] getDifferenceCodePatterns() {
        return new Pattern[] { DIFFERENCE_CODE_PATTERN };
    }

    @Override
    public TransformationResult tryTransform(@Nullable E oldElement, @Nullable E newElement, Difference difference) {
        if (newElement == null) {
            // Missing element to compare.
            return TransformationResult.keep();
        }

        if (!(newElement instanceof JavaTypeElement)) {
            // Unknown element type.
            return TransformationResult.keep();
        }

        TypeElement outermostElement = findOuterMostClass(((JavaTypeElement) newElement).getDeclaringElement());
        ExternalApiStatus shouldBeIgnored = shouldExternalApiBeIgnored(outermostElement);

        return shouldBeIgnored.ignore ? TransformationResult.discard() : TransformationResult.keep();
    }

    @Override
    public String getExtensionId() {
        return "azure-sdk-allowed-external-apis";
    }

    private static TypeElement findOuterMostClass(javax.lang.model.element.Element el) {
        while (el != null && !(el instanceof TypeElement)) {
            el = el.getEnclosingElement();
        }

        if (el == null) {
            return null;
        }

        return ((TypeElement) el).getNestingKind() == NestingKind.TOP_LEVEL
            ? (TypeElement) el
            : findOuterMostClass(el.getEnclosingElement());
    }



    private static ExternalApiStatus shouldExternalApiBeIgnored(TypeElement element) {
        if (element == null) {
            return ExternalApiStatus.KEEP;
        }

        String className = element.getQualifiedName().toString();

        if (className.startsWith("com.")) {
            if ("azure.".regionMatches(0, className, 4, 6)) {
                if ("communication.common.".regionMatches(0, className, 10, 21)
                    || "core.".regionMatches(0, className, 10, 5)
                    || "cosmos.".regionMatches(0, className, 10, 7)
                    || "data.schemaregistry.".regionMatches(0, className, 10, 20)
                    || "data.appconfiguration.".regionMatches(0, className, 10, 22)
                    || "identity.".regionMatches(0, className, 10, 9)
                    || "json.".regionMatches(0, className, 10, 5)
                    || "messaging.eventgrid.".regionMatches(0, className, 10, 20)
                    || "messaging.eventhubs.".regionMatches(0, className, 10, 20)
                    || "messaging.servicebus.".regionMatches(0, className, 10, 21)
                    || "resourcemanager.".regionMatches(0, className, 10, 16)
                    || "security.keyvault.".regionMatches(0, className, 10, 18)
                    || "spring.cloud.appconfiguration.config.".regionMatches(0, className, 10, 20)
                    || "spring.cloud.feature.".regionMatches(0, className, 10, 21)
                    || "storage.".regionMatches(0, className, 10, 8)
                    || "xml.".regionMatches(0, className, 10, 4)) {
                    return ExternalApiStatus.SDK_CLASSES;
                } else if ("perf.test.core.".regionMatches(0, className, 10, 15)) {
                    return ExternalApiStatus.PERF_TEST;
                } else if (className.length() == 53
                    && className.endsWith("spring.cloud.config.AppConfigurationRefresh")) {
                    return ExternalApiStatus.APP_CONFIGURATION_REFRESH;
                }
            } else if ("mysql.cj.".regionMatches(0, className, 4, 9)) {
                return ExternalApiStatus.MYSQL_CJ;
            }
        } else if (className.startsWith("io.")) {
            if ("cloudevents.".regionMatches(0, className, 3, 12)) {
                return ExternalApiStatus.CLOUD_EVENTS;
            } else if ("opentelemetry.".regionMatches(0, className, 3, 14)) {
                return ExternalApiStatus.OPEN_TELEMETRY;
            } else if ("clientcore.".regionMatches(0, className, 3, 10)) {
                return ExternalApiStatus.SDK_CLASSES;
            }
        } else if (className.startsWith("org.")) {
            if ("json.".regionMatches(0, className, 4, 5)) {
                return ExternalApiStatus.ORG_JSON;
            } else if ("postgresql.".regionMatches(0, className, 4, 11)) {
                return ExternalApiStatus.POSTGRESQL;
            } else if ("reactivestreams.".regionMatches(0, className, 4, 16)) {
                return ExternalApiStatus.REACTIVE_STREAMS;
            } else if (className.length() == 37 && className.endsWith("springframework.util.ErrorHandler")) {
                return ExternalApiStatus.SPRING_ERROR_HANDLER;
            }
        } else if (className.startsWith("redis.clients.jedis")) {
            return ExternalApiStatus.JEDIS;
        }

        return ExternalApiStatus.KEEP;
    }

    private static final class ExternalApiStatus {
        private static final ExternalApiStatus KEEP = new ExternalApiStatus(false);
        private static final ExternalApiStatus MYSQL_CJ = new ExternalApiStatus("Mysql driver classes are allowed to "
            + "be exposed by dependencies using them.");
        private static final ExternalApiStatus SDK_CLASSES = new ExternalApiStatus("SDK classes are allowed to be "
            + "exposed by dependencies using them.");
        private static final ExternalApiStatus PERF_TEST = new ExternalApiStatus("perf-test classes are allowed to be "
            + "exposed.");
        private static final ExternalApiStatus APP_CONFIGURATION_REFRESH = new ExternalApiStatus("This isn't an "
            + "external class");
        private static final ExternalApiStatus CLOUD_EVENTS = new ExternalApiStatus("Azure Event Grid cloud native "
            + "cloud event is allowed to use CloudEvents types in public APIs as it implements interfaces defined by "
            + "CloudEvents");
        private static final ExternalApiStatus OPEN_TELEMETRY = new ExternalApiStatus("Azure Monitor Exporter is "
            + "allowed to use OpenTelemetry types in public APIs as it implements interfaces defined by OpenTelemetry");
        private static final ExternalApiStatus ORG_JSON = new ExternalApiStatus("To support the EventHubs "
            + "JedisRedisCheckpointStore constructor");
        private static final ExternalApiStatus POSTGRESQL = new ExternalApiStatus("Postgresql driver classes are "
            + "allowed to be exposed by dependencies using them.");
        private static final ExternalApiStatus SPRING_ERROR_HANDLER = new ExternalApiStatus("Azure Spring Cloud "
            + "Messaging need the Spring's public interface for error handler registration, it is a common class for "
            + "users to handle runtime errors.");
        private static final ExternalApiStatus REACTIVE_STREAMS = new ExternalApiStatus("Reactive streams types are "
            + "allowed to be exposed.");

        private static final ExternalApiStatus JEDIS = new ExternalApiStatus("To support the EventHubs "
            + "JedisRedisCheckpointStore constructor");
        private final boolean ignore;
        private final String justification;

        ExternalApiStatus(boolean ignore) {
            this.ignore = ignore;
            this.justification = null;
        }

        ExternalApiStatus(String justification) {
            this.ignore = true;
            this.justification = justification;
        }
    }
}
