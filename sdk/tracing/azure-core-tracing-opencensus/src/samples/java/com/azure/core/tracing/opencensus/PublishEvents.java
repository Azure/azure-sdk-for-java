package com.azure.core.tracing.opencensus;

import com.azure.core.util.Context;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubAsyncProducer;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import io.opencensus.common.Scope;
import io.opencensus.exporter.trace.zipkin.ZipkinTraceExporter;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.samplers.Samplers;
import reactor.core.publisher.Flux;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to send a message to an Azure Event Hub with tracing support.
 */
public class PublishEvents {
    /**
     * Main method to invoke this demo on how to send a message to an Azure Event Hub
     * with trace spans exported to zipkin.
     *
     * Please refer to the  <a href=https://zipkin.io/pages/quickstart>Quickstart Zipkin</a>
     * for more documentation on using a zipkin exporter.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        ZipkinTraceExporter.createAndRegister("http://localhost:9411/api/v2/spans", "tracing-to-zipkin-service");

        TraceConfig traceConfig = Tracing.getTraceConfig();
        TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
        traceConfig.updateActiveTraceParams(activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build());

        Tracer tracer = Tracing.getTracer();
        String connectionString = "Endpoint=sb://eventdatamigrationns.servicebus.windows.net/;SharedAccessKeyName=samVPolicy;SharedAccessKey=t6fQstSM9lcH/51GJaS1XKXEnl3nWX1zcJJMviBadYI=;EntityPath=samv";


        try (Scope scope = tracer.spanBuilder("opencensus-span").startScopedSpan()) {
            // The connection string value can be obtained by:
            // 1. Going to your Event Hubs namespace in Azure Portal.
            // 2. Creating an Event Hub instance.
            // 3. Creating a "Shared access policy" for your Event Hub instance.
            // 4. Copying the connection string from the policy's properties.

            // Instantiate a client that will be used to call the service.
            EventHubAsyncClient client = new EventHubClientBuilder()
                .connectionString(connectionString)
                .buildAsyncClient();

            final String CONTENTS = "SSLorem ipsum dolor sit amet, consectetur adipiscing elit. Donec vehicula posuere lobortis. Aliquam finibus volutpat dolor, faucibus pellentesque ipsum bibendum vitae. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Ut sit amet urna hendrerit, dapibus justo a, sodales justo. Mauris finibus augue id pulvinar congue. Nam maximus luctus ipsum, at commodo ligula euismod ac. Phasellus vitae lacus sit amet diam porta placerat. \n"
            		+ "Ut sodales efficitur sapien ut posuere. Morbi sed tellus est. Proin eu erat purus. Proin massa nunc, condimentum id iaculis dignissim, consectetur et odio. Cras suscipit sem eu libero aliquam tincidunt. Nullam ut arcu suscipit, eleifend velit in, cursus libero. Ut eleifend facilisis odio sit amet feugiat. Phasellus at nunc sit amet elit sagittis commodo ac in nisi. Fusce vitae aliquam quam. Integer vel nibh euismod, tempus elit vitae, pharetra est. Duis vulputate enim a elementum dignissim. Morbi dictum enim id elit scelerisque, in elementum nulla pharetra. \n"
            		+ "Aenean aliquet aliquet condimentum. Proin dapibus dui id libero tempus feugiat. Sed commodo ligula a lectus mattis, vitae tincidunt velit auctor. Fusce quis semper dui. Phasellus eu efficitur sem. Ut non sem sit amet enim condimentum venenatis id dictum massa. Nullam sagittis lacus a neque sodales, et ultrices arcu mattis. Aliquam erat volutpat. \n"
            		+ "Aenean fringilla quam elit, id mattis purus vestibulum nec. Praesent porta eros in dapibus molestie. Vestibulum orci libero, tincidunt et turpis eget, condimentum lobortis enim. Fusce suscipit ante et mauris consequat cursus nec laoreet lorem. Maecenas in sollicitudin diam, non tincidunt purus. Nunc mauris purus, laoreet eget interdum vitae, placerat a sapien. In mi risus, blandit eu facilisis nec, molestie suscipit leo. Pellentesque molestie urna vitae dui faucibus bibendum. \n"
            		+ "Donec quis ipsum ultricies, imperdiet ex vel, scelerisque eros. Ut at urna arcu. Vestibulum rutrum odio dolor, vitae cursus nunc pulvinar vel. Donec accumsan sapien in malesuada tempor. Maecenas in condimentum eros. Sed vestibulum facilisis massa a iaculis. Etiam et nibh felis. Donec maximus, sem quis vestibulum gravida, turpis risus congue dolor, pharetra tincidunt lectus nisi at velit.";
            Context context = new Context("opencensus-span", tracer.getCurrentSpan());
            final int count = 2;
            final byte[] contents = CONTENTS.getBytes(UTF_8);
            final Flux<EventData> testData = Flux.range(0, count).flatMap(number -> {
            	final EventData data = new EventData(contents, context);
            	return Flux.just(data);
            });

            // Create a producer. This overload of `createProducer` does not accept any arguments. Consequently, events
            // sent from this producer are load balanced between all available partitions in the Event Hub instance.
            EventHubAsyncProducer producer = client.createProducer();

            // Send those events. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
            // event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
            // the event.
            producer.send(testData).block();
            try {
                producer.close();
            } catch (IOException e) {
                System.err.println("Error encountered while closing producer: " + e.toString());
            }

            client.close();
        }

        Tracing.getExportComponent().shutdown();
    }
}

