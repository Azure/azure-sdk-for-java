package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.util.regex.Pattern;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getClasses;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.getPackageName;
import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.makeId;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.INFO;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.WARNING;

/**
 *
 */
public class MissingEqualsAndHashCodeDiagnosticRule implements DiagnosticRule {

    private final Pattern packageRegex;

    public MissingEqualsAndHashCodeDiagnosticRule(String packageRegex) {
        this.packageRegex = Pattern.compile(packageRegex);
    }

    @Override
    public void scanIndividual(final CompilationUnit cu, final APIListing listing) {
        getClasses(cu)
            .filter(type -> packageRegex.matcher(getPackageName(type)).matches())
            .forEach(typeDeclaration -> {
                boolean hasEquals = hasValidEqualsMethod(typeDeclaration);
                boolean hasHashCode = hasValidHashCodeMethod(typeDeclaration);

                if (!hasEquals && !hasHashCode) {
                    listing.addDiagnostic(new Diagnostic(
                            INFO,
                            makeId(typeDeclaration),
                            "Both equals and hashCode methods are missing. Consider adding both methods."
                    ));
                } else if (!hasEquals) {
                    listing.addDiagnostic(new Diagnostic(
                            WARNING,
                            makeId(typeDeclaration),
                            "equals method is missing, but hashCode method is present. Consider adding the equals method."
                    ));
                } else if (!hasHashCode) {
                    listing.addDiagnostic(new Diagnostic(
                            WARNING,
                            makeId(typeDeclaration),
                            "hashCode method is missing, but equals method is present. Consider adding the hashCode method."
                    ));
                }
            });
    }

    private boolean hasValidEqualsMethod(TypeDeclaration<?> typeDeclaration) {
        return typeDeclaration.getMembers().stream()
            .filter(BodyDeclaration::isMethodDeclaration)
            .map(BodyDeclaration::asMethodDeclaration)
            .anyMatch(this::isValidEqualsMethod);
    }

    private boolean hasValidHashCodeMethod(TypeDeclaration<?> typeDeclaration) {
        return typeDeclaration.getMethods().stream()
            .filter(BodyDeclaration::isMethodDeclaration)
            .map(BodyDeclaration::asMethodDeclaration)
            .anyMatch(this::isValidHashCodeMethod);
    }

    private boolean isValidEqualsMethod(MethodDeclaration method) {
        return method.getParameters().size() == 1
            && "equals".equals(method.getNameAsString())
            && method.getType().isPrimitiveType()
            && "boolean".equals(method.getType().asString())
            && "Object".equals(method.getParameter(0).getType().asString())
            && method.isAnnotationPresent("Override");
    }

    private boolean isValidHashCodeMethod(MethodDeclaration method) {
        return method.getParameters().isEmpty()
            && "hashCode".equals(method.getNameAsString())
            && method.getType().isPrimitiveType()
            && "int".equals(method.getType().asString())
            && method.isAnnotationPresent("Override");
    }
}
