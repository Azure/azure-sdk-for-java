import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.slf4j.Logger;

/**
 * This class contains the customization code to customize the AutoRest generated code for App Configuration.
 */
public class MyCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {

        logger.info("Customizing the JobRouterAdministrationClientBuilder class");
        PackageCustomization packageCustomization = customization.getPackage("com.azure.communication.jobrouter");
        ClassCustomization classCustomization = packageCustomization.getClass("JobRouterAdministrationClientBuilder");

        // add ConnectionString imports
        classCustomization.addImports("com.azure.core.client.traits.ConnectionStringTrait");

        // add ConnectionStringTrait<JobRouterAdministrationClientBuilder>
        classCustomization.customizeAst(compilationUnit -> {
            ClassOrInterfaceDeclaration jobRouterAdministrationClientBuilderClass = compilationUnit.getClassByName("JobRouterAdministrationClientBuilder").get().asClassOrInterfaceDeclaration();
            NodeList<ClassOrInterfaceType> implementedTypes = jobRouterAdministrationClientBuilderClass.getImplementedTypes();
            boolean hasConnectionStringTrait = implementedTypes.stream().filter(implementedType -> implementedType.getNameAsString().equals("ConnectionStringTrait")).findFirst().isPresent();
            if (!hasConnectionStringTrait) {
                jobRouterAdministrationClientBuilderClass.addImplementedType("ConnectionStringTrait<JobRouterAdministrationClientBuilder>");
            }
        });
    }
}
