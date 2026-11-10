package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;

import java.util.regex.Pattern;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getClasses;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPublicOrProtectedMethods;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.makeId;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.INFO;

/**
 * This diagnostic rule checks that the client builders and clients have codesnippets in their JavaDocs. This also
 * checks to see if any of service methods have codesnippets.
 */
public class MissingJavadocCodeSnippetsRule implements DiagnosticRule {
    public static final String CODE_SNIPPET_TAG = "{@codesnippet";

    private static final Pattern NEW_CODESNIPPET_TAG = Pattern
        .compile("(\\s*)\\*?\\s*<!--\\s+src_embed\\s+([a-zA-Z0-9.#\\-_]+)\\s*-->");

    @Override
    public void scanIndividual(final CompilationUnit cu, final APIListing listing) {
        getClasses(cu).forEach(typeDeclaration -> {
            String className = typeDeclaration.getNameAsString();

            // Check for codesnippets in class level javadoc for clients and client builders
            if (typeDeclaration.getJavadocComment().isPresent()
                    && (className.endsWith("Client") || className.endsWith("Builder"))) {

                if (!javadocContainsCodeSnippetTag(typeDeclaration.getJavadocComment().get().getContent())) {
                    listing.addDiagnostic((new Diagnostic(
                            INFO,
                            makeId(typeDeclaration),
                            "JavaDoc for clients and builders should include code samples to instantiate clients.",
                            "https://github.com/Azure/azure-sdk-for-java/wiki/JavaDoc-with-CodeSnippet"
                    )));
                }
            }

            // check for codesnippets in javadoc for service methods
            if (className.endsWith("Client")) {
                // Check if at least one service method has codesnippets in the JavaDoc.
                // Ideally, all service methods should have codesnippets but due to the number of overloads,
                // adding codesnippet for each variant can be a lot. So, this check is to find cases where none of the
                // service methods have codesnippets. If we find at least one service method with codesnippets, we
                // assume library developers have picked the methods that are most useful to provide code samples.
                boolean serviceMethodHasCodesnippets = getPublicOrProtectedMethods(cu)
                        .filter(methodDeclaration -> methodDeclaration.isAnnotationPresent("ServiceMethod"))
                        .filter(methodDeclaration -> methodDeclaration.getJavadocComment().isPresent())
                        .map(methodDeclaration -> methodDeclaration.getJavadocComment().get().getContent())
                        .anyMatch(MissingJavadocCodeSnippetsRule::javadocContainsCodeSnippetTag);

                if (!serviceMethodHasCodesnippets) {
                    listing.addDiagnostic((new Diagnostic(
                            INFO,
                            makeId(typeDeclaration),
                            "JavaDoc for service methods should include code samples.",
                            "https://github.com/Azure/azure-sdk-for-java/wiki/JavaDoc-with-CodeSnippet"
                    )));
                }
            }
        });
    }

    private static boolean javadocContainsCodeSnippetTag(String javadoc) {
        if (javadoc == null) {
            return false;
        }

        return javadoc.contains(CODE_SNIPPET_TAG) || NEW_CODESNIPPET_TAG.matcher(javadoc).find();
    }
}
