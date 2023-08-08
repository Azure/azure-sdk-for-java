// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepositoryJsonBuilder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyNameException;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AdditionalSpringConfigurationMetadataTests {

    @Test
    void validatePropertyNames() {
        final List<String> invalidatePropertyNames = loadRepository()
            .getAllProperties()
            .values()
            .stream()
            .map(this::validatePropertyName)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        assertThat(invalidatePropertyNames)
            .describedAs("Names should be kebab-case ('-' separated), lowercase alpha-numeric characters and must start with a letter")
            .isEmpty();
    }

    private String validatePropertyName(ConfigurationMetadataProperty property) {
        try {
            ConfigurationPropertyName.of(property.getId());
        } catch (InvalidConfigurationPropertyNameException e) {
            String invalid = e.getInvalidCharacters()
                .stream()
                .map(this::quote)
                .collect(Collectors.joining(", "));
            return String.format("Property '%s' contains invalid character(s): %s", e.getName(), invalid);
        }
        return null;
    }

    private String quote(Character c) {
        return "'" + c + "'";
    }

    private ConfigurationMetadataRepository loadRepository() {
        try {
            return loadRepository(ConfigurationMetadataRepositoryJsonBuilder.create());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load metadata", ex);
        }
    }

    private ConfigurationMetadataRepository loadRepository(ConfigurationMetadataRepositoryJsonBuilder builder)
        throws IOException {
        ClassPathResource resource = new ClassPathResource("/META-INF/additional-spring-configuration-metadata.json");

        try (InputStream inputStream = resource.getInputStream()) {
            builder.withJsonResource(inputStream);
        }

        return builder.build();
    }
}
