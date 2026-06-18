import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
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
 * This class contains the customization code to customize the AutoRest generated code for the Agents Client library
 * Reference: https://github.com/Azure/autorest.java/blob/main/customization-base/README.md
 */
public class AgentsCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        renameImageGenToolSize(libraryCustomization, logger);
        modifyPollingStrategies(libraryCustomization, logger);
        annotateBetaFields(libraryCustomization, loadBetaAnnotations(logger), logger);
    }

    private void renameImageGenToolSize(LibraryCustomization customization, Logger logger) {
        customization.getClass("com.azure.ai.agents.models", "ImageGenToolSize").customizeAst(ast -> ast.getEnumByName("ImageGenToolSize")
            .ifPresent(clazz -> clazz.getEntries().stream()
                .filter(entry -> "ONE_ZERO_TWO_FOURX_ONE_ZERO_TWO_FOUR".equals(entry.getName().getIdentifier()))
                .forEach(entry -> entry.setName("RESOLUTION_1024_X_1024"))));

        customization.getClass("com.azure.ai.agents.models", "ImageGenToolSize").customizeAst(ast -> ast.getEnumByName("ImageGenToolSize")
            .ifPresent(clazz -> clazz.getEntries().stream()
                .filter(entry -> "ONE_ZERO_TWO_FOURX_ONE_FIVE_THREE_SIX".equals(entry.getName().getIdentifier()))
                .forEach(entry -> entry.setName("RESOLUTION_1024_X_1536"))));

        customization.getClass("com.azure.ai.agents.models", "ImageGenToolSize").customizeAst(ast -> ast.getEnumByName("ImageGenToolSize")
            .ifPresent(clazz -> clazz.getEntries().stream()
                .filter(entry -> "ONE_FIVE_THREE_SIXX_ONE_ZERO_TWO_FOUR".equals(entry.getName().getIdentifier()))
                .forEach(entry -> entry.setName("RESOLUTION_1536_X_1024"))));
    }

    private void modifyPollingStrategies(LibraryCustomization customization, Logger logger) {
        customization.getClass("com.azure.ai.agents.implementation", "OperationLocationPollingStrategy")
            .customizeAst(ast -> ast.getClassByName("OperationLocationPollingStrategy")
                .ifPresent(clazz -> {
                    clazz.getConstructors().get(1).getBody().getStatements()
                        .set(0, StaticJavaParser.parseStatement("super(PollingUtils.OPERATION_LOCATION_HEADER, AgentsServicePollUtils.withFoundryFeatures(pollingStrategyOptions));"));

                    clazz.addMember(StaticJavaParser.parseMethodDeclaration("@Override public Mono<PollResponse<T>> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType) { return super.poll(pollingContext, pollResponseType).map(AgentsServicePollUtils::remapStatus); }"));
                }));

        customization.getClass("com.azure.ai.agents.implementation", "SyncOperationLocationPollingStrategy")
            .customizeAst(ast -> ast.getClassByName("SyncOperationLocationPollingStrategy")
                .ifPresent(clazz -> {
                    clazz.getConstructors().get(1).getBody().getStatements()
                        .set(0, StaticJavaParser.parseStatement("super(PollingUtils.OPERATION_LOCATION_HEADER, AgentsServicePollUtils.withFoundryFeatures(pollingStrategyOptions));"));

                    clazz.addMember(StaticJavaParser.parseMethodDeclaration("@Override public PollResponse<T> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType) { return AgentsServicePollUtils.remapStatus(super.poll(pollingContext, pollResponseType)); }"));
                }));
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

            classCustomization.getClass(packageName, simpleName).customizeAst(ast -> ast.getTypes().stream()
                .filter(type -> type.getNameAsString().equals(simpleName))
                .findFirst()
                .ifPresent(type -> {
                    ast.addImport("com.azure.ai.agents.implementation.utils.Beta");
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
     * working directory set to the library module, so the file lives at {@code <module>/customizations/...}.
     */
    private Path locateBetaCsv(Logger logger) {
        Path csvPath = Paths.get(System.getProperty("user.dir"), "customizations", CSV_FILE_NAME).toAbsolutePath();
        if (!Files.isRegularFile(csvPath)) {
            logger.error("Could not locate {} at expected path {} (user.dir={})", CSV_FILE_NAME, csvPath,
                System.getProperty("user.dir"));
            throw new IllegalStateException("Could not locate " + CSV_FILE_NAME + " at " + csvPath);
        }
        return csvPath;
    }
}
