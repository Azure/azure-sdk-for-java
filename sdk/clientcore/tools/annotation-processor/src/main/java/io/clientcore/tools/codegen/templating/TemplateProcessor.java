// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.tools.codegen.templating;

import io.clientcore.tools.codegen.models.TemplateInput;

import javax.annotation.processing.ProcessingEnvironment;

public interface TemplateProcessor {
    static TemplateProcessor getInstance() {
        return new JavaPoetTemplateProcessor();
    }

    void process(TemplateInput templateInput, ProcessingEnvironment processingEnv);
}
