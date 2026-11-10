package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.diagnostics.DiagnosticRule;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Diagnostic;
import com.github.javaparser.ast.CompilationUnit;

import java.util.regex.Pattern;

import static com.azure.tools.apiview.processor.analysers.util.ASTUtils.*;
import static com.azure.tools.apiview.processor.model.DiagnosticKind.*;

public class PackageNameDiagnosticRule implements DiagnosticRule {
    private final Pattern regex;

    public PackageNameDiagnosticRule(Pattern regex) {
        this.regex = regex;
    }

    @Override
    public void scanIndividual(final CompilationUnit cu, final APIListing listing) {
        getPackageName(cu).ifPresent(packageName -> {
            // we need to map the issue to the class id, because package text isn't printed in the APIView output
            getClassName(cu).map(listing.getKnownTypes()::get).ifPresent(typeId -> {
                if (!regex.matcher(packageName).matches()) {
                    listing.addDiagnostic(new Diagnostic(
                        ERROR,
                        typeId,
                        "Package name match the following regex: " + regex.pattern()));
                }
            });
        });
    }
}
