package com.azure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.azure.core.test.TestBase;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest(classes = {Application.class}
    , webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ControllerTest extends TestBase {
//public class ControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void controller_should_return_ok() {
        String response = restTemplate.getForObject(Controller.URL, String.class);
        assertThat(response).isEqualTo("OK!");
    }

}
