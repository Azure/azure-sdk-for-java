// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class RuntimeHintsUtils {

    private static final Log LOGGER = LogFactory.getLog(RuntimeHintsUtils.class);

    private RuntimeHintsUtils() {

    }

    @SuppressWarnings("rawtypes")
    public static List<Class> findTypes(String basePackage, ClassLoader classLoader) throws IOException {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(classLoader);
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        List<Class> candidates = new ArrayList<>();
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
            + resolveBasePackage(basePackage) + "/" + "**/*.class";
        Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);

                String className = metadataReader.getClassMetadata().getClassName();
                if (className.endsWith("1") || className.endsWith("2")) {
                    continue;
                }
                LOGGER.debug("Will register class: " + className);
                try {
                    Class e = Class.forName(className);
                    candidates.add(e);
                } catch (ClassNotFoundException ex) {
                    LOGGER.error("Class not found", ex);
                } catch (AssertionError e) {
                    LOGGER.error("Assertion error happens", e);
                }
            }
        }
        return candidates;
    }

    private static String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }
}
