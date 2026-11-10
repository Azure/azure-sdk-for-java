package com.azure.tools.apiview.processor.diagnostics.rules.clientcore;

import com.azure.tools.apiview.processor.diagnostics.rules.utils.FluentSetterReturnTypeDiagnosticRule;

public class ClientCoreFluentSetterReturnTypeDiagnosticRule extends FluentSetterReturnTypeDiagnosticRule {

    public ClientCoreFluentSetterReturnTypeDiagnosticRule() {
        super(type -> type.getAnnotationByName("Metadata")
            .map(annotationExpr -> annotationExpr.asNormalAnnotationExpr().getPairs().stream()
                .filter(pair -> pair.getName().asString().equals("conditions"))
                .anyMatch(pair -> pair.getValue().toString().contains("FLUENT"))).orElse(false));
    }
}
