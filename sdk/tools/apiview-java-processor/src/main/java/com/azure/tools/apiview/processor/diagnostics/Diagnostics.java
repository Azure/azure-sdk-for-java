package com.azure.tools.apiview.processor.diagnostics;

import com.azure.tools.apiview.processor.diagnostics.rules.*;
import com.azure.tools.apiview.processor.diagnostics.rules.azure.*;
import com.azure.tools.apiview.processor.diagnostics.rules.clientcore.ClientCoreBuilderTraitsDiagnosticRule;
import com.azure.tools.apiview.processor.diagnostics.rules.clientcore.ClientCoreFluentSetterReturnTypeDiagnosticRule;
import com.azure.tools.apiview.processor.diagnostics.rules.clientcore.ExpandableEnumDiagnosticRule;
import com.azure.tools.apiview.processor.diagnostics.rules.general.*;
import com.azure.tools.apiview.processor.diagnostics.rules.utils.MiscUtils;
import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.Flavor;
import com.github.javaparser.ast.CompilationUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.azure.tools.apiview.processor.model.Flavor.*;

import static com.azure.tools.apiview.processor.diagnostics.rules.general.IllegalMethodNamesDiagnosticRule.Rule;
import static com.azure.tools.apiview.processor.diagnostics.rules.RequiredBuilderMethodsDiagnosticRule.DirectSubclassCheckFunction;
import static com.azure.tools.apiview.processor.diagnostics.rules.RequiredBuilderMethodsDiagnosticRule.ExactTypeNameCheckFunction;
import static com.azure.tools.apiview.processor.diagnostics.rules.RequiredBuilderMethodsDiagnosticRule.ParameterAllowedTypes;
import static com.azure.tools.apiview.processor.diagnostics.rules.general.BadAnnotationDiagnosticRule.BadAnnotation;

/**
 * The Diagnostics class is responsible for setting up and running all diagnostics rules against the provided
 * compilation units.
 */
public class Diagnostics {
    final List<DiagnosticRule> rules;

    public Diagnostics(APIListing apiListing) {
        rules = new ArrayList<>();

        System.out.println("  Setting up diagnostics...");

        Flavor flavor = Flavor.getFlavor(apiListing);

        // Special rules for com.azure or io.clientcore libraries only
        switch (flavor) {
            case AZURE: {
                System.out.println("    Applying com.azure specific diagnostics...");
                addAzureCoreDiagnostics();
                break;
            }
            case GENERIC: {
                System.out.println("    Applying io.clientcore specific diagnostics...");
                addClientCoreDiagnostics();
                break;
            }
            case UNKNOWN:
            default: {
                System.out.println("    Unknown library flavor...");
                break;
            }
        }

        System.out.println("    Applying general-purpose diagnostics...");

        // general rules applicable in all cases
        add(new UpperCaseEnumValuesDiagnosticRule());
        add(new ImportsDiagnosticRule("com.sun"));
        add(new IllegalPackageAPIExportsDiagnosticRule("implementation", "netty"));
        add(new NoPublicFieldsDiagnosticRule());
        add(new UpperCaseNamingDiagnosticRule("URL", "HTTP", "XML", "JSON", "SAS", "CPK", "API"));
        add(new ConsiderFinalClassDiagnosticRule());
        add(new IllegalMethodNamesDiagnosticRule(
                new Rule("Builder$", "tokenCredential"), // it should just be 'credential'
                new Rule("Builder$", "^set"),            // we shouldn't have setters in the builder
                new Rule("^isHas"),
                new Rule("^setHas")
        ));
        add(new MissingJavaDocDiagnosticRule());
        add(new MissingJavadocCodeSnippetsRule());
        add(new NoLocalesInJavadocUrlDiagnosticRule());
        add(new ModuleInfoDiagnosticRule());
        add(new BadAnnotationDiagnosticRule(
                new BadAnnotation("JacksonXmlRootElement",
                        "From the Jackson JavaDoc: \"NOTE! Since 2.4 this annotation is usually not necessary and " +
                                "you should use JsonRootName instead. About the only expected usage may be to have different " +
                                "root name for XML content than other formats.\"")
        ));
    }

    private void addAzureCoreDiagnostics() {
        add(new PackageNameDiagnosticRule(Pattern.compile("^" + AZURE.getPackagePrefix() + "(\\.[a-z0-9]+)+$")));
        add(new AzureCoreBuilderTraitsDiagnosticRule());
        add(new MissingAnnotationsDiagnosticRule(AZURE.getPackagePrefix()));
        add(new AzureCoreFluentSetterReturnTypeDiagnosticRule());
        add(new ServiceVersionDiagnosticRule());
        add(new ExpandableEnumDiagnosticRule("ExpandableStringEnum"));
        add(new MavenPackageAndDescriptionDiagnosticRule());
        add(new MissingEqualsAndHashCodeDiagnosticRule("(.+\\.)?models(\\..+)?"));

        // common APIs for all builders (below we will do rules for http or amqp builders)
        add(new RequiredBuilderMethodsDiagnosticRule(null)
            .addMethod("configuration", new ExactTypeNameCheckFunction("Configuration"))
            .addMethod("clientOptions", new ExactTypeNameCheckFunction("ClientOptions"))
            .addMethod("connectionString", new ExactTypeNameCheckFunction("String"))
            .addMethod("credential", new ExactTypeNameCheckFunction(new ParameterAllowedTypes("TokenCredential",
                    "AzureKeyCredential", "AzureSasCredential", "AzureNamedKeyCredential", "KeyCredential")))
            .addMethod("endpoint", new ExactTypeNameCheckFunction("String"))
            .addMethod("serviceVersion", m -> MiscUtils.checkMethodParameterTypeSuffix(m, "ServiceVersion")));
        add(new RequiredBuilderMethodsDiagnosticRule("amqp")
            .addMethod("proxyOptions", new ExactTypeNameCheckFunction("ProxyOptions"))
            .addMethod("retry", new ExactTypeNameCheckFunction("AmqpRetryOptions"))
            .addMethod("transportType", new DirectSubclassCheckFunction("AmqpTransportType")));
        add(new RequiredBuilderMethodsDiagnosticRule("http")
            .addMethod("addPolicy", new ExactTypeNameCheckFunction("HttpPipelinePolicy"))
            .addMethod("httpClient", new ExactTypeNameCheckFunction("HttpClient"))
            .addMethod("httpLogOptions", new ExactTypeNameCheckFunction("HttpLogOptions"))
            .addMethod("pipeline", new ExactTypeNameCheckFunction("HttpPipeline"))
            .addMethod("retryPolicy", new ExactTypeNameCheckFunction("RetryPolicy")));
    }

    private void addClientCoreDiagnostics() {
        add(new ClientCoreBuilderTraitsDiagnosticRule());
        add(new MissingAnnotationsDiagnosticRule(GENERIC.getPackagePrefix()));
        add(new ClientCoreFluentSetterReturnTypeDiagnosticRule());
        add(new ExpandableEnumDiagnosticRule("ExpandableEnum"));
        add(new MissingEqualsAndHashCodeDiagnosticRule("(.+\\.)?models(\\..+)?"));

        // common APIs for all builders (below we will do rules for http or amqp builders)
        add(new RequiredBuilderMethodsDiagnosticRule(null)
            .addMethod("endpoint", new ExactTypeNameCheckFunction("String"))
            .addMethod("configuration", new ExactTypeNameCheckFunction("Configuration"))
            .addMethod("credential", new ExactTypeNameCheckFunction(new ParameterAllowedTypes("KeyCredential")))
            .addMethod("addHttpPipelinePolicy", new ExactTypeNameCheckFunction("HttpPipelinePolicy"))
            .addMethod("httpClient", new ExactTypeNameCheckFunction("HttpClient"))
            .addMethod("httpLogOptions", new ExactTypeNameCheckFunction("HttpLogOptions"))
            .addMethod("httpPipeline", new ExactTypeNameCheckFunction("HttpPipeline"))
            .addMethod("httpRetryOptions", new ExactTypeNameCheckFunction("HttpRetryOptions"))
            .addMethod("httpRedirectOptions", new ExactTypeNameCheckFunction("HttpRedirectOptions"))
            .addMethod("proxyOptions", new ExactTypeNameCheckFunction("ProxyOptions")));
    }

    private void add(DiagnosticRule rule) {
        rules.add(rule);
    }

    /**
     * Runs any diagnostics that can be run against the single compilation unit that has been provided.
     */
    public void scanIndividual(CompilationUnit cu, APIListing listing) {
        // We do not scan compilation units that are missing any primary type (i.e. they are completely commented out).
        if (!cu.getPrimaryType().isPresent()) {
            return;
        }
        rules.forEach(rule -> rule.scanIndividual(cu, listing));
    }

    /**
     * Called once to allow for any full analysis to be performed after all individual scans have been completed.
     */
    public void scanFinal(APIListing listing) {
        rules.forEach(rule -> rule.scanFinal(listing));
    }
}
