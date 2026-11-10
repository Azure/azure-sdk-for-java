// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.azure;

import com.azure.tools.apiview.processor.analysers.models.Constants;
import com.azure.tools.apiview.processor.analysers.util.ASTUtils;
import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.azure.tools.apiview.processor.model.DiagnosticKind;
import com.azure.tools.apiview.processor.model.ReviewLine;
import com.azure.tools.apiview.processor.model.maven.Pom;
import com.github.javaparser.ast.CompilationUnit;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Diagnostic rule that validates the Maven package name and description match the convention specified in the
 * guidelines.
 */
public final class MavenPackageAndDescriptionDiagnosticRule implements DiagnosticRule {
    /*
     * Default {@link Pattern} for the Maven package name.
     */
    static final Pattern DEFAULT_MAVEN_NAME = Pattern.compile("Microsoft Azure client library for .*");

    /*
     * Default {@link Pattern} for the Maven package description.
     */
    static final Pattern DEFAULT_MAVEN_DESCRIPTION =
        Pattern.compile("This package contains the Microsoft Azure .* client library");

    /*
     * Default guideline link for the Maven package name.
     */
    static final String DEFAULT_MAVEN_NAME_GUIDELINE_LINK =
        "https://azure.github.io/azure-sdk/java_introduction.html#java-maven-name";

    /*
     * Default guideline link for the Maven package description.
     */
    static final String DEFAULT_MAVEN_DESCRIPTION_GUIDELINE_LINK =
        "https://azure.github.io/azure-sdk/java_introduction.html#java-maven-description";

    private final Pattern mavenNamePattern;
    private final Pattern mavenDescriptionPattern;
    private final String mavenNameGuidelineLink;
    private final String mavenDescriptionGuidelineLink;

    /**
     * Creates an instance of {@link MavenPackageAndDescriptionDiagnosticRule} using default values.
     */
    public MavenPackageAndDescriptionDiagnosticRule() {
        this(DEFAULT_MAVEN_NAME, DEFAULT_MAVEN_DESCRIPTION, DEFAULT_MAVEN_NAME_GUIDELINE_LINK,
            DEFAULT_MAVEN_DESCRIPTION_GUIDELINE_LINK);
    }

    /**
     * Creates an instance of {@link MavenPackageAndDescriptionDiagnosticRule}.
     *
     * @param mavenNamePattern {@link Pattern} that the Maven package name must follow.
     * @param mavenDescriptionPattern {@link Pattern} that the Maven package description must follow.
     * @param mavenNameGuidelineLink Guideline link that explains the Maven package name rules.
     * @param mavenDescriptionGuidelineLink Guideline link that explains the Maven package description rules.
     */
    public MavenPackageAndDescriptionDiagnosticRule(Pattern mavenNamePattern, Pattern mavenDescriptionPattern,
        String mavenNameGuidelineLink, String mavenDescriptionGuidelineLink) {
        this.mavenNamePattern = mavenNamePattern;
        this.mavenDescriptionPattern = mavenDescriptionPattern;
        this.mavenNameGuidelineLink = mavenNameGuidelineLink;
        this.mavenDescriptionGuidelineLink = mavenDescriptionGuidelineLink;
    }

    @Override
    public void scanIndividual(CompilationUnit cu, APIListing listing) {
        // no-op, package rule only needs to be run once at the end.
    }

    @Override
    public void scanFinal(APIListing listing) {
        Pom pom = listing.getMavenPom();

        // it's possible there was no real maven POM in the uploaded file, in which case we should warn the user
        // of this so that they can investigate if their build is configured correctly to include this file.
        if (!pom.isPomFileReal()) {
            // TODO
        } else {
            // Maven name
            final String mavenName = pom.getName();
            if (mavenName == null || !mavenNamePattern.matcher(pom.getName()).matches()) {
                final Optional<ReviewLine> optionalLine = ASTUtils.findReviewLine(listing, line -> line.hasProperty(Constants.PROPERTY_MAVEN_NAME));
                final String lineId = optionalLine.map(ReviewLine::getLineId).orElse(null);
                listing.addDiagnostic(new Diagnostic(DiagnosticKind.WARNING, lineId,
                    "Maven library name should follow the pattern '" + mavenNamePattern.pattern() + "'.",
                    mavenNameGuidelineLink));
            }


            // Maven description
            final String mavenDescription = pom.getDescription();
            if (mavenDescription == null || !mavenDescriptionPattern.matcher(pom.getDescription()).matches()) {
                final Optional<ReviewLine> optionalLine = ASTUtils.findReviewLine(listing, line -> line.hasProperty(Constants.PROPERTY_MAVEN_DESCRIPTION));
                final String lineId = optionalLine.map(ReviewLine::getLineId).orElse(null);
                listing.addDiagnostic(new Diagnostic(DiagnosticKind.WARNING, lineId,
                        "Maven library description should follow the pattern '" + mavenDescriptionPattern.pattern() + "'.",
                        mavenDescriptionGuidelineLink));
            }
        }
    }
}
