// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.templating;

import io.clientcore.annotation.processor.models.TemplateInput;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * Interface for processing templates.
 */
public interface TemplateProcessor {
    static TemplateProcessor getInstance() {
        return new JavaPoetTemplateProcessor();
    }

    void process(TemplateInput templateInput, ProcessingEnvironment processingEnv);
}
