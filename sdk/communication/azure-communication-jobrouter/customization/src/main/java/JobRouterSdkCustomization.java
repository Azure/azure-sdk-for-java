import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.slf4j.Logger;

/**
 * This class contains the customization code to customize the AutoRest generated code for App Configuration.
 */
public class JobRouterSdkCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {

        logger.info("Customizing the JobRouterAdministrationClientBuilder class");
        PackageCustomization packageCustomization = customization.getPackage("com.azure.communication.jobrouter");
        ClassCustomization classCustomizationForJobRouterAdministrationClientBuilder = packageCustomization.getClass("JobRouterAdministrationClientBuilder");

        addConnectionStringTrait(classCustomizationForJobRouterAdministrationClientBuilder);
        addKeyCredentialTrait(classCustomizationForJobRouterAdministrationClientBuilder);
        addTokenCredentialTrait(classCustomizationForJobRouterAdministrationClientBuilder);


        logger.info("Customizing the JobRouterClientBuilder class");
        ClassCustomization classCustomizationForJobRouterClientBuilder = packageCustomization.getClass("JobRouterClientBuilder");

        addConnectionStringTrait(classCustomizationForJobRouterClientBuilder);
        addKeyCredentialTrait(classCustomizationForJobRouterClientBuilder);
        addTokenCredentialTrait(classCustomizationForJobRouterClientBuilder);
    }

    private void addConnectionStringTrait(ClassCustomization classCustomization) {
        // add ConnectionString imports
        classCustomization.addImports("com.azure.core.client.traits.ConnectionStringTrait");

        // add ConnectionStringTrait<> as implemented type
        classCustomization.customizeAst(compilationUnit -> {
            ClassOrInterfaceDeclaration jobRouterAdministrationClientBuilderClass = compilationUnit.getClassByName(classCustomization.getClassName()).get().asClassOrInterfaceDeclaration();
            NodeList<ClassOrInterfaceType> implementedTypes = jobRouterAdministrationClientBuilderClass.getImplementedTypes();
            boolean hasConnectionStringTrait = implementedTypes.stream().filter(implementedType -> implementedType.getNameAsString().equals("ConnectionStringTrait")).findFirst().isPresent();
            if (!hasConnectionStringTrait) {
                jobRouterAdministrationClientBuilderClass.addImplementedType(String.format("ConnectionStringTrait<%s>", classCustomization.getClassName()));
            }
        });
    }

    private void addKeyCredentialTrait(ClassCustomization classCustomization) {
        // add KeyCredential imports
        classCustomization.addImports("com.azure.core.client.traits.KeyCredentialTrait");

        // add KeyCredentialTrait<> as implemented type
        classCustomization.customizeAst(compilationUnit -> {
            ClassOrInterfaceDeclaration jobRouterAdministrationClientBuilderClass = compilationUnit.getClassByName(classCustomization.getClassName()).get().asClassOrInterfaceDeclaration();
            NodeList<ClassOrInterfaceType> implementedTypes = jobRouterAdministrationClientBuilderClass.getImplementedTypes();
            boolean hasKeyCredentialTrait = implementedTypes.stream().filter(implementedType -> implementedType.getNameAsString().equals("KeyCredentialTrait")).findFirst().isPresent();
            if (!hasKeyCredentialTrait) {
                jobRouterAdministrationClientBuilderClass.addImplementedType(String.format("KeyCredentialTrait<%s>", classCustomization.getClassName()));
            }
        });
    }

    private void addTokenCredentialTrait(ClassCustomization classCustomization) {
        // add TokenCredential imports
        classCustomization.addImports("com.azure.core.client.traits.TokenCredentialTrait");

        // add TokenCredentialTrait<> as implemented type
        classCustomization.customizeAst(compilationUnit -> {
            ClassOrInterfaceDeclaration jobRouterAdministrationClientBuilderClass = compilationUnit.getClassByName(classCustomization.getClassName()).get().asClassOrInterfaceDeclaration();
            NodeList<ClassOrInterfaceType> implementedTypes = jobRouterAdministrationClientBuilderClass.getImplementedTypes();
            boolean hasTokenCredentialTrait = implementedTypes.stream().filter(implementedType -> implementedType.getNameAsString().equals("TokenCredentialTrait")).findFirst().isPresent();
            if (!hasTokenCredentialTrait) {
                jobRouterAdministrationClientBuilderClass.addImplementedType(String.format("TokenCredentialTrait<%s>", classCustomization.getClassName()));
            }
        });
    }
}
