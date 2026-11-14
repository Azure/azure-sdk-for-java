// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.azure;

import com.azure.tools.apiview.processor.diagnostics.rules.utils.FluentSetterReturnTypeDiagnosticRule;

/**
 * Checks azure core fluent setter return type conventions.
 */
public class AzureCoreFluentSetterReturnTypeDiagnosticRule extends FluentSetterReturnTypeDiagnosticRule {

    /**
     * Creates an azure core fluent setter return type diagnostic rule.
     */
    public AzureCoreFluentSetterReturnTypeDiagnosticRule() {
        super(type -> type.getAnnotationByName("Fluent").isPresent());
    }
}
