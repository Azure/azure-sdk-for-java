package com.microsoft.azure.cognitiveservices.language.textanalytics;

import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.HttpRequest;
import com.azure.common.http.ProxyOptions;
import com.azure.common.http.ProxyOptions.Type;
import com.azure.common.http.policy.HttpPipelinePolicy;
import com.microsoft.azure.cognitiveservices.language.textanalytics.implementation.TextAnalyticsClientImpl;
import com.microsoft.azure.cognitiveservices.language.textanalytics.models.LanguageBatchResult;
import com.microsoft.azure.cognitiveservices.language.textanalytics.models.LanguageInput;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class TextAnalyticsTests {

    private static TextAnalyticsClient client;

    @BeforeClass
    public static void setup() {
        client = new TextAnalyticsClientImpl(new HttpPipeline(
            HttpClient.createDefault().proxy(() -> new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))),
            (HttpPipelinePolicy) (context, next) -> {
                HttpRequest newRequest = context.httpRequest().withHeader("Ocp-Apim-Subscription-Key", System.getenv("SUBSCRIPTION_KEY"));
                context.withHttpRequest(newRequest);
                return next.process();
            }))
        .withEndpoint(System.getenv("ENDPOINT"));
    }

    @Test
    public void canDetectLanguage() {
        List<LanguageInput> inputs = new ArrayList<>();
        inputs.add(new LanguageInput().withId("1").withText("This is a document written in English."));
        inputs.add(new LanguageInput().withId("2").withText("Este es un document escrito en Español."));
        inputs.add(new LanguageInput().withId("3").withText("这是一个用中文写的文件"));
        LanguageBatchResult result = client.detectLanguage(false, inputs);

        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.documents().size());
        Assert.assertEquals("English", result.documents().get(0).detectedLanguages().get(0).name());
        Assert.assertEquals("Spanish", result.documents().get(1).detectedLanguages().get(0).name());
        Assert.assertEquals("Chinese_Simplified", result.documents().get(2).detectedLanguages().get(0).name());
    }
}
