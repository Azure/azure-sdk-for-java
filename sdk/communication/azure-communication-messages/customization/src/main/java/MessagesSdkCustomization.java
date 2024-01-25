import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.slf4j.Logger;

public class MessagesSdkCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        logger.info("Customizing the JobRouterAdministrationClientBuilder class");
        PackageCustomization packageCustomization = libraryCustomization.getPackage("com.azure.communication.messages");

        ClassCustomization notificationMessagesClientBuilderCustomization = packageCustomization.getClass("NotificationMessagesClientBuilder");
        addAuthTrait(notificationMessagesClientBuilderCustomization);

        ClassCustomization messageTemplateClientBuilderCustomization = packageCustomization.getClass("MessageTemplateClientBuilder");
        addAuthTrait(messageTemplateClientBuilderCustomization);
    }

    private void addAuthTrait(ClassCustomization classCustomization) {
        classCustomization.addImports("com.azure.core.client.traits.TokenCredentialTrait");
        classCustomization.addImports("com.azure.core.client.traits.KeyCredentialTrait");
        classCustomization.addImports("com.azure.core.client.traits.ConnectionStringTrait");
        classCustomization.customizeAst(compilationUnit -> {
            compilationUnit.getClassByName(classCustomization.getClassName()).ifPresent(builderClass -> {
                ClassOrInterfaceDeclaration clientBuilderClass = builderClass.asClassOrInterfaceDeclaration();
                NodeList<ClassOrInterfaceType> implementedTypes = clientBuilderClass.getImplementedTypes();
                boolean hasTokenCredentialTrait = implementedTypes.stream()
                    .anyMatch(implementedType -> implementedType.getNameAsString().equals("TokenCredentialTrait"));
                if (!hasTokenCredentialTrait) {
                    clientBuilderClass
                        .addImplementedType(String.format("TokenCredentialTrait<%s>", classCustomization.getClassName()));
                }

                boolean hasKeyCredentialTrait = implementedTypes.stream()
                    .anyMatch(implementedType -> implementedType.getNameAsString().equals("KeyCredentialTrait"));
                if (!hasKeyCredentialTrait) {
                    clientBuilderClass
                        .addImplementedType(String.format("KeyCredentialTrait<%s>", classCustomization.getClassName()));
                }

                boolean hasConnectionStringTrait = implementedTypes.stream()
                    .anyMatch(implementedType -> implementedType.getNameAsString().equals("ConnectionStringTrait"));
                if (!hasConnectionStringTrait) {
                    clientBuilderClass
                        .addImplementedType(String.format("ConnectionStringTrait<%s>", classCustomization.getClassName()));
                }
            });
        });
    }
}
