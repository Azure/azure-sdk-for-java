package com.azure.spring.data.cosmos.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CosmosNonUniqueExceptionTest {

    @Test
    void testExceptionMessage() {
        String message = "Non-unique exception occurred";
        CosmosNonUniqueException exception = new CosmosNonUniqueException(message);
        assertEquals(message, exception.getMessage());
    }
}
