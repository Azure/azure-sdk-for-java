package com.azure;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import com.azure.core.http.*;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.util.annotation.Nullable;

@SpringBootTest(
    classes = {Application.class, TelemetrySpringNativeTest.TestConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "applicationinsights.connection.string=InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEEF;IngestionEndpoint=https://test.in.applicationinsights.azure.com/;LiveEndpoint=https://test.livediagnostics.monitor.azure.com/"
    })
public class TelemetrySpringNativeTest {

    private static CountDownLatch countDownLatch;

    private static CustomValidationPolicy customValidationPolicy;

    @Autowired private TestRestTemplate restTemplate;
    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {

        @Bean
        HttpPipeline httpPipeline() {
            countDownLatch = new CountDownLatch(1);
            customValidationPolicy = new CustomValidationPolicy(countDownLatch);
            return getHttpPipeline(customValidationPolicy);
        }

        HttpPipeline getHttpPipeline(@Nullable HttpPipelinePolicy policy) {
            return new HttpPipelineBuilder()
                .httpClient(HttpClient.createDefault())
                .policies(policy)
                .build();
        }
    }

    @Test
    public void should_send_telemetry() throws InterruptedException, MalformedURLException {
        String response = restTemplate.getForObject(Controller.URL, String.class);

        countDownLatch.await(10, SECONDS);

        assertThat(customValidationPolicy.url)
            .isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));

        assertThat(customValidationPolicy.actualTelemetryItems.size()).isEqualTo(1);

        TelemetryItem telemetry = customValidationPolicy.actualTelemetryItems.get(0);
        assertThat(telemetry.getName()).isEqualTo("Request");

        assertThat(response).isEqualTo("OK!");
    }
}
