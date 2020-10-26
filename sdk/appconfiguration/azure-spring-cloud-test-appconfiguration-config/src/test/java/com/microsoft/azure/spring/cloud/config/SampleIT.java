package com.microsoft.azure.spring.cloud.config;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class SampleIT {

    private final Logger log = LoggerFactory.getLogger(SampleIT.class);
    @Test
    public void sampleTest() {
        log.info("test log info.");
        assertThat(true).isEqualTo(true);
    }
}
