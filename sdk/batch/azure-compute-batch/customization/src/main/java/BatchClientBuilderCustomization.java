import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.slf4j.Logger;

public class BatchClientBuilderCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customization.getClass("com.azure.compute.batch", "BatchClientBuilder").customizeAst(ast -> {
        // Add AzureNamedKeyCredentialTrait imports to BatchClientBuilder
        ast.addImport("com.azure.core.client.traits.AzureNamedKeyCredentialTrait");

            ast.getClassByName("BatchClientBuilder").ifPresent(clazz -> {
                NodeList<ClassOrInterfaceType> implementedTypes = clazz.getImplementedTypes();
                boolean hasKeyCredentialTrait = implementedTypes.stream()
                    .filter(implementedType -> implementedType.getNameAsString().equals("AzureNamedKeyCredentialTrait"))
                    .findFirst()
                    .isPresent();
                if (!hasKeyCredentialTrait) {
                    clazz.addImplementedType("AzureNamedKeyCredentialTrait<BatchClientBuilder>");
                }
            });
        });

        // API 2025-06-01 returns diskReadIOps/diskWriteIOps as strings; 2026-10-01 returns numbers.
        // JsonReader.getString() accepts both, and Long.parseLong keeps exact int64 precision.
        customization.getClass("com.azure.compute.batch.models", "BatchPoolResourceStatistics").customizeAst(ast ->
            ast.getClassByName("BatchPoolResourceStatistics").ifPresent(clazz ->
                clazz.getMethodsByName("fromJson").forEach(method ->
                    method.findAll(AssignExpr.class).forEach(assign -> {
                        String target = assign.getTarget().toString();
                        if ("diskReadIops".equals(target) || "diskWriteIops".equals(target)) {
                            assign.setValue(StaticJavaParser.parseExpression("Long.parseLong(reader.getString())"));
                        }
                    }))));
    }
}
