// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.source;

import com.azure.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.azure.spring.data.gremlin.conversion.result.GremlinResultsReader;
import com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteral;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provider interface to obtain and store information from domain class.
 * For Vertex and Edge, they consist of id (String, Reserved), label (String, Reserved) and
 * a set of properties.
 * The property key should be String, and value can be one of String, number and boolean.
 *
 * @param <T> The type of domain.
 */
public interface GremlinSource<T> {

    /**
     * Set the property map of domain
     *
     * @param key The key of the property.
     * @param value The value of the property.
     */
    void setProperty(String key, Object value);

    /**
     * Get the id of domain
     *
     * @return the Optional of id
     */
    Optional<Object> getId();

    /**
     * Set the id of domain
     *
     * @param id The id of the domain object.
     */
    void setId(Object id);

    /**
     * Get the id Field of domain
     *
     * @return will never be null
     */
    Field getIdField();

    /**
     * Set the id of domain
     *
     * @param id The id field.
     */
    void setIdField(Field id);

    /**
     * Get the label of domain
     *
     * @return will never be null
     */
    @NonNull
    String getLabel();

    /**
     * Set the label of domain
     *
     * @param label The label of the domain object.
     */
    void setLabel(String label);

    /**
     * Get the Class type of domain
     *
     * @return will never be null
     */
    @NonNull
    Class<T> getDomainClass();

    /**
     * Get the properties of domain
     *
     * @return The properties map of the domain, and it will never be null.
     */
    Map<String, Object> getProperties();

    /**
     * do the real write from domain to GremlinSource
     *
     * @param domain The domain object which needed to be written to GremlinSource.
     * @param converter The entity converter.
     */
    void doGremlinSourceWrite(Object domain, MappingGremlinConverter converter);

    /**
     * do the real reading from Result to GremlinSource
     *
     * @param results The results retrieved from Gremlin server.
     */
    void doGremlinResultRead(List<Result> results);

    /**
     * do the real reading from GremlinSource to domain
     *
     * @param domainClass The class type of domain object.
     * @param converter The entity converter.
     *
     * @return The domain object read from gremlin source.
     */
    T doGremlinSourceRead(Class<T> domainClass, MappingGremlinConverter converter);

    /**
     * @return the GremlinScriptLiteral
     */
    GremlinScriptLiteral getGremlinScriptLiteral();

    /**
     * Set the script Strategy of GremlinSource
     *
     * @param script The script literal.
     */
    void setGremlinScriptStrategy(GremlinScriptLiteral script);

    /**
     * Set the SourceWriter of GremlinSource
     *
     * @param writer The source writer to use.
     */
    void setGremlinSourceWriter(GremlinSourceWriter<T> writer);

    /**
     * Set the ResultReader for reading data from Gremlin Result to GremlinSource
     *
     * @param reader The results reader to use.
     */
    void setGremlinResultReader(GremlinResultsReader reader);

    /**
     * Set the SourceReader for reading data from GremlinSource to domain
     *
     * @param reader The source reader to use.
     */
    void setGremlinSourceReader(GremlinSourceReader<T> reader);
}
