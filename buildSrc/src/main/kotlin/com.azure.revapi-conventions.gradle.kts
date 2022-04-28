import org.gradle.api.Plugin
import org.gradle.api.Project
import org.revapi.API
import org.revapi.AnalysisContext
import org.revapi.PipelineConfiguration
import org.revapi.Revapi
import org.revapi.base.FileArchive

repositories {
    mavenCentral()
}

val revapiBaseline by configurations.creating {
    resolutionStrategy {
        componentSelection {
            all {
                if (candidate.group == "com.azure" && candidate.module == project.name
                    && !candidate.version.matches(Regex("^\\d+\\.\\d+\\.\\d+\$"))) {
                    reject("Non GA Version")
                }
            }
        }
    }

}

dependencies {
    revapiBaseline("com.azure", project.name, "[1.0.0,)")
}

class RevApiPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        val skipRevApi: String? by project
        if (skipRevApi != "true") {
            project.task("revapi") {
                dependsOn("jar")
                doLast {

                    // TODO this needs further refinement.

                    val newArchiveFile = project.tasks.getByName<Jar>("jar").archiveFile.get().asFile
                    val newArchiveSupportingFiles = project.configurations.getByName("compileClasspath").toList()

                    val oldArchiveFile = project.configurations.getByName("revapiBaseline").first()
                    val oldArchiveSupportingFiles = project.configurations.getByName("revapiBaseline").drop(1)

                    val newAPI = API.of(FileArchive(newArchiveFile))
                        .supportedBy(newArchiveSupportingFiles.map { f -> FileArchive(f) })
                        .build()
                    val oldAPI = API.of(FileArchive(oldArchiveFile))
                        .supportedBy(oldArchiveSupportingFiles.map { f -> FileArchive(f) })
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
}

apply<RevApiPlugin>()
