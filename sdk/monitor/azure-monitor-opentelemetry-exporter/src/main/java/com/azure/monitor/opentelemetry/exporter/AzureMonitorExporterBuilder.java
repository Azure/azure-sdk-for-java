// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.LogDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.MetricDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.SpanDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.heartbeat.HeartbeatExporter;
import com.azure.monitor.opentelemetry.exporter.implementation.localstorage.LocalStorageStats;
import com.azure.monitor.opentelemetry.exporter.implementation.localstorage.LocalStorageTelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipeline;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.TempDirs;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.VersionGenerator;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * This class provides a fluent builder API to instantiate {@link AzureMonitorTraceExporter} that
 * implements {@link SpanExporter} interface defined by OpenTelemetry API specification.
 */
public final class AzureMonitorExporterBuilder {

    private static final ClientLogger LOGGER = new ClientLogger(AzureMonitorExporterBuilder.class);

    private static final String APPLICATIONINSIGHTS_CONNECTION_STRING =
        "APPLICATIONINSIGHTS_CONNECTION_STRING";
    private static final String APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE =
        "https://monitor.azure.com//.default";

    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-monitor-opentelemetry-exporter.properties");

    private ConnectionString connectionString;
    private TokenCredential credential;

    // suppress warnings is needed in ApplicationInsights-Java repo, can be removed when upstreaming
    @SuppressWarnings({"UnusedVariable", "FieldCanBeLocal"})
    private AzureMonitorExporterServiceVersion serviceVersion;

    private HttpPipeline httpPipeline;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private RetryPolicy retryPolicy;
    private final List<HttpPipelinePolicy> httpPipelinePolicies = new ArrayList<>();

    private Configuration configuration = Configuration.getGlobalConfiguration();
    private ClientOptions clientOptions;

    /**
     * Creates an instance of {@link AzureMonitorExporterBuilder}.
     */
    public AzureMonitorExporterBuilder() {
    }

    /**
     * Sets the HTTP pipeline to use for the service client. If {@code httpPipeline} is set, all other
     * settings are ignored.
     *
     * @param httpPipeline The HTTP pipeline to use for sending service requests and receiving
     * responses.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder httpPipeline(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param httpClient The HTTP client to use for requests.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p>If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
     *
     * @param httpLogOptions The logging configuration to use when sending and receiving HTTP
     * requests/responses.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     *
     * <p>The default retry policy will be used if not provided to build {@link
     * AzureMonitorExporterBuilder} .
     *
     * @param retryPolicy user's retry policy applied to each request.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder retryPolicy(RetryPolicy retryPolicy) {
        // TODO (trask) revisit this when we add local storage / retry
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
     *
     * @param httpPipelinePolicy a policy to be added to the http pipeline.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public AzureMonitorExporterBuilder addHttpPipelinePolicy(HttpPipelinePolicy httpPipelinePolicy) {
        httpPipelinePolicies.add(
            Objects.requireNonNull(httpPipelinePolicy, "'policy' cannot be null."));
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * <p>The default configuration store is a clone of the {@link
     * Configuration#getGlobalConfiguration() global configuration store}, use {@link
     * Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the client options such as application ID and custom headers to set on a request.
     *
     * @param clientOptions The client options.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Sets the connection string to use for exporting telemetry events to Azure Monitor.
     *
     * @param connectionString The connection string for the Azure Monitor resource.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     * @throws NullPointerException If the connection string is {@code null}.
     * @throws IllegalArgumentException If the connection string is invalid.
     */
    public AzureMonitorExporterBuilder connectionString(String connectionString) {
        this.connectionString = ConnectionString.parse(connectionString);
        return this;
    }

    /**
     * Sets the Azure Monitor service version.
     *
     * @param serviceVersion The Azure Monitor service version.
     * @return The update {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder serviceVersion(
        AzureMonitorExporterServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Sets the token credential required for authentication with the ingestion endpoint service.
     *
     * @param credential The Azure Identity TokenCredential.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    public AzureMonitorExporterBuilder credential(TokenCredential credential) {
        this.credential = credential;
        return this;
    }

    /**
     * Creates an {@link AzureMonitorTraceExporter} based on the options set in the builder. This
     * exporter is an implementation of OpenTelemetry {@link SpanExporter}.
     *
     * @return An instance of {@link AzureMonitorTraceExporter}.
     * @throws NullPointerException if the connection string is not set on this builder or if the
     * environment variable "APPLICATIONINSIGHTS_CONNECTION_STRING" is not set.
     */
    public SpanExporter buildTraceExporter() {
        SpanDataMapper mapper =
            new SpanDataMapper(true, this::populateDefaults, (event, instrumentationName) -> false);

        return new AzureMonitorTraceExporter(mapper, initExporterBuilder());
    }

    /**
     * Creates an {@link AzureMonitorMetricExporter} based on the options set in the builder. This
     * exporter is an implementation of OpenTelemetry {@link MetricExporter}.
     *
     * <p>When a new {@link MetricExporter} is created, it will automatically start {@link
     * HeartbeatExporter}.
     *
     * @return An instance of {@link AzureMonitorMetricExporter}.
     * @throws NullPointerException if the connection string is not set on this builder or if the
     * environment variable "APPLICATIONINSIGHTS_CONNECTION_STRING" is not set.
     */
    public MetricExporter buildMetricExporter() {
        TelemetryItemExporter telemetryItemExporter = initExporterBuilder();
        HeartbeatExporter.start(
            MINUTES.toSeconds(15), this::populateDefaults, telemetryItemExporter::send);
        return new AzureMonitorMetricExporter(
            new MetricDataMapper(this::populateDefaults, true), telemetryItemExporter);
    }

    /**
     * Creates an {@link AzureMonitorLogRecordExporter} based on the options set in the builder. This
     * exporter is an implementation of OpenTelemetry {@link LogRecordExporter}.
     *
     * @return An instance of {@link AzureMonitorLogRecordExporter}.
     * @throws NullPointerException if the connection string is not set on this builder or if the
     * environment variable "APPLICATIONINSIGHTS_CONNECTION_STRING" is not set.
     */
    public LogRecordExporter buildLogRecordExporter() {
        return new AzureMonitorLogRecordExporter(
            new LogDataMapper(true, false, this::populateDefaults), initExporterBuilder());
    }

    private TelemetryItemExporter initExporterBuilder() {
        if (connectionString == null) {
            // if connection string is not set, try loading from configuration
            Configuration configuration = Configuration.getGlobalConfiguration();
            connectionString(configuration.get(APPLICATIONINSIGHTS_CONNECTION_STRING));
        }

        Objects.requireNonNull(connectionString, "'connectionString' cannot be null");

        if (this.credential != null) {
            // Add authentication policy to HttpPipeline
            BearerTokenAuthenticationPolicy authenticationPolicy =
                new BearerTokenAuthenticationPolicy(
                    this.credential, APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE);
            httpPipelinePolicies.add(authenticationPolicy);
        }

        if (httpPipeline == null) {
            httpPipeline = createHttpPipeline();
        }

        TelemetryPipeline pipeline = new TelemetryPipeline(httpPipeline);

        File tempDir =
            TempDirs.getApplicationInsightsTempDir(
                LOGGER,
                "Telemetry will not be stored to disk and retried later"
                    + " on sporadic network failures");

        TelemetryItemExporter telemetryItemExporter;
        if (tempDir != null) {
            telemetryItemExporter =
                new TelemetryItemExporter(
                    pipeline,
                    new LocalStorageTelemetryPipelineListener(
                        50, // default to 50MB
                        TempDirs.getSubDir(tempDir, "telemetry"),
                        pipeline,
                        LocalStorageStats.noop(),
                        false));
        } else {
            telemetryItemExporter = new TelemetryItemExporter(pipeline, TelemetryPipelineListener.noop());
        }
        return telemetryItemExporter;
    }

    private HttpPipeline createHttpPipeline() {
        Configuration buildConfiguration =
            (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;
        if (httpLogOptions == null) {
            httpLogOptions = new HttpLogOptions();
        }

        if (clientOptions == null) {
            clientOptions = new ClientOptions();
        }
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = PROPERTIES.getOrDefault("name", "UnknownName");
        String clientVersion = PROPERTIES.getOrDefault("version", "UnknownVersion");

        String applicationId = CoreUtils.getApplicationId(clientOptions, httpLogOptions);

        policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, buildConfiguration));
        policies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);
        policies.add(new CookiePolicy());
        policies.addAll(this.httpPipelinePolicies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));
        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
                .tracer(new NoopTracer())
            .build();
    }

    void populateDefaults(AbstractTelemetryBuilder builder, Resource resource) {
        builder.setConnectionString(connectionString);
        builder.addTag(
            ContextTagKeys.AI_INTERNAL_SDK_VERSION.toString(), VersionGenerator.getSdkVersion());
        ResourceParser.updateRoleNameAndInstance(builder, resource, configuration);
    }
}
