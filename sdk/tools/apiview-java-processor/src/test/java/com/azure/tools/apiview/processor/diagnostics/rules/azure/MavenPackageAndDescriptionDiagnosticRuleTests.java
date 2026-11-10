// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.azure;

import com.azure.tools.apiview.processor.diagnostics.rules.azure.MavenPackageAndDescriptionDiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.maven.Pom;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.azure.tools.apiview.processor.diagnostics.rules.azure.MavenPackageAndDescriptionDiagnosticRule.DEFAULT_MAVEN_DESCRIPTION;
import static com.azure.tools.apiview.processor.diagnostics.rules.azure.MavenPackageAndDescriptionDiagnosticRule.DEFAULT_MAVEN_DESCRIPTION_GUIDELINE_LINK;
import static com.azure.tools.apiview.processor.diagnostics.rules.azure.MavenPackageAndDescriptionDiagnosticRule.DEFAULT_MAVEN_NAME;
import static com.azure.tools.apiview.processor.diagnostics.rules.azure.MavenPackageAndDescriptionDiagnosticRule.DEFAULT_MAVEN_NAME_GUIDELINE_LINK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MavenPackageAndDescriptionDiagnosticRuleTests {
    /**
     * Tests that a missing package description is flagged.
     */
    @Test
    public void missingPackageDescription() {
        MavenPackageAndDescriptionDiagnosticRule rule = new MavenPackageAndDescriptionDiagnosticRule();
        APIListing apiListing = new APIListing();
        Pom pom = mockRealPom("Microsoft Azure client library for ApiView", null);
        apiListing.setMavenPom(pom);

        rule.scanFinal(apiListing);

        assertEquals(1, apiListing.getDiagnostics().size());
        assertTrue(apiListing.getDiagnostics().get(0).getHelpLinkUri()
            .contains(DEFAULT_MAVEN_DESCRIPTION_GUIDELINE_LINK));
    }

    /**
     * Tests that an invalid package description is flagged.
     */
    @Test
    public void invalidPackageDescription() {
        MavenPackageAndDescriptionDiagnosticRule rule = new MavenPackageAndDescriptionDiagnosticRule();
        APIListing apiListing = new APIListing();
        Pom pom = mockRealPom("Microsoft Azure client library for ApiView", "A description that doesn't pass the rule");
        apiListing.setMavenPom(pom);

        rule.scanFinal(apiListing);

        assertEquals(1, apiListing.getDiagnostics().size());
        assertTrue(apiListing.getDiagnostics().get(0).getHelpLinkUri()
            .contains(DEFAULT_MAVEN_DESCRIPTION_GUIDELINE_LINK));
    }

    /**
     * Tests that a valid package description isn't flagged.
     */
    @Test
    public void validPackageDescription() {
        MavenPackageAndDescriptionDiagnosticRule rule = new MavenPackageAndDescriptionDiagnosticRule();
        APIListing apiListing = new APIListing();
        Pom pom = mockRealPom("Microsoft Azure client library for ApiView",
            "This package contains the Microsoft Azure ApiView client library");
        apiListing.setMavenPom(pom);

        rule.scanFinal(apiListing);

        assertEquals(0, apiListing.getDiagnostics().size());
    }

    /**
     * Tests that a custom package description validation can be used.
     */
    @Test
    public void validCustomPackageDescription() {
        MavenPackageAndDescriptionDiagnosticRule rule = new MavenPackageAndDescriptionDiagnosticRule(DEFAULT_MAVEN_NAME,
            Pattern.compile("A custom package description validation"), DEFAULT_MAVEN_NAME_GUIDELINE_LINK,
            DEFAULT_MAVEN_DESCRIPTION_GUIDELINE_LINK);
        APIListing apiListing = new APIListing();
        Pom pom = mockRealPom("Microsoft Azure client library for ApiView", "A custom package description validation");
        apiListing.setMavenPom(pom);

        rule.scanFinal(apiListing);

        assertEquals(0, apiListing.getDiagnostics().size());
    }

    /**
     * Tests that a custom link can be used for the package description guideline.
     */
    @Test
    public void customPackageDescriptionGuidelineLink() {
        MavenPackageAndDescriptionDiagnosticRule rule = new MavenPackageAndDescriptionDiagnosticRule(DEFAULT_MAVEN_NAME,
            DEFAULT_MAVEN_DESCRIPTION, DEFAULT_MAVEN_NAME_GUIDELINE_LINK, "custom package description guideline link");
        APIListing apiListing = new APIListing();
        Pom pom = mockRealPom("Microsoft Azure client library for ApiView", "A description that doesn't pass the rule");
        apiListing.setMavenPom(pom);

        rule.scanFinal(apiListing);

        assertEquals(1, apiListing.getDiagnostics().size());
        assertTrue(apiListing.getDiagnostics().get(0).getHelpLinkUri()
            .contains("custom package description guideline link"));
    }

    /**
     * Tests that a missing package name is flagged.
     */
    @Test
    public void missingPackageName() {
        MavenPackageAndDescriptionDiagnosticRule rule = new MavenPackageAndDescriptionDiagnosticRule();
        APIListing apiListing = new APIListing();
        Pom pom = mockRealPom(null, "This package contains the Microsoft Azure ApiView client library");
        apiListing.setMavenPom(pom);

        rule.scanFinal(apiListing);

        assertEquals(1, apiListing.getDiagnostics().size());
        assertTrue(apiListing.getDiagnostics().get(0).getHelpLinkUri().contains(DEFAULT_MAVEN_NAME_GUIDELINE_LINK));
    }

    /**
     * Tests that an invalid package name is flagged.
     */
    @Test
    public void invalidPackageName() {
        MavenPackageAndDescriptionDiagnosticRule rule = new MavenPackageAndDescriptionDiagnosticRule();
        APIListing apiListing = new APIListing();
        Pom pom = mockRealPom("A name that doesn't pass the rule",
            "This package contains the Microsoft Azure ApiView client library");
        apiListing.setMavenPom(pom);

        rule.scanFinal(apiListing);

        assertEquals(1, apiListing.getDiagnostics().size());
        assertTrue(apiListing.getDiagnostics().get(0).getHelpLinkUri().contains(DEFAULT_MAVEN_NAME_GUIDELINE_LINK));
    }

    /**
     * Tests that a valid package name isn't flagged.
     */
    @Test
    public void validPackageName() {
        MavenPackageAndDescriptionDiagnosticRule rule = new MavenPackageAndDescriptionDiagnosticRule();
        APIListing apiListing = new APIListing();
        Pom pom = mockRealPom("Microsoft Azure client library for ApiView",
            "This package contains the Microsoft Azure ApiView client library");
        apiListing.setMavenPom(pom);

        rule.scanFinal(apiListing);

        assertEquals(0, apiListing.getDiagnostics().size());
    }

    /**
     * Tests that a custom package name validation can be used.
     */
    @Test
    public void validCustomPackageName() {
        MavenPackageAndDescriptionDiagnosticRule rule = new MavenPackageAndDescriptionDiagnosticRule(
            Pattern.compile("A custom package name validation"), DEFAULT_MAVEN_DESCRIPTION,
            DEFAULT_MAVEN_NAME_GUIDELINE_LINK, DEFAULT_MAVEN_DESCRIPTION_GUIDELINE_LINK);
        APIListing apiListing = new APIListing();
        Pom pom = mockRealPom("A custom package name validation",
            "This package contains the Microsoft Azure ApiView client library");
        apiListing.setMavenPom(pom);

        rule.scanFinal(apiListing);

        assertEquals(0, apiListing.getDiagnostics().size());
    }

    /**
     * Tests that a custom link can be used for the package name guideline.
     */
    @Test
    public void customPackageNameGuidelineLink() {
        MavenPackageAndDescriptionDiagnosticRule rule = new MavenPackageAndDescriptionDiagnosticRule(DEFAULT_MAVEN_NAME,
            DEFAULT_MAVEN_DESCRIPTION, "custom package name guideline link", DEFAULT_MAVEN_DESCRIPTION_GUIDELINE_LINK);
        APIListing apiListing = new APIListing();
        Pom pom = mockRealPom("A name that doesn't pass the rule",
            "This package contains the Microsoft Azure ApiView client library");
        apiListing.setMavenPom(pom);

        rule.scanFinal(apiListing);

        assertEquals(1, apiListing.getDiagnostics().size());
        assertTrue(apiListing.getDiagnostics().get(0).getHelpLinkUri().contains("custom package name guideline link"));
    }

    private static Pom mockRealPom(String name, String description) {
        Pom pom = mock(Pom.class);
        when(pom.getName()).thenReturn(name);
        when(pom.getDescription()).thenReturn(description);
        when(pom.isPomFileReal()).thenReturn(true);

        return pom;
    }
}
