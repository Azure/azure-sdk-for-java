package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;

import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.*;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.*;

/**
 * This diagnostic, whilst being in the 'general' package, is still very heavily skewed towards azure and client core
 * libraries, so it isn't enabled for all libraries.
 */
public class MissingAnnotationsDiagnosticRule implements DiagnosticRule {

    private final String packagePrefix;

    public MissingAnnotationsDiagnosticRule(String packagePrefix) {
        this.packagePrefix = packagePrefix;
    }

    @Override
    public void scanIndividual(final CompilationUnit cu, final APIListing listing) {
        getClasses(cu)
            .filter(type -> getPackageName(type).startsWith(packagePrefix))   // we only want to give this guidance to Azure SDK developers
            .forEach(typeDeclaration -> {
                String className = typeDeclaration.getNameAsString();

                if (className.endsWith("Builder")) {
                    // check if @ServiceClientBuilder annotation is present
                    if (!typeDeclaration.isAnnotationPresent("ServiceClientBuilder")) {
                        listing.addDiagnostic(new Diagnostic(
                            INFO,
                            makeId(typeDeclaration),
                            "Classes named *Builder are potential candidates to have the @ServiceClientBuilder annotation applied.",
                            "https://azure.github.io/azure-sdk/java_introduction.html#service-client-creation"));
                    }
                } else if (className.endsWith("Client")) {
                    // check if the @ServiceClient annotation is present
                    if (!typeDeclaration.isAnnotationPresent("ServiceClient")) {
                        listing.addDiagnostic(new Diagnostic(
                            INFO,
                            makeId(typeDeclaration),
                            "Classes named *Client are potential candidates to have the @ServiceClient annotation applied.",
                            "https://azure.github.io/azure-sdk/java_introduction.html#service-client"));
                    }

                    // check all public / protected methods in client classes. Because we can't easily determine if a method
                    // should have an annotation, all we can do is count the number of methods that are annotated and compare
                    // this with the total number of methods. If the ratio is not high enough, we will warn the user that there
                    // may be missing annotations.
                    final AtomicInteger methodCount = new AtomicInteger();
                    final AtomicInteger annotatedMethodCount = new AtomicInteger();
                    getPublicOrProtectedMethods(cu).forEach(methodDeclaration -> {
                        methodCount.incrementAndGet();
                        if (methodDeclaration.isAnnotationPresent("ServiceMethod")) {
                            annotatedMethodCount.incrementAndGet();
                        }
                    });

                    if (annotatedMethodCount.get() / (double) methodCount.get() < 0.75) {
                        // warn user to double check
                        listing.addDiagnostic(new Diagnostic(
                            WARNING,
                            makeId(typeDeclaration),
                        "There is a low number of methods annotated with @ServiceMethod. " +
                                "Please review to ensure all appropriate methods have this annotation.",
                            "https://azure.github.io/azure-sdk/java_introduction.html#service-client"));
                    }
                }
        });
    }
}
