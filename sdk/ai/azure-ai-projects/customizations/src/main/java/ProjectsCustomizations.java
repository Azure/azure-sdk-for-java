import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

/**
 * This class contains the customization code to customize the AutoRest generated code for the Projects Client library
 * Reference: https://github.com/Azure/autorest.java/blob/main/customization-base/README.md
 */
public class ProjectsCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        preserveRoutineCompatibilityOverloads(libraryCustomization, logger);
        annotateBetaClients(libraryCustomization, logger);
        annotateBetaFields(libraryCustomization, loadBetaAnnotations(logger), logger);
    }

    private void preserveRoutineCompatibilityOverloads(LibraryCustomization customization, Logger logger) {
        addRoutineCompatibilityOverload(customization, "BetaRoutinesClient", "Routine", false, logger);
        addRoutineCompatibilityOverload(customization, "BetaRoutinesAsyncClient", "Mono<Routine>", true, logger);
    }

    private void addRoutineCompatibilityOverload(LibraryCustomization customization, String className,
        String returnType, boolean isAsync, Logger logger) {
        customization.getClass("com.azure.ai.projects", className).customizeAst(ast -> {
            ast.addImport("com.azure.ai.projects.models.Routine");
            ast.addImport("com.azure.ai.projects.models.RoutineAction");
            ast.addImport("com.azure.ai.projects.models.RoutineTrigger");
            ast.addImport("com.azure.core.annotation.ServiceMethod");
            ast.addImport("java.util.Map");
            if (isAsync) {
                ast.addImport("reactor.core.publisher.Mono");
            }

            ast.getClassByName(className).ifPresent(clazz -> {
                if (hasRoutineCompatibilityOverload(clazz)) {
                    return;
                }

                logger.info("Adding Revapi compatibility overload to {}", className);
                clazz.addMethod("createOrUpdateRoutine", Modifier.Keyword.PUBLIC)
                    .setType(returnType)
                    .addParameter("String", "routineName")
                    .addParameter("String", "description")
                    .addParameter("Boolean", "enabled")
                    .addParameter("Map<String, RoutineTrigger>", "triggers")
                    .addParameter("RoutineAction", "action")
                    .addAnnotation(StaticJavaParser.parseAnnotation(
                        "@ServiceMethod(returns = com.azure.core.annotation.ReturnType.SINGLE)"))
                    .setJavadocComment("Creates a new routine or replaces an existing routine without authorization.\n"
                        + "\n"
                        + "@param routineName The unique name of the routine.\n"
                        + "@param description The routine description.\n"
                        + "@param enabled Whether the routine is enabled.\n"
                        + "@param triggers The triggers that invoke the routine.\n"
                        + "@param action The action performed by the routine.\n"
                        + "@return The created or updated routine.")
                    .setBody(StaticJavaParser.parseBlock("{ return createOrUpdateRoutine(routineName, description, "
                        + "enabled, triggers, action, null); }"));
            });
        });
    }

    private boolean hasRoutineCompatibilityOverload(TypeDeclaration<?> type) {
        return type.getMethodsByName("createOrUpdateRoutine").stream().anyMatch(method ->
            method.getParameters().size() == 5
                && "String".equals(method.getParameter(0).getType().asString())
                && "String".equals(method.getParameter(1).getType().asString())
                && "Boolean".equals(method.getParameter(2).getType().asString())
                && "Map<String, RoutineTrigger>".equals(method.getParameter(3).getType().asString())
                && "RoutineAction".equals(method.getParameter(4).getType().asString()));
    }

    private void annotateBetaClients(LibraryCustomization customization, Logger logger) {
        customization.getPackage("com.azure.ai.projects")
            .listClasses()
            .stream()
            .filter(cc -> cc.getClassName().startsWith("Beta") && cc.getClassName().endsWith("Client"))
            .forEach(classCustomization -> {
                String simpleName = classCustomization.getClassName();
                logger.info("Annotating {} with @Beta", simpleName);
                classCustomization.customizeAst(ast -> ast.getClassByName(simpleName).ifPresent(clazz -> {
                    ast.addImport("com.azure.ai.projects.implementation.utils.Beta");
                    clazz.addAnnotation(betaAnnotation("This class is in preview and may change in future releases."));
                }));
            });
    }

    private void annotateBetaFields(LibraryCustomization customization, List<String[]> betaAnnotations,
                                    Logger logger) {
        for (String[] entry : betaAnnotations) {
            String className = entry[0];
            String member = entry[1];
            String description = entry[2];
            int lastDot = className.lastIndexOf('.');
            String packageName = className.substring(0, lastDot);
            String simpleName = className.substring(lastDot + 1);

            logger.info("Annotating {}{} with @Beta", className, member == null ? "" : "#" + member);

            ClassCustomization classCustomization = null;
            try {
                classCustomization = customization.getClass(packageName, simpleName);
            } catch (IllegalArgumentException ex) {
                logger.info(packageName + simpleName + " does not exit.");
                continue;
            }

            classCustomization.customizeAst(ast -> ast.getTypes().stream()
                .filter(type -> type.getNameAsString().equals(simpleName))
                .findFirst()
                .ifPresent(type -> {
                    ast.addImport("com.azure.ai.projects.implementation.utils.Beta");
                    if (member == null) {
                        type.addAnnotation(betaAnnotation(description));
                    } else {
                        annotateMember(type, member, description, logger);
                    }
                }));
        }
    }

    private void annotateMember(TypeDeclaration<?> type, String member, String description, Logger logger) {
        String fieldName = toCamelCase(member);
        boolean found = false;

        for (FieldDeclaration field : type.getFields()) {
            if (field.getVariables().stream().anyMatch(v -> v.getNameAsString().equals(fieldName))) {
                field.addAnnotation(betaAnnotation(description));
                found = true;
            }
        }

        if (!found) {
            logger.error("Could not find field '{}' on type {}", fieldName, type.getNameAsString());
            throw new IllegalStateException(
                "Could not find field '" + fieldName + "' on type " + type.getNameAsString() + ".");
        }

        String capitalized = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        for (String accessor : new String[] { "get" + capitalized, "is" + capitalized, "set" + capitalized }) {
            for (MethodDeclaration method : type.getMethodsByName(accessor)) {
                method.addAnnotation(betaAnnotation(description));
            }
        }
    }

    private static AnnotationExpr betaAnnotation(String description) {
        StringLiteralExpr warningText = new StringLiteralExpr();
        warningText.setString(description);
        NormalAnnotationExpr annotation = new NormalAnnotationExpr();
        annotation.setName("Beta");
        annotation.addPair("warningText", warningText);
        return annotation;
    }

    private static String toCamelCase(String name) {
        if (name.indexOf('_') < 0) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name.length());
        boolean upperNext = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                upperNext = true;
            } else {
                sb.append(upperNext ? Character.toUpperCase(c) : c);
                upperNext = false;
            }
        }
        return sb.toString();
    }

    private static final String CSV_FILE_NAME = "beta-annotations.csv";

    /**
     * Loads the {@code @Beta} annotation entries from {@code beta-annotations.csv}. This file is the single source of
     * truth and is produced/updated by external tooling.
     * <p>
     * Format: a header row followed by {@code ;}-separated entries of
     * {@code type;class_name;annotation_description;member_name}. {@code type} is {@code class} (no member) or
     * {@code field} (member required). Blank lines and lines starting with {@code #} are ignored.
     */
    private List<String[]> loadBetaAnnotations(Logger logger) {
        Path csvPath = locateBetaCsv(logger);
        if (csvPath == null) {
            return new ArrayList<>();
        }
        logger.info("Loading @Beta annotations from {}", csvPath);

        List<String> lines;
        try {
            lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            logger.error("Failed to read @Beta annotations from {}", csvPath, ex);
            throw new UncheckedIOException("Failed to read @Beta annotations from " + csvPath, ex);
        }

        List<String[]> annotations = new ArrayList<>();
        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.charAt(0) == '#') {
                continue;
            }

            // Skip the header row (type;class_name;annotation_description;member_name).
            if (lineNumber == 1) {
                continue;
            }

            String[] columns = line.split(";", 4);
            if (columns.length < 3) {
                logger.error("Line {} of {} must have ';'-separated columns"
                    + " (type;class_name;annotation_description;member_name): {}", lineNumber, CSV_FILE_NAME, line);
                throw new IllegalStateException("Line " + lineNumber + " of " + CSV_FILE_NAME
                    + " must have ';'-separated columns (type;class_name;annotation_description;member_name): "
                    + line);
            }

            String type = columns[0].trim();
            String className = columns[1].trim();
            String description = columns[2];
            String member = columns.length >= 4 ? columns[3].trim() : "";
            if (className.isEmpty() || description.isEmpty()) {
                logger.error("Line {} of {} requires a class_name and an annotation_description: {}", lineNumber,
                    CSV_FILE_NAME, line);
                throw new IllegalStateException("Line " + lineNumber + " of " + CSV_FILE_NAME
                    + " requires a class_name and an annotation_description: " + line);
            }
            if ("field".equals(type) && member.isEmpty()) {
                logger.error("Line {} of {} is a field entry but has no member_name: {}", lineNumber, CSV_FILE_NAME,
                    line);
                throw new IllegalStateException("Line " + lineNumber + " of " + CSV_FILE_NAME
                    + " is a field entry but has no member_name: " + line);
            }

            annotations.add(new String[] { className, member.isEmpty() ? null : member, description });
        }

        logger.info("Loaded {} @Beta annotation entries", annotations.size());
        return annotations;
    }

    /**
     * Resolves the {@code beta-annotations.csv} path. {@code tsp-client update} launches the customization with its
     * working directory set to the library module (so the file lives at {@code <module>/customizations/...}), while
     * spec SDK-generation launches from the repo root; both locations are checked. Returns {@code null} (rather than
     * failing) when the file cannot be found, in which case {@code @Beta} annotations are skipped.
     */
    private Path locateBetaCsv(Logger logger) {
        // tsp-client update launches the customization from the module folder (user.dir = module).
        Path modulePath = Paths.get(System.getProperty("user.dir"), "customizations", CSV_FILE_NAME).toAbsolutePath();
        if (Files.isRegularFile(modulePath)) {
            return modulePath;
        }
        // spec SDK-generation (spec-gen-sdk) launches from the repo root (user.dir = repo root).
        Path repoRootPath = Paths
            .get(System.getProperty("user.dir"), "sdk", "ai", "azure-ai-projects", "customizations", CSV_FILE_NAME)
            .toAbsolutePath();
        if (Files.isRegularFile(repoRootPath)) {
            return repoRootPath;
        }
        logger.warn("Could not locate {} at {} or {} (user.dir={}); skipping @Beta annotations.", CSV_FILE_NAME,
            modulePath, repoRootPath, System.getProperty("user.dir"));
        return null;
    }
}
