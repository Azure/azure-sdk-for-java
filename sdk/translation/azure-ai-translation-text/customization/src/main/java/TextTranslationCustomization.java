import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.slf4j.Logger;

/**
 * This class contains the customization code to customize the TypeSpec generated code for Text Translation.
 */
public class TextTranslationCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the TextTranslationClientBuilder class");
        customization.getClass("com.azure.ai.translation.text", "TextTranslationClientBuilder").customizeAst(ast -> {
            // add KeyCredentialTrait and TokenCredentialTrait imports
            ast.addImport("com.azure.core.client.traits.KeyCredentialTrait");
            ast.addImport("com.azure.core.client.traits.TokenCredentialTrait");

            ast.getClassByName("TextTranslationClientBuilder").ifPresent(clazz -> {
                NodeList<ClassOrInterfaceType> implementedTypes = clazz.getImplementedTypes();
                boolean hasKeyCredentialTrait = implementedTypes.stream()
                    .filter(implementedType -> implementedType.getNameAsString().equals("KeyCredentialTrait"))
                    .findFirst()
                    .isPresent();
                if (!hasKeyCredentialTrait) {
                    clazz.addImplementedType("KeyCredentialTrait<TextTranslationClientBuilder>");
                }

                boolean hasTokenCredentialTrait = implementedTypes.stream()
                    .filter(implementedType -> implementedType.getNameAsString().equals("TokenCredentialTrait"))
                    .findFirst()
                    .isPresent();
                if (!hasTokenCredentialTrait) {
                    clazz.addImplementedType("TokenCredentialTrait<TextTranslationClientBuilder>");
                }
            });
        });

        logger.info("Customizing the TextTranslationServiceVersion enum - removing V3_0");
        customization.getClass("com.azure.ai.translation.text", "TextTranslationServiceVersion")
            .customizeAst(ast -> ast.getEnumByName("TextTranslationServiceVersion")
                .ifPresent(enumDeclaration -> {
                    enumDeclaration.getEntries().stream()
                        .filter(entry -> entry.getNameAsString().equals("V3_0"))
                        .findFirst()
                        .ifPresent(enumConstant -> enumConstant.remove());
                }
            )
        );

        logger.info("Fixing ReturnType.COLLECTION to ReturnType.SINGLE for List return types");
        customization.getClass("com.azure.ai.translation.text", "TextTranslationAsyncClient")
            .customizeAst(ast -> ast.getClassByName("TextTranslationAsyncClient").ifPresent(clazz -> {
                clazz.getMethods().forEach(method -> {
                    method.getAnnotations().forEach(annotation -> {
                        if (annotation.getNameAsString().equals("ServiceMethod")) {
                            annotation.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                                if (pair.getNameAsString().equals("returns") 
                                    && pair.getValue().toString().contains("COLLECTION")) {
                                    pair.setValue("ReturnType.SINGLE");
                                }
                            });
                        }
                    });
                });
            }));

        customization.getClass("com.azure.ai.translation.text", "TextTranslationClient")
            .customizeAst(ast -> ast.getClassByName("TextTranslationClient").ifPresent(clazz -> {
                clazz.getMethods().forEach(method -> {
                    method.getAnnotations().forEach(annotation -> {
                        if (annotation.getNameAsString().equals("ServiceMethod")) {
                            annotation.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                                if (pair.getNameAsString().equals("returns") 
                                    && pair.getValue().toString().contains("COLLECTION")) {
                                    pair.setValue("ReturnType.SINGLE");
                                }
                            });
                        }
                    });
                });
            }));
    }
}
