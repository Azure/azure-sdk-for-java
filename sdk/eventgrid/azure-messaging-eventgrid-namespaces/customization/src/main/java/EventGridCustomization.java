import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.PropertyCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocSnippet;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.javaparser.StaticJavaParser.parseBlock;
import static com.github.javaparser.StaticJavaParser.parseExpression;

/**
 * This class contains the customization code to customize the AutoRest generated code for Event Grid.
 */
public class EventGridCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customizeEventGridClientImplImports(customization, logger);
    }


    public void customizeEventGridClientImplImports(LibraryCustomization customization, Logger logger) {

        Arrays.asList("com.azure.messaging.eventgrid.namespaces", "com.azure.messaging.eventgrid.namespaces.models").forEach(p -> {
            logger.info("Working on " + p);
            PackageCustomization packageCustomization = customization.getPackage(p);
            packageCustomization.listClasses().forEach(c -> {
                c.customizeAst(comp -> {
                    if (comp.getImports().removeIf(i -> i.getNameAsString().equals("com.azure.messaging.eventgrid.namespaces.models.CloudEvent"))) {
                        logger.info("Removed CloudEvent import from " + c.getClassName());
                        comp.addImport("com.azure.core.models.CloudEvent");
                    }
                });
            });
        });
        customization.getRawEditor().removeFile("src/main/java/com/azure/messaging/eventgrid/namespaces/models/PublishResult.java");
        customization.getRawEditor().removeFile("src/main/java/com/azure/messaging/eventgrid/namespaces/models/CloudEvent.java");

    }
}
