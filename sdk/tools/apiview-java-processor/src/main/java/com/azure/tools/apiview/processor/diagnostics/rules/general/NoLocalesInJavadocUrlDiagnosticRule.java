package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getClasses;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedConstructors;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedFields;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedMethods;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.makeId;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.WARNING;

/**
 * Diagnostic rule that verifies URL links don't contain locale specifiers.
 * <p>
 * When a URL contains a locale specifier it will almost always load to that locale's language instead of inferring it
 * from a user's machine, which provides a better user experience.
 */
public class NoLocalesInJavadocUrlDiagnosticRule implements DiagnosticRule {
    private static final Pattern LANGUAGE_PATTERN;

    static {
        String pattern = Arrays.stream(Locale.getAvailableLocales())
            .map(Locale::toLanguageTag)
            .map(String::toLowerCase)
            .collect(Collectors.joining("|", "/(", ")/"));

        LANGUAGE_PATTERN = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public void scanIndividual(final CompilationUnit cu, final APIListing listing) {
        getClasses(cu).forEach(typeDeclaration -> {
            // Javadoc on the type itself
            checkJavaDoc(typeDeclaration, makeId(typeDeclaration), listing);

            // then get constructors, fields, and methods and check the javadoc on them
            getPublicOrProtectedConstructors(typeDeclaration).forEach(n -> checkJavaDoc(n, makeId(n), listing));
            getPublicOrProtectedFields(typeDeclaration).forEach(n -> checkJavaDoc(n, makeId(n), listing));
            getPublicOrProtectedMethods(typeDeclaration).forEach(n -> checkJavaDoc(n, makeId(n), listing));
        });
    }

    void checkJavaDoc(NodeWithJavadoc<?> n, String id, APIListing listing) {
        n.getJavadocComment().ifPresent(javadoc -> {
            final String javadocString = javadoc.getContent();

            Matcher matcher = LANGUAGE_PATTERN.matcher(javadocString);
            while (matcher.find()) {
                listing.addDiagnostic(new Diagnostic(WARNING, id,
                    "The JavaDoc string for this API contains what appears to be a locale ('/" + matcher.group(1) + "/'). " +
                        "Commonly, this indicates a URL in the JavaDoc is linking to a specific locale. If so, this " +
                        "should be removed, so we do not assume about the user's locale when accessing a URL."));
            }
        });
    }
}
