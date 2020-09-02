package com.azure.digitaltwins.core;

import com.azure.core.http.rest.PagedIterable;
import com.azure.digitaltwins.core.models.ModelData;
import org.junit.jupiter.api.Test;

public class SampleTest extends DigitalTwinsTestBase {

    private static DigitalTwinsClient client;

    @Override
    protected void beforeTest(){
        super.beforeTest();
        client = setupClient();
    }

    @Override
    protected  void afterTest()
    {
        super.afterTest();
    }

    private DigitalTwinsClient setupClient(){
        return getDigitalTwinsClientBuilder()
            .buildClient();
    }

    @Test
    public void ListTest(){
        PagedIterable<ModelData> models = client.listModels();

        // Process using the Stream interface by iterating over each page
        models
            // You can also subscribe to pages by specifying the preferred page size or the associated continuation token to start the processing from.
            .streamByPage()
            .forEach(page -> {
                System.out.println("Response headers status code is " + page.getStatusCode());
                page.getValue().forEach(item -> System.out.println("Model retrieved: " + item.getId()));
            });
    }
}
