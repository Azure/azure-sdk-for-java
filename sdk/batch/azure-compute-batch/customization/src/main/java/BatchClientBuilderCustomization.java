import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;

public class BatchClientBuilderCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization) {
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
