import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;


/**
 * This class contains the customization code to customize the AutoRest generated code for the Agents Client library
 * Reference: https://github.com/Azure/autorest.java/blob/main/customization-base/README.md
 */
public class AgentsCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        renameContainerMemoryLimitVariants(libraryCustomization, logger);
    }

    private void renameContainerMemoryLimitVariants(LibraryCustomization customization, Logger logger) {
        customization.getClass("com.azure.ai.agents.models", "ContainerMemoryLimit").customizeAst(ast -> ast.getEnumByName("ContainerMemoryLimit")
            .ifPresent(clazz -> clazz.getEntries().stream()
                .filter(entry -> "ONEG".equals(entry.getName().getIdentifier()))
                .forEach(entry -> entry.setName("ONE_G"))));

        customization.getClass("com.azure.ai.agents.models", "ContainerMemoryLimit").customizeAst(ast -> ast.getEnumByName("ContainerMemoryLimit")
            .ifPresent(clazz -> clazz.getEntries().stream()
                .filter(entry -> "FOURG".equals(entry.getName().getIdentifier()))
                .forEach(entry -> entry.setName("FOUR_G"))));

        customization.getClass("com.azure.ai.agents.models", "ContainerMemoryLimit").customizeAst(ast -> ast.getEnumByName("ContainerMemoryLimit")
            .ifPresent(clazz -> clazz.getEntries().stream()
                .filter(entry -> "ONE_SIXG".equals(entry.getName().getIdentifier()))
                .forEach(entry -> entry.setName("SIXTEEN_G"))));

        customization.getClass("com.azure.ai.agents.models", "ContainerMemoryLimit").customizeAst(ast -> ast.getEnumByName("ContainerMemoryLimit")
            .ifPresent(clazz -> clazz.getEntries().stream()
                .filter(entry -> "SIX_FOURG".equals(entry.getName().getIdentifier()))
                .forEach(entry -> entry.setName("SIXTY_FOUR_G"))));
    }
}
