import org.gradle.api.Plugin
import org.gradle.api.Project
import org.revapi.API
import org.revapi.AnalysisContext
import org.revapi.PipelineConfiguration
import org.revapi.Revapi
import org.revapi.base.FileArchive

class RevApiPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.task("revapi") {
            dependsOn("jar")
            doLast {
                // TODO this needs further refinement.
                val newAPI = API.of(FileArchive(project.tasks.getByName<Jar>("jar").archiveFile.get().asFile))
                    .supportedBy(project.configurations.getByName("compileClasspath").map { f -> FileArchive(f) })
                    .build()
                val oldAPI = API.of(FileArchive(project.tasks.getByName<Jar>("jar").archiveFile.get().asFile))
                    .supportedBy(project.configurations.getByName("compileClasspath").map { f -> FileArchive(f) })
                    .build()

                val pipelineConfiguration = PipelineConfiguration.builder()
                    .withAllExtensionsFrom(project.javaClass.classLoader)
                    .withAllExtensionsFromThreadContextClassLoader()
                    .build()
                val revApi = Revapi(pipelineConfiguration)
                val analysisContext = AnalysisContext.builder(revApi)
                    .withOldAPI(oldAPI)
                    .withNewAPI(newAPI)
                    .build()
                val analysisResult = revApi.analyze(analysisContext)
                analysisResult.throwIfFailed()
            }
        }
        project.tasks.getByName("check").dependsOn("revapi")
    }
}

apply<RevApiPlugin>()
