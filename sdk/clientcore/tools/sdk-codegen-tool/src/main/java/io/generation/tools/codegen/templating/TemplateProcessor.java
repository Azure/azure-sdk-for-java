package io.generation.tools.codegen.templating;

import io.generation.tools.codegen.models.TemplateInput;

import javax.annotation.processing.ProcessingEnvironment;

public interface TemplateProcessor {
    static TemplateProcessor getInstance() {
        return new JavaPoetTemplateProcessor();
    }

    void process(TemplateInput templateInput, ProcessingEnvironment processingEnv);
}
