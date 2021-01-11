// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SpringBootApplication
@RestController
public class Application implements CommandLineRunner {

    @Autowired
    private ConfigurableEnvironment environment;


    @Value("${spring.cosmos.db.key:local}")
    private String cosmosDBkey;

    private static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping("hello")
    public String hello() {
        try {
            return mapper.writeValueAsString(System.getenv());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Some error happens";
        }
    }

    @GetMapping("get")
    public String get() {
        return cosmosDBkey;
    }

    @GetMapping("env/{key}")
    public String env(@PathVariable String key) {
        final String property = environment.getProperty(key);
        return property;
    }

    @GetMapping("list")
    public String list() {
        final List list = new ArrayList();
        final MutablePropertySources propertySources = this.environment.getPropertySources();
        final Iterator<PropertySource<?>> iterator = propertySources.iterator();
        while (iterator.hasNext()) {
            final PropertySource<?> next = iterator.next();
            list.add(next.getName());
        }
        return JSON.toJSONString(list);
    }

    @GetMapping("getSpecificProperty/{ps}/{key}")
    public String getSpecificProperty(@PathVariable String ps, @PathVariable String key) {
        final MutablePropertySources propertySources = this.environment.getPropertySources();
        final PropertySource<?> propertySource = propertySources.get(ps);
        if (propertySource != null) {
            final Object property = propertySource.getProperty(key);
            return property == null ? null : property.toString();
        } else {
            return null;
        }
    }

    public void run(String... varl) throws Exception {
        System.out.println("property your-property-name value is: " + cosmosDBkey);
    }

}
