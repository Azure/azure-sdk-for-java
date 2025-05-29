package com.azure.openrewrite.goldenimages;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "compile-golden-image")
public class GoldenImageCompilerMojo extends AbstractMojo {
    private static final boolean LIST_DEPENDENCIES = false;
    private static final boolean LIST_CLASSES = false;
    private static final boolean LOG_COMPILE_COMMAND = false;

    /**
     * Comma-separated list of profiles to build.
     * For example: "v1,v2"
     */
    @Parameter(property = "targetProfiles", defaultValue = "v1")
    private String targetProfiles;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File baseDir;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Component
    private ProjectBuilder projectBuilder;

    public void execute() throws MojoExecutionException {
        String testResourcesPath = baseDir.getAbsolutePath() + "/src/test/resources/migrationExamples";
        File testResourcesDir = new File(testResourcesPath);

        if (!testResourcesDir.exists()) {
            throw new MojoExecutionException("Test resources directory does not exist: " + testResourcesPath);
        }

        List<File> testFolders = findTestFolders(testResourcesDir);
        String[] profiles = targetProfiles.split(",");

        for (File folder : testFolders) {
            for (String profile : profiles) {
                File profileDir = new File(folder, profile);

                if (!profileDir.exists()) {
                    getLog().warn("Skipping " + folder.getAbsolutePath() + " as profile directory " + profile + " does not exist.");
                    continue;
                }

                File previousProfileDir = getBaseProfileDir(folder, profile);
                if (previousProfileDir != null && previousProfileDir.exists()) {
                    if (hasChanges(profileDir, previousProfileDir)) {
                        compileJavaFilesWithProfile(profileDir, profile);
                    } else {
                        getLog().info("No changes detected between " + profileDir.getAbsolutePath() + " and " + previousProfileDir.getAbsolutePath());
                    }
                } else {
                    compileJavaFilesWithProfile(profileDir, profile);
                }
            }
        }
    }

    private List<File> findTestFolders(File baseDir) throws MojoExecutionException {
        try (Stream<Path> paths = Files.walk(baseDir.toPath())) {
            return paths
                .filter(Files::isDirectory)
                .filter(path -> Arrays.stream(targetProfiles.split(","))
                    .allMatch(profile -> path.resolve(profile).toFile().exists()))
                .map(Path::toFile)
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to find test folders", e);
        }
    }

    private File getBaseProfileDir(File folder, String profile) {
        int profileVersion;
        try {
            profileVersion = Integer.parseInt(profile.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }
        if (profileVersion > 1) {
            return new File(folder, "v" + (profileVersion - 1));
        }
        return null;
    }

    private boolean hasEqualFileContents(File file1, File file2) throws IOException {


        String before = Files.readAllLines(file1.toPath())
            .stream()
            .collect(Collectors.joining("\n"));

        String after = Files.readAllLines(file2.toPath())
            .stream()
            .collect(Collectors.joining("\n"));

        return before.equals(after);
    }

    private boolean hasChanges(File profileDir, File previousProfileDir) throws MojoExecutionException {
        getLog().info("Comparing files in " + profileDir.getAbsolutePath() + " with " + previousProfileDir.getAbsolutePath());

        try (Stream<Path> profilePaths = Files.walk(profileDir.toPath())) {
            return profilePaths
                       .filter(Files::isRegularFile)
                       .filter(path -> path.toString().endsWith(".java"))
                       .anyMatch(profileFile -> {
                           Path relativePath = profileDir.toPath().relativize(profileFile);
                           Path previousFile = previousProfileDir.toPath().resolve(relativePath);

                           try {
                               if (!Files.exists(previousFile) || !hasEqualFileContents(profileFile.toFile(), previousFile.toFile())) {
                                   getLog().info("Changes detected in " + profileFile.toString());
                                   return true;
                               }
                           } catch (IOException e) {
                               throw new RuntimeException(e);
                           }
                            getLog().info("No changes detected in " + profileFile.toString());
                            return false;
                       });

        } catch (IOException e) {
            throw new MojoExecutionException("Failed to compare files in " + profileDir.getAbsolutePath() + " and " + previousProfileDir.getAbsolutePath(), e);
        }
    }

    private void compileJavaFilesWithProfile(File dir, String profile) throws MojoExecutionException {
        getLog().info("Compiling " + dir.getAbsolutePath() + " using profile " + profile);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new MojoExecutionException("No Java compiler available. Make sure you are running the plugin with a JDK.");
        }

        try (Stream<Path> paths = Files.walk(Paths.get(dir.getAbsolutePath()))) {
            List<String> javaFiles = paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .map(Path::toString)
                .collect(Collectors.toList());

            if (javaFiles.isEmpty()) {
                getLog().info("No Java files found in " + dir.getAbsolutePath());
                return;
            }

            if (LIST_CLASSES) {
                // Log the Java files that will be compiled
                getLog().info("Java files to be compiled:");
                for (String javaFile : javaFiles) {
                    getLog().info(" - " + javaFile);
                }
            }

            // Activate the profile and rebuild the project
            ProjectBuildingRequest buildingRequest = session.getProjectBuildingRequest();
            buildingRequest.setActiveProfileIds(Collections.singletonList(profile));
            buildingRequest.setResolveDependencies(true);

            // Rebuild the project with the active profile
            ProjectBuildingResult result;
            try {
                result = projectBuilder.build(project.getFile(), buildingRequest);
            } catch (ProjectBuildingException e) {
                getLog().error("Failed to build project with profile " + profile, e);
                throw new MojoExecutionException("Failed to build project with profile " + profile, e);
            }
            MavenProject activeProject = result.getProject();

            // Add classpath dependencies
            List<String> classpathElements;
            try {
                classpathElements = activeProject.getCompileClasspathElements();
            } catch (DependencyResolutionRequiredException e) {
                getLog().error("Failed to resolve dependencies for profile " + profile, e);
                throw new MojoExecutionException("Failed to resolve dependencies for profile " + profile, e);
            }
            String classpath = String.join(File.pathSeparator, classpathElements);

            // Log the classpath elements
            if (LIST_DEPENDENCIES) {
                getLog().info("Classpath for profile " + profile + ":");
                for (String element : classpathElements) {
                    getLog().info(" - " + element);
                }
            }

            // Create the compilation command
            List<String> compileOptions = new ArrayList<>(javaFiles);
            compileOptions.add(0, "-classpath");
            compileOptions.add(1, classpath);

            // Log the compilation command
            if (LOG_COMPILE_COMMAND) {
                getLog().info("Compilation command:");
                for (String option : compileOptions) {
                    getLog().info(" " + option);
                }
            }

            // Compile Java files
            int compileResult = compiler.run(null, null, null, compileOptions.toArray(new String[0]));

            if (compileResult != 0) {
                throw new MojoExecutionException("Compilation failed for " + dir.getAbsolutePath());
            } else {
                getLog().info("Compilation successful for " + dir.getAbsolutePath());
            }
        } catch (IOException e) {
            getLog().error("Failed to compile Java files in " + dir.getAbsolutePath(), e);
            throw new MojoExecutionException("Failed to compile Java files in " + dir.getAbsolutePath(), e);
        } finally {
            // Clean up .class files
            cleanClassFiles(dir);
        }
    }

    private void cleanClassFiles(File dir) {
        try (Stream<Path> paths = Files.walk(dir.toPath())) {
            List<Path> classFiles = paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".class"))
                .collect(Collectors.toList());

            for (Path classFile : classFiles) {
                Files.delete(classFile);
                getLog().info("Deleted .class file: " + classFile.toString());
            }
        } catch (IOException e) {
            getLog().error("Failed to clean .class files in " + dir.getAbsolutePath(), e);
        }
    }
}
