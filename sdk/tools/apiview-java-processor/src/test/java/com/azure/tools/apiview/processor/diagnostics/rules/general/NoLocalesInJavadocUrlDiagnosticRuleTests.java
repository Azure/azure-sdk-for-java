// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.rules.general.NoLocalesInJavadocUrlDiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link NoLocalesInJavadocUrlDiagnosticRule}.
 */
public class NoLocalesInJavadocUrlDiagnosticRuleTests {
    @Test
    public void matchesSingleLocaleInJavadocLine() {
        final String javadoc = "A Javadoc line with a/url/with/en-us/locale";

        NoLocalesInJavadocUrlDiagnosticRule rule = new NoLocalesInJavadocUrlDiagnosticRule();
        NodeWithJavadoc<?> javadocNode = createJavadocNode(javadoc);
        APIListing apiListing = new APIListing();

        rule.checkJavaDoc(javadocNode, "matchesSingleLocaleInJavadocLine", apiListing);

        assertEquals(1, apiListing.getDiagnostics().size());
        assertTrue(apiListing.getDiagnostics().get(0).getText().contains("/en-us/"));
    }

    @Test
    public void matchesAllLocalesInJavadocLine() {
        final String javadoc = "A Javadoc line with a/url/with/en-us/local and another/url/with/fr-fr/locale";

        NoLocalesInJavadocUrlDiagnosticRule rule = new NoLocalesInJavadocUrlDiagnosticRule();
        NodeWithJavadoc<?> javadocNode = createJavadocNode(javadoc);
        APIListing apiListing = new APIListing();

        rule.checkJavaDoc(javadocNode, "matchesAllLocalesInJavadocLine", apiListing);

        assertEquals(2, apiListing.getDiagnostics().size());
        assertTrue(apiListing.getDiagnostics().get(0).getText().contains("/en-us/"));
        assertTrue(apiListing.getDiagnostics().get(1).getText().contains("/fr-fr/"));
    }

    @Test
    public void doesNotMatchLocaleNotInUrl() {
        final String javadoc = "A Javadoc line with a non-URL en-us locale";

        NoLocalesInJavadocUrlDiagnosticRule rule = new NoLocalesInJavadocUrlDiagnosticRule();
        NodeWithJavadoc<?> javadocNode = createJavadocNode(javadoc);
        APIListing apiListing = new APIListing();

        rule.checkJavaDoc(javadocNode, "doesNotMatchLocaleNotInUrl", apiListing);

        assertEquals(0, apiListing.getDiagnostics().size());
    }

    private static NodeWithJavadoc<?> createJavadocNode(String javadoc) {
        return new NodeWithJavadoc<Node>() {
            @Override
            public Optional<Comment> getComment() {
                return Optional.of(new JavadocComment(javadoc));
            }

            @Override
            public Node setComment(Comment comment) {
                return null;
            }
        };
    }
}
