package com.azure.sdk.build.tool.mojo;

import com.azure.core.util.CoreUtils;
import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import com.azure.sdk.build.tool.ReportGenerator;
import com.azure.sdk.build.tool.Tools;
import com.azure.sdk.build.tool.models.BuildError;
import com.azure.sdk.build.tool.models.BuildErrorLevel;
import com.azure.sdk.build.tool.models.BuildReport;
import com.azure.sdk.build.tool.util.logging.Logger;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Azure SDK build tools Maven plugin Mojo for analyzing Maven configuration of an application to provide Azure
 * SDK-specific recommendations.
 */
@Mojo(name = "run",
        defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
        requiresDependencyCollection = ResolutionScope.RUNTIME,
        requiresDependencyResolution = ResolutionScope.RUNTIME)
public class AzureSdkMojo extends AbstractMojo {

    public static AzureSdkMojo MOJO;
    private static final Logger LOGGER = Logger.getInstance();
    private static final String APP_INSIGHTS_CONNECTION_STRING = "InstrumentationKey=1d377c0e-44f8-4d56-bee7-7f13a3fef594;IngestionEndpoint=https://centralus-2.in.applicationinsights.azure.com/;LiveEndpoint=https://centralus.livediagnostics.monitor.azure.com/ ";

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "validateAzureSdkBomUsed", defaultValue = "true")
    private boolean validateAzureSdkBomUsed;

    @Parameter(property = "validateNoDeprecatedMicrosoftLibraryUsed", defaultValue = "true")
    private boolean validateNoDeprecatedMicrosoftLibraryUsed;

    @Parameter(property = "validateBomVersionsAreUsed", defaultValue = "true")
    private boolean validateBomVersionsAreUsed;

    @Parameter(property = "validateNoBetaLibraryUsed", defaultValue = "true")
    private boolean validateNoBetaLibraryUsed;

    @Parameter(property = "validateNoBetaApiUsed", defaultValue = "true")
    private boolean validateNoBetaApiUsed;

    @Parameter(property = "reportFile", defaultValue = "")
    private String reportFile;

    @Parameter(property = "sendToMicrosoft", defaultValue = "true")
    private boolean sendToMicrosoft;

    private final BuildReport buildReport;

    /**
     * Creates an instance of Azure SDK build tool Mojo.
     */
    public AzureSdkMojo() {
        MOJO = this;
        this.buildReport = new BuildReport();
    }

    /**
     * Returns the build report.
     * @return The build report.
     */
    public BuildReport getReport() {
        return buildReport;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("========================================================================");
        getLog().info("= Running the Azure SDK Maven Build Tool                               =");
        getLog().info("========================================================================");

        Tools.getTools().forEach(Runnable::run);
        ReportGenerator reportGenerator = new ReportGenerator(buildReport);
        reportGenerator.generateReport();
        BuildReport report = reportGenerator.getReport();
        if (sendToMicrosoft) {
            sendReportToAppInsights(report);
        }

        StringBuilder sb = new StringBuilder("Build failure for the following reasons:\n");
        boolean hasErrors = false;
        for (BuildError error : report.getErrors()) {
            if (BuildErrorLevel.WARNING.equals(error.getLevel())) {
                getLog().warn(error.getMessage());
            } else if (BuildErrorLevel.ERROR.equals(error.getLevel())){
                hasErrors = true;
                sb.append(" - " + error.getMessage() + "\n");
            }
        }
        // we throw a single runtime exception encapsulating all failure messages into one
        if (hasErrors) {
            throw new RuntimeException(sb.toString());
        }
    }

    private void sendReportToAppInsights(BuildReport report) {
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            SpanExporter azureMonitorExporter = new AzureMonitorExporterBuilder()
                .connectionString(APP_INSIGHTS_CONNECTION_STRING)
                .addHttpPipelinePolicy((context, next) -> next.process()
                    .doAfterTerminate(() -> countDownLatch.countDown()))
                .buildTraceExporter();

            SpanProcessor processor = SimpleSpanProcessor.create(azureMonitorExporter);
            SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(processor)
                .build();

            String version = CoreUtils
                .getProperties("azure-sdk-build-tool.properties")
                .get("version");
            Tracer tracer = tracerProvider.get("AzureSDKMavenBuildTool", version);
            tracer.spanBuilder("azsdk-maven-build-tool")
                .setAttribute("build-report", report.getJsonReport())
                .setSpanKind(SpanKind.SERVER)
                .startSpan()
                .end();

            CompletableResultCode completionCode = processor.forceFlush().join(30, TimeUnit.SECONDS);
            if (!completionCode.isSuccess()) {
                getLog().warn("Failed to send report to Application Insights");
            }
            processor.close();
            countDownLatch.await(10, TimeUnit.SECONDS);
        } catch (Exception ex) {
            getLog().warn("Unable to send report to Application Insights. " + ex.getMessage());
        }
    }

    /**
     * Returns the Maven project.
     * @return The Maven project.
     */
    public MavenProject getProject() {
        return project;
    }

    /**
     * If this validation is enabled, build is configured to fail if Azure SDK BOM is not used. By default, this is
     * set to {@code true}.
     *
     * @return {@code true} if this validation is enabled.
     */
    public boolean isValidateAzureSdkBomUsed() {
        return validateAzureSdkBomUsed;
    }

    /**
     * If this validation is enabled, build will fail if the application uses deprecated Microsoft libraries. By
     * default, this is set to {@code true}.
     * @return {@code true} if validation is enabled.
     */
    public boolean isValidateNoDeprecatedMicrosoftLibraryUsed() {
        return validateNoDeprecatedMicrosoftLibraryUsed;
    }

    /**
     * If this validation is enabled, build will fail if the any dependency overrides the version used in Azure SDK
     * BOM. By default, this is set to {@code true}.
     *
     * @return {@code true} if this validation is enabled.
     */
    public boolean isValidateBomVersionsAreUsed() {
        return validateBomVersionsAreUsed;
    }

    /**
     * If this validation is enabled, build will fail if a beta (preview) version of Azure library is used. By
     * default, this is set to {@code true}.
     * @return {@code true} if this validation is enabled.
     */
    public boolean isValidateNoBetaLibraryUsed() {
        return validateNoBetaLibraryUsed;
    }

    /**
     * If this validation is enabled, build will fail if any method annotated with @Beta is called. By
     * default, this is set to {@code true}.
     * @return {@code true} if this validation is enabled.
     */
    public boolean isValidateNoBetaApiUsed() {
        return validateNoBetaApiUsed;
    }

    /**
     * The report file to which the build report is written to.
     * @return The report file.
     */
    public String getReportFile() {
        return reportFile;
    }
}
