// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.clientcore;

import com.azure.tools.apiview.processor.diagnostics.rules.utils.FluentSetterReturnTypeDiagnosticRule;

/**
 * Checks client core fluent setter return type conventions.
 */
public class ClientCoreFluentSetterReturnTypeDiagnosticRule extends FluentSetterReturnTypeDiagnosticRule {

    /**
     * Creates a client core fluent setter return type diagnostic rule.
     */
    public ClientCoreFluentSetterReturnTypeDiagnosticRule() {
        super(type -> type.getAnnotationByName("Metadata")
            .map(annotationExpr -> annotationExpr.asNormalAnnotationExpr()
                .getPairs()
                .stream()
                .filter(pair -> "conditions".equals(pair.getName().asString()))
                .anyMatch(pair -> pair.getValue().toString().contains("FLUENT")))
            .orElse(false));
    }
}
