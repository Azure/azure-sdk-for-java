// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.templating;

import io.clientcore.annotation.processor.models.TemplateInput;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * Interface for processing templates.
 */
public interface TemplateProcessor {

    /**
     * Returns an instance of the TemplateProcessor.
     *
     * @return a new instance of JavaPoetTemplateProcessor
     */
    static TemplateProcessor getInstance() {
        return new JavaParserTemplateProcessor();
    }

    /**
     * Processes the given template input using the provided processing environment.
     *
     * @param templateInput the input data for the template
     * @param processingEnv the environment used for processing
     */
    void process(TemplateInput templateInput, ProcessingEnvironment processingEnv);
}
