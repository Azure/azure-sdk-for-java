//package com.azure.cosmos;
//import com.azure.cosmos.models.CosmosAsyncDatabaseResponse;
//import com.azure.cosmos.models.CosmosDatabaseProperties;
//import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//import io.opentelemetry.OpenTelemetry;
//import io.opentelemetry.context.Scope;
//import io.opentelemetry.exporters.jaeger.JaegerGrpcSpanExporter;
//import io.opentelemetry.sdk.trace.TracerSdkFactory;
//import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
//import io.opentelemetry.trace.Span;
//import io.opentelemetry.trace.Tracer;
//import reactor.core.publisher.Mono;
//import reactor.util.context.Context;
//
//import java.util.logging.Logger;
//
//import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
//import static java.util.logging.Logger.getLogger;
//
//public class CosmosTracingSample {
//    final static String ENDPOINT = "";
//    final static String KEY = "";
//    private static final Logger LOGGER = getLogger("Sample");
//    private static final Tracer TRACER;
//    private static final TracerSdkFactory TRACER_SDK_PROVIDER;
//    static {
//        TRACER_SDK_PROVIDER = configureOpenTelemetryAndJaegerExporter();
//        TRACER = TRACER_SDK_PROVIDER.get("Sample1");
//    }
//    public static void main(String[] args) {
//        doAppConfigClientWork();
//        TRACER_SDK_PROVIDER.shutdown();
//        LOGGER.info("=== Tracer Shutdown  ===");
//    }
//
//    private static void doAppConfigClientWork() {
//        CosmosAsyncClient client = new CosmosClientBuilder()
//            .endpoint(ENDPOINT)
//            .key(KEY)
//            .buildAsyncClient();
//
//        LOGGER.info("=== Start user scoped span  ===");
//
//        Span span = TRACER.spanBuilder("user-parent-span").startSpan();
//        try (final Scope scope = TRACER.withSpan(span)) {
//            Context traceContext = Context.of(PARENT_SPAN_KEY, TRACER.getCurrentSpan());
//
//            System.out.println("CosmosTracingSample.doCosmosClientWork "+scope);
//            //  Create database if not exists
//            Mono<CosmosAsyncDatabaseResponse> databaseIfNotExists = client.createDatabase(new CosmosDatabaseProperties("passengers"))
//                .subscriberContext(traceContext);
//            databaseIfNotExists.flatMap(databaseResponse -> {
//                CosmosAsyncDatabase database = databaseResponse.getDatabase();
//                System.out.println("Checking database... " + database.getId() + " completed!\n");
//                return Mono.empty();
//            }).block();
//        }     finally {
//            span.end();
//            client.close();
//        }
//
//    }
////    static TracerSdkProvider configureOpenTelemetryAndJaegerExporter() {
////        // logger.info("=== Runing With JaegerExporter ===");
////        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 14250).usePlaintext().build();
////        JaegerGrpcSpanExporter exporter = JaegerGrpcSpanExporter.newBuilder()
////            .setChannel(channel)
////            .setServiceName("Sample")
////            .setDeadlineMs(Long.MIN_VALUE)
////            .build();
////        TracerSdkProvider tracerSdkFactory = (TracerSdkProvider) OpenTelemetry.getTracerProvider();
////        tracerSdkFactory.addSpanProcessor(SimpleSpansProcessor.newBuilder(exporter).build());
////        return tracerSdkFactory;
////    }
//    static TracerSdkFactory configureOpenTelemetryAndJaegerExporter() {
//        // logger.info("=== Runing With JaegerExporter ===");
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 14250).usePlaintext().build();
//        JaegerGrpcSpanExporter exporter = JaegerGrpcSpanExporter.newBuilder()
//            .setChannel(channel)
//            .setServiceName("Sample1")
//            .build();
//        TracerSdkFactory tracerSdkFactory = (TracerSdkFactory) OpenTelemetry.getTracerFactory();
//        tracerSdkFactory.addSpanProcessor(SimpleSpansProcessor.newBuilder(exporter).build());
//        return tracerSdkFactory;
//    }
//}
