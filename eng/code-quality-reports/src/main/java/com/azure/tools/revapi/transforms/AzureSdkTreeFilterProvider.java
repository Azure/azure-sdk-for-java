// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.revapi.transforms;

import org.revapi.AnalysisContext;
import org.revapi.ArchiveAnalyzer;
import org.revapi.Element;
import org.revapi.FilterStartResult;
import org.revapi.Ternary;
import org.revapi.TreeFilter;
import org.revapi.TreeFilterProvider;
import org.revapi.base.IndependentTreeFilter;
import org.revapi.java.spi.JavaTypeElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.Reader;
import java.util.Optional;

public final class AzureSdkTreeFilterProvider implements TreeFilterProvider {
    @Override
    public <E extends Element<E>> Optional<TreeFilter<E>> filterFor(ArchiveAnalyzer<E> archiveAnalyzer) {
        if (!"revapi.java".equals(archiveAnalyzer.getApiAnalyzer().getExtensionId())) {
            return Optional.empty();
        }

        return Optional.of(new IndependentTreeFilter<E>() {
            @Override
            protected FilterStartResult doStart(E element) {
                if (!(element instanceof JavaTypeElement)) {
                    // Unknown element type.
                    return FilterStartResult.defaultResult();
                }

                TypeElement outermostClass = findOuterMostClass(((JavaTypeElement) element).getDeclaringElement());
                boolean excludeClass = excludeClass(outermostClass);

                if (excludeClass) {
                    // Class is being excluded, no need to inspect package.
                    return FilterStartResult.direct(Ternary.FALSE, Ternary.FALSE);
                }

                PackageElement packageElement = findPackage((JavaTypeElement) element);

                if (packageElement == null) {
                    // No Java package.
                    return FilterStartResult.defaultResult();
                }

                String packageName = packageElement.getQualifiedName().toString();
                boolean excludePackage = excludePackage(packageName);

                return FilterStartResult.direct(Ternary.fromBoolean(!excludePackage), Ternary.fromBoolean(!excludePackage));
            }
        });
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

    private static boolean excludeClass(javax.lang.model.element.TypeElement el) {
        if (el == null || el.getKind() != ElementKind.CLASS) {
            return false;
        }

        String className = el.getQualifiedName().toString();

        if (className.startsWith("com.azure.")) {
            if ("core.".regionMatches(0, className, 10, 5)) {
                // Exclude com.azure.core.util.Configuration
                return className.length() == 33
                    && className.endsWith("util.Configuration");
            } else if ("cosmos.".regionMatches(0, className, 10, 6)) {
                // Exclude
                //
                // - com.azure.cosmos.BridgeInternal
                // - com.azure.cosmos.CosmosBridgeInternal
                // - com.azure.cosmos.models.ModelBridgeInternal
                // - com.azure.cosmos.util.UtilBridgeInternal
                return (className.length() == 31 && className.endsWith("BridgeInternal"))
                    || (className.length() == 37 && className.endsWith("CosmosBridgeInternal"))
                    || (className.length() == 43 && className.endsWith("models.ModelBridgeInternal"))
                    || (className.length() == 40 && className.endsWith("util.UtilBridgeInternal"));
            } else if ("spring.cloud.config.".regionMatches(0, className, 10, 20)) {
                // Exclude
                //
                // - com.azure.spring.cloud.config.AppConfigurationBootstrapConfiguration
                // - com.azure.spring.cloud.config.AppConfigurationRefresh
                // - com.azure.spring.cloud.config.State
                // - com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties
                // - com.azure.spring.cloud.config.web.AppConfigurationEndpoint
                // - com.azure.spring.cloud.config.web.pushrefresh.AppConfigurationRefreshEvent
                return (className.length() == 68 && className.endsWith("AppConfigurationBootstrapConfiguration"))
                    || (className.length() == 53 && className.endsWith("AppConfigurationRefresh"))
                    || (className.length() == 35 && className.endsWith("State"))
                    || (className.length() == 75 && className.endsWith("properties.AppConfigurationProviderProperties"))
                    || (className.length() == 58 && className.endsWith("web.AppConfigurationEndpoint"))
                    || (className.length() == 74 && className.endsWith("web.pushrefresh.AppConfigurationRefreshEvent"));
            }
        }

        return false;
    }

    private static PackageElement findPackage(JavaTypeElement element) {
        javax.lang.model.element.Element el = element.getDeclaringElement();
        while (el != null && !(el instanceof PackageElement)) {
            el = el.getEnclosingElement();
        }

        return (PackageElement) el;
    }

    private static boolean excludePackage(String packageName) {
        if (packageName.startsWith("com.")) {
            return matchComPackage(packageName);
        }

        if (packageName.startsWith("io")) {
            // Exclude io.micrometer*, io.netty*, and io.vertx*
            return "micrometer".regionMatches(0, packageName, 2, 10)
                || "netty".regionMatches(0, packageName, 2, 5)
                || "vertx".regionMatches(0, packageName, 2, 5);
        }

        if (packageName.startsWith("javax")) {
            // Exclude javax.jms* and javax.servlet*
            return "jms".regionMatches(0, packageName, 5, 3)
                || "servlet".regionMatches(0, packageName, 5, 7);
        }

        if (packageName.startsWith("kotlin")) {
            // Exclude kotlin*
            return true;
        }

        if (packageName.startsWith("okhttp3")) {
            // Exclude okhttp3*
            return true;
        }

        if (packageName.startsWith("okio")) {
            // Exclude okio*
            return true;
        }

        if (packageName.startsWith("org.")) {
            if ("apache.".regionMatches(0, packageName, 4, 7)) {
                // Exclude org.apache.avro*, org.apache.commons*, and org.apache.qpid*
                return "avro".regionMatches(0, packageName, 11, 4)
                    || "commons".regionMatches(0, packageName, 11, 7)
                    || "qpid".regionMatches(0, packageName, 11, 4);
            } else if ("junit".regionMatches(0, packageName, 4, 5)) {
                // Exclude org.junit*
                return true;
            } else if ("slf4j".regionMatches(0, packageName, 4, 5)) {
                // Exclude org.slf4j*
                return true;
            } else if ("springframework".regionMatches(0, packageName, 4, 15)) {
                // Exclude org.springframework*
                return true;
            } else {
                return false;
            }
        }

        if (packageName.startsWith("reactor.")) {
            // Exclude reactor.core*, reactor.netty*, and reactor.util*
            return "core".regionMatches(0, packageName, 8, 4)
                || "netty".regionMatches(0, packageName, 8, 5)
                || "util".regionMatches(0, packageName, 8, 4);
        }

        return false;
    }

    private static boolean matchComPackage(String packageName) {
        if ("azure.".regionMatches(0, packageName, 4, 6)) {
            if ("data.cosmos".regionMatches(0, packageName, 10, 11)) {
                // Exclude com.azure.data.cosmos*
                return true;
            } else if (packageName.indexOf("implementation", 4) != -1
                || packageName.indexOf("samples", 4) != -1) {
                // Exclude com.azure*.implementation* and com.azure*.samples*
                return true;
            } else if ("resourcemanager".regionMatches(0, packageName, 10, 15)
                && packageName.indexOf("fluent", 15) != -1) {
                // Exclude com.azure.resourcemanager.*fluent*
                return true;
            } else {
                return false;
            }
        } else if ("fasterxml.jackson".regionMatches(0, packageName, 4, 17)) {
            // Exclude com.fasterxml.jackson*
            return true;
        } else if ("google.gson".regionMatches(0, packageName, 4, 11)) {
            // Exclude com.google.gson*
            return true;
        } else if ("microsoft.azure".regionMatches(0, packageName, 4, 15)) {
            // Exclude com.microsoft.azure*
            return true;
        } else if ("nimbusds".regionMatches(0, packageName, 4, 8)) {
            // Exclude com.nimbusds*
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void close() {
    }

    @Override
    public String getExtensionId() {
        return "azure-sdk-tree-provider";
    }

    @Nullable
    @Override
    public Reader getJSONSchema() {
        return null;
    }

    @Override
    public void initialize(@Nonnull AnalysisContext analysisContext) {
    }
}
