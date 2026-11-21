package com.azure.tools.apiview.processor.diagnostics.rules.azure;

import com.azure.tools.apiview.processor.diagnostics.rules.utils.FluentSetterReturnTypeDiagnosticRule;

public class AzureCoreFluentSetterReturnTypeDiagnosticRule extends FluentSetterReturnTypeDiagnosticRule {

    public AzureCoreFluentSetterReturnTypeDiagnosticRule() {
        super(type -> type.getAnnotationByName("Fluent").isPresent());
    }
}
