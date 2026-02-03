// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.revapi.transforms;

import com.fasterxml.jackson.databind.JsonNode;
import org.revapi.AnalysisContext;
import org.revapi.ArchiveAnalyzer;
import org.revapi.Element;
import org.revapi.FilterStartResult;
import org.revapi.TreeFilter;
import org.revapi.TreeFilterProvider;
import org.revapi.base.IndependentTreeFilter;
import org.revapi.java.spi.JavaTypeElement;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static io.clientcore.linting.extensions.revapi.transforms.RevApiUtils.createPrefixMatchersFromConfiguration;
import static io.clientcore.linting.extensions.revapi.transforms.RevApiUtils.findOuterMostClass;

/**
 * A {@link TreeFilterProvider} that filters out classes and packages that shouldn't have RevApi analysis performed.
 */
public final class ClassAndPackageTreeFilterProvider implements TreeFilterProvider {
    private static final String PATTERN_ERROR_MESSAGE = "Configuration 'ignoredPackagesPatterns' must be an array of "
        + "strings representing regular expressions to ignore.";

    /**
     * Creates a new instance of {@link ClassAndPackageTreeFilterProvider}.
     */
    public ClassAndPackageTreeFilterProvider() {
    }

    @Override
    public String getExtensionId() {
        return "class-and-package-tree-filter-provider";
    }

    @Override
    public Reader getJSONSchema() {
        return null;
    }

    // TreeFilterProviders don't need to have a configuration for enabled as
    private List<PrefixMatcher> ignoredClasses = Collections.emptyList();
    private List<PrefixMatcher> ignoredPackages = Collections.emptyList();
    private List<Pattern> ignoredPackagesPatterns = Collections.emptyList();

    @Override
    public void initialize(AnalysisContext analysisContext) {
        this.ignoredClasses = createPrefixMatchersFromConfiguration(analysisContext, "ignoredClasses");
        this.ignoredPackages = createPrefixMatchersFromConfiguration(analysisContext, "ignoredPackages");

        JsonNode patterns = analysisContext.getConfigurationNode().get("ignoredPackagesPatterns");
        if (patterns != null) {
            if (!patterns.isArray()) {
                throw new IllegalArgumentException(PATTERN_ERROR_MESSAGE);
            }

            List<Pattern> ignoredPackagesPatterns = new ArrayList<>();
            for (JsonNode pattern : patterns) {
                if (!pattern.isTextual()) {
                    throw new IllegalArgumentException(PATTERN_ERROR_MESSAGE);
                }

                ignoredPackagesPatterns.add(Pattern.compile(pattern.asText()));
            }

            this.ignoredPackagesPatterns = ignoredPackagesPatterns;
        }
    }

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
                boolean excludeClass
                    = outermostClass != null && excludeClass(outermostClass.getQualifiedName().toString());

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

    boolean excludeClass(String className) {
        for (PrefixMatcher matcher : ignoredClasses) {
            if (matcher.test(className)) {
                return true;
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

    boolean excludePackage(String packageName) {
        for (PrefixMatcher matcher : ignoredPackages) {
            if (matcher.test(packageName)) {
                return true;
            }
        }

        for (Pattern pattern : ignoredPackagesPatterns) {
            if (pattern.matcher(packageName).matches()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void close() {
    }
}
