package com.azure.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.revapi.API
import org.revapi.AnalysisContext
import org.revapi.AnalysisResult
import org.revapi.PipelineConfiguration
import org.revapi.Revapi
import org.revapi.base.FileArchive

class RevApiPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task('revapi') {
            dependsOn project.jar
            doLast {

                // TODO this needs further refinement.
                API newAPI = API.of(new FileArchive(project.jar.archiveFile.get().getAsFile()))
                    .supportedBy(project.configurations.compileClasspath.collect { new FileArchive(it)})
                    .build()
                API oldAPI = API.of(new FileArchive(project.jar.archiveFile.get().getAsFile()))
                    .supportedBy(project.configurations.compileClasspath.collect { new FileArchive(it)})
                    .build()

                PipelineConfiguration pipelineConfiguration = PipelineConfiguration.builder()
                    .withAllExtensionsFrom(project.getClass().getClassLoader())
                    .withAllExtensionsFromThreadContextClassLoader()
                    .build()
                Revapi revApi = new Revapi(pipelineConfiguration)
                AnalysisContext analysisContext = AnalysisContext.builder(revApi)
                    .withOldAPI(oldAPI)
                    .withNewAPI(newAPI)
                    .build()
                AnalysisResult analysisResult = revApi.analyze(analysisContext)
                analysisResult.throwIfFailed()
            }
        }
        project.check.dependsOn project.revapi
    }
}
