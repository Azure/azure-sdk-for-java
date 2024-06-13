// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.revapi.transforms;

import org.revapi.AnalysisContext;
import org.revapi.ArchiveAnalyzer;
import org.revapi.Element;
import org.revapi.FilterStartResult;
import org.revapi.TreeFilter;
import org.revapi.TreeFilterProvider;
import org.revapi.base.IndependentTreeFilter;
import org.revapi.java.spi.JavaTypeElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

                // No guarantee there is an outermost class, the enclosing type could be an interface or enum.
                boolean excludeClass = outermostClass != null
                    && excludeClass(outermostClass.getQualifiedName().toString());

                if (excludeClass) {
                    // Class is being excluded, no need to inspect package.
                    return FilterStartResult.doesntMatch();
                }

                PackageElement packageElement = findPackage((JavaTypeElement) element);

                if (packageElement == null) {
                    // No Java package.
                    return FilterStartResult.defaultResult();
                }

                String packageName = packageElement.getQualifiedName().toString();
                boolean excludePackage = excludePackage(packageName);

                return excludePackage ? FilterStartResult.doesntMatch() : FilterStartResult.matchAndDescend();
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

    static boolean excludeClass(String className) {
        if (!className.startsWith("com.azure.")) {
            return false;
        }

        if ("core.".regionMatches(0, className, 10, 5)) {
            // Exclude com.azure.core.util.Configuration
            return className.length() == 33 && className.endsWith("util.Configuration");
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
            // - com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties
            // - com.azure.spring.cloud.config.web.AppConfigurationEndpoint
            // - com.azure.spring.cloud.config.web.pushrefresh.AppConfigurationRefreshEvent
            return (className.length() == 68 && className.endsWith("AppConfigurationBootstrapConfiguration"))
                || (className.length() == 53 && className.endsWith("AppConfigurationRefresh"))
                || (className.length() == 75 && className.endsWith("properties.AppConfigurationProviderProperties"))
                || (className.length() == 58 && className.endsWith("web.AppConfigurationEndpoint"))
                || (className.length() == 74 && className.endsWith("web.pushrefresh.AppConfigurationRefreshEvent"));
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

    static boolean excludePackage(String packageName) {
        if (packageName.startsWith("com.")) {
            if ("azure.".regionMatches(0, packageName, 4, 6)) {
                if ("data.cosmos".regionMatches(0, packageName, 10, 11)) {
                    // Exclude com.azure.data.cosmos*
                    return true;
                } else if (packageName.indexOf("implementation", 10) != -1
                    || packageName.indexOf("samples", 10) != -1) {
                    // Exclude com.azure*.implementation*, com.azure.json*, com.azure*.samples*, and com.azure.xml*
                    return true;
                } else if ("resourcemanager".regionMatches(0, packageName, 10, 15)) {
                    // Exclude com.azure.resourcemanager*.fluent.* but don't match fluentcore or confluent
                    int fluentIndex = packageName.indexOf("fluent", 25);
                    return fluentIndex != -1 && (fluentIndex + 6 == packageName.length()
                        || (packageName.charAt(fluentIndex - 1) == '.' && packageName.charAt(fluentIndex + 6) == '.'));
                } else {
                    return false;
                }
            } else {
                // Exclude com.fasterxml.jackson*, com.google.gson*, com.microsoft.azure*, and com.nimbusds*
                return "fasterxml.jackson".regionMatches(0, packageName, 4, 17)
                    || "google.gson".regionMatches(0, packageName, 4, 11)
                    || "microsoft.azure".regionMatches(0, packageName, 4, 15)
                    || "nimbusds".regionMatches(0, packageName, 4, 8);
            }
        }

        if (packageName.startsWith("io.")) {
            // Exclude io.micrometer*, io.netty*, and io.vertx*
            return "micrometer".regionMatches(0, packageName, 3, 10)
                || "netty".regionMatches(0, packageName, 3, 5)
                || "vertx".regionMatches(0, packageName, 3, 5);
        }

        if (packageName.startsWith("javax.")) {
            // Exclude javax.jms* and javax.servlet*
            return "jms".regionMatches(0, packageName, 6, 3)
                || "servlet".regionMatches(0, packageName, 6, 7);
        }

        if (packageName.startsWith("kotlin")
            || packageName.startsWith("okhttp3")
            || packageName.startsWith("okio")) {
            // Exclude kotlin*, okhttp3*, and okio*
            return true;
        }

        if (packageName.startsWith("org.")) {
            if ("apache.".regionMatches(0, packageName, 4, 7)) {
                // Exclude org.apache.avro*, org.apache.commons*, and org.apache.qpid*
                return "avro".regionMatches(0, packageName, 11, 4)
                    || "commons".regionMatches(0, packageName, 11, 7)
                    || "qpid".regionMatches(0, packageName, 11, 4);
            } else {
                // Exclude org.junit*, org.slf4j*, and org.springframework*
                return "junit".regionMatches(0, packageName, 4, 5)
                    || "reactivestreams".regionMatches(0, packageName, 4, 15)
                    || "slf4j".regionMatches(0, packageName, 4, 5)
                    || "springframework".regionMatches(0, packageName, 4, 15);
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
