package com.azure.core.tracing.opencensus;

import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.Context;
import static com.azure.core.util.tracing.Tracer.OPENCENSUS_SPAN_KEY;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.Secret;
import io.opencensus.common.Scope;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.samplers.Samplers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;

public class OpenCensusTracerIntegrationTest {

    @Mock
    private OpenCensusTracer opencensusTracer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        opencensusTracer = new OpenCensusTracer();
    }

    @BeforeClass
    public static void beforeClass() {
        // 1. Configure 100% sample rate, otherwise, few traces will be sampled.
        TraceConfig traceConfig = Tracing.getTraceConfig();
        TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
        traceConfig.updateActiveTraceParams(
            activeTraceParams.toBuilder().setSampler(
                Samplers.alwaysSample()).build());

        // 2. Get the global singleton Tracer object.
        Tracer tracer = Tracing.getTracer();

        // Instantiate a client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SecretClient client = new SecretClientBuilder()
            .endpoint("https://samvaitykv.vault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // 3. Create a scoped span, a scoped span will automatically end when closed.
        try (Scope scope = tracer.spanBuilder("main").startScopedSpan()) {
            System.out.println("About to do some busy work...");
            doWork(client, tracer);
        }
    }

    private static void doWork(SecretClient client, Tracer tracer) {
        Context traceContext = new Context(OPENCENSUS_SPAN_KEY, tracer.getCurrentSpan());
        // 4. Call Key vault Secret client method passing the current user parent tracing span
        client.setSecretWithResponse(new Secret("StorageAccountPassword", "password"), traceContext);
    }

    @Test
    public void testSomething() {
        // OpenCensusTracer openCensusTracer = new OpenCensusTracer();
        verify(opencensusTracer, times(1)).start("s", Context.NONE);
    }
}

