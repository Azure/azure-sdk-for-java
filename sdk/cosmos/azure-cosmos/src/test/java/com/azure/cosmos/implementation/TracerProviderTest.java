package com.azure.cosmos.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.changefeed.exceptions.PartitionNotFoundException;
import com.azure.cosmos.models.CosmosResponse;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertThrows;

public class TracerProviderTest {
    @Test(groups = { "unit" })
    public void startSpan() {
        Tracer tracerMock = Mockito.mock(Tracer.class);
        String methodName = "get item";
        String endpoint = "endpoint";
        String instance = "instance";
        Context context = new Context("foo", "bar");

        ArgumentCaptor<StartSpanOptions> optionsCaptor = ArgumentCaptor.forClass(StartSpanOptions.class);
        TracerProvider provider = new TracerProvider(tracerMock, false, false);
        provider.startSpan(methodName, instance, endpoint, context);
        verify(tracerMock, times(1)).start(eq(methodName), optionsCaptor.capture(), eq(context));

        assertThat(optionsCaptor.getValue().getSpanKind()).isEqualTo(SpanKind.CLIENT);

        Map<String, Object> attributes = optionsCaptor.getValue().getAttributes();

        assertThat(attributes.get("az.namespace")).isEqualTo("Microsoft.DocumentDB");
        assertThat(attributes.get("db.type")).isEqualTo("Cosmos");
        assertThat(attributes.get("db.url")).isEqualTo(endpoint);
        assertThat(attributes.get("db.statement")).isEqualTo(methodName);
        assertThat(attributes.get("db.instance")).isEqualTo(instance);
    }

    @Test(groups = { "unit" })
    public void endSpanSuccess() {
        Context sdkContext = new Context("span", new Object());
        reactor.util.context.Context reactorContext = TracerProvider.setContextInReactor(sdkContext);

        Tracer tracerMock = Mockito.mock(Tracer.class);
        TracerProvider provider = new TracerProvider(tracerMock, false, false);
        provider.endSpan(Signal.complete(reactorContext), 200);
        verify(tracerMock, times(1)).end(eq(200), isNull(),  eq(sdkContext));
    }

    @Test(groups = { "unit" })
    public void endSpanFailureNotCosmosException() {
        Context sdkContext = new Context("span", new Object());
        reactor.util.context.Context reactorContext = TracerProvider.setContextInReactor(sdkContext);

        Tracer tracerMock = Mockito.mock(Tracer.class);
        TracerProvider provider = new TracerProvider(tracerMock, false, false);
        Exception ex = new Exception("foo");
        provider.endSpan(Signal.error(ex, reactorContext), 500);
        verify(tracerMock, times(1)).end(eq(500), eq(ex),  eq(sdkContext));
    }

    @Test(groups = { "unit" })
    public void endSpanFailureCosmosException() {
        Context sdkContext = new Context("span", new Object());
        reactor.util.context.Context reactorContext = TracerProvider.setContextInReactor(sdkContext);

        Tracer tracerMock = Mockito.mock(Tracer.class);
        TracerProvider provider = new TracerProvider(tracerMock, false, false);
        Exception ex = new ServiceUnavailableException();
        provider.endSpan(Signal.error(ex, reactorContext), -1);
        verify(tracerMock, times(1)).end(eq(503), eq(ex),  eq(sdkContext));
    }

    @Test(groups = { "unit" })
    public void traceMonoPublisher() {
        Tracer tracerMock = Mockito.mock(Tracer.class);

        CosmosResponse<?> response = Mockito.mock(CosmosResponse.class);
        Context sdkContext = new Context("span", new Object());

        TracerProvider provider = new TracerProvider(tracerMock, false, false);
        AtomicBoolean closed = new AtomicBoolean(false);
        when(tracerMock.start(anyString(), any(StartSpanOptions.class), any(Context.class))).thenReturn(sdkContext);
        when(tracerMock.makeSpanCurrent(any())).thenReturn(() -> closed.set(true));
        when(response.getStatusCode()).thenReturn(412);

        provider.traceEnabledCosmosResponsePublisher(Mono.deferContextual(ctx -> {
                assertThat(TracerProvider.getContextFromReactorOrNull(ctx)).isSameAs(sdkContext);
                return Mono.just(response);
            }),
            Context.NONE, "methodName", "instance", "endpoint").block();

        ArgumentCaptor<StartSpanOptions> optionsCaptor = ArgumentCaptor.forClass(StartSpanOptions.class);
        verify(tracerMock, times(1)).start(anyString(), optionsCaptor.capture(), any(Context.class));
        verify(tracerMock, times(1)).makeSpanCurrent(eq(sdkContext));
        verify(tracerMock, times(1)).end(eq(412), any(), eq(sdkContext));
        assertThat(closed.get()).isTrue();
    }

    @Test(groups = { "unit" })
    public void traceMonoPublisherException() {
        Tracer tracerMock = Mockito.mock(Tracer.class);

        CosmosResponse<?> response = Mockito.mock(CosmosResponse.class);
        TracerProvider provider = new TracerProvider(tracerMock, false, false);
        AtomicBoolean closed = new AtomicBoolean(false);
        when(tracerMock.start(anyString(), any(StartSpanOptions.class), any(Context.class))).thenReturn(Context.NONE);
        when(tracerMock.makeSpanCurrent(any())).thenReturn(() -> closed.set(true));
        when(response.getStatusCode()).thenReturn(412);

        Exception ex = new BadRequestException("foo");

        assertThrows(
            CosmosException.class,
            () -> provider.traceEnabledCosmosResponsePublisher(Mono.error(ex),
            Context.NONE, "methodName", "instance", "endpoint").block());

        verify(tracerMock, times(1)).start(anyString(), any(StartSpanOptions.class), any(Context.class));
        verify(tracerMock, times(1)).makeSpanCurrent(any());
        verify(tracerMock, times(1)).end(eq(400), eq(ex), any());
        assertThat(closed.get()).isTrue();
    }

    @Test(groups = { "unit" })
    public void testSetGetReactorContext() {
        Context sdkContext = new Context("span", new Object());

        reactor.util.context.Context reactorContext =
            TracerProvider.setContextInReactor(sdkContext);

        assertThat(TracerProvider.getContextFromReactorOrNull(reactorContext)).isSameAs(sdkContext);
    }

    @Test(groups = { "unit" })
    public void traceFluxPropagation() {
        Tracer tracerMock = Mockito.mock(Tracer.class);

        CosmosResponse<?> response = Mockito.mock(CosmosResponse.class);
        Context sdkContext = new Context("span", new Object());

        TracerProvider provider = new TracerProvider(tracerMock, false, false);
        AtomicBoolean closed = new AtomicBoolean(false);
        when(tracerMock.makeSpanCurrent(any())).thenReturn(() -> closed.set(true));

        provider
            .runUnderSpanInContext(Flux.just(response))
            .contextWrite(TracerProvider.setContextInReactor(sdkContext))
            .blockLast();

        verify(tracerMock, times(1)).makeSpanCurrent(eq(sdkContext));
        assertThat(closed.get()).isTrue();
    }
}
