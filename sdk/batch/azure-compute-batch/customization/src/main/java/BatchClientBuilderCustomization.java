import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.ast.NodeList;
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
    }
}
