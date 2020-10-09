// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StorageApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(
    locations = "classpath:application-test.properties")
public class StorageApplicationIT {

    @Autowired
    private MockMvc mvc;

    @Test
    public void testPostAndGetSuccess()
        throws Exception {
        String content = UUID.randomUUID().toString();

        mvc.perform(post("/blob")
            .contentType(MediaType.APPLICATION_JSON).content(content))
            .andExpect(status().isOk())
            .andExpect(content().string("file was updated"));

        mvc.perform(get("/blob")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(content));
    }
}
