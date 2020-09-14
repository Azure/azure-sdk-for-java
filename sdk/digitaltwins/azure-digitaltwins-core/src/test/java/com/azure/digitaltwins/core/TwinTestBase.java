package com.azure.digitaltwins.core;

import org.junit.jupiter.api.Test;
import com.azure.core.http.HttpClient;

/**
 * This abstract test class defines all the tests that both the sync and async twin test classes need to implement.
 */
public abstract class TwinTestBase extends DigitalTwinsTestBase{

    @Test
    public abstract void DigitalTwins_Lifecycle(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);

    @Test
    public abstract void DigitalTwins_TwinNotExist_ThrowsNotFoundException(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);

}
