// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.script;

import com.azure.spring.data.gremlin.common.domain.Person;
import com.azure.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceEdge;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import com.azure.spring.data.gremlin.mapping.GremlinMappingContext;
import com.azure.spring.data.gremlin.repository.support.GremlinEntityInformation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GremlinScriptLiteralVertexUnitTest {

    private MappingGremlinConverter converter;
    private GremlinMappingContext mappingContext;
    private GremlinSource<Person> gremlinSource;
    private GremlinScriptLiteralVertex scriptLiteralVertex;
    private AutoCloseable closeable;

    @Mock
    private ApplicationContext applicationContext;

    @BeforeEach
    public void setup() {
        this.closeable = MockitoAnnotations.openMocks(this);
        this.mappingContext = new GremlinMappingContext();
        this.mappingContext.setApplicationContext(this.applicationContext);
        this.mappingContext.afterPropertiesSet();
        this.mappingContext.getPersistentEntity(Person.class);
        this.converter = new MappingGremlinConverter(this.mappingContext);

        final Person person = new Person("123", "bill");
        final GremlinEntityInformation<Person, String> info = new GremlinEntityInformation<>(Person.class);
        this.gremlinSource = info.createGremlinSource();
        this.scriptLiteralVertex = new GremlinScriptLiteralVertex();
        this.converter.write(person, gremlinSource);
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }


    @Test
    public void testGenerateCountScript() {
        final List<String> queryList = scriptLiteralVertex.generateCountScript(gremlinSource);
        Assertions.assertEquals(queryList.get(0), "g.V()");
    }

    @Test
    public void testGenerateFindByIdScript() {
        final List<String> queryList = scriptLiteralVertex.generateFindByIdScript(gremlinSource);
        Assertions.assertEquals(queryList.get(0), "g.V().hasId('123')");
    }

    @Test
    public void testGenerateFindAllScript() {
        final List<String> queryList = scriptLiteralVertex.generateFindAllScript(gremlinSource);
        Assertions.assertEquals(queryList.get(0), "g.V().has(label, 'label-person')"
            + ".has('_classname', 'com.azure.spring.data.gremlin.common.domain.Person')");
    }

    @Test
    public void testGenerateInsertScript() {
        final List<String> queryList = scriptLiteralVertex.generateInsertScript(gremlinSource);
        Assertions.assertEquals(queryList.get(0), "g.addV('label-person').property(id, '123').property('name', 'bill')"
            + ".property('_classname', 'com.azure.spring.data.gremlin.common.domain.Person')");
    }

    @Test
    public void testGenerateUpdateScript() {
        final List<String> queryList = scriptLiteralVertex.generateUpdateScript(gremlinSource);
        Assertions.assertEquals(queryList.get(0), "g.V('123').property('name', 'bill')"
            + ".property('_classname', 'com.azure.spring.data.gremlin.common.domain.Person')");
    }

    @Test
    public void testGenerateDeleteByIdScript() {
        final List<String> queryList = scriptLiteralVertex.generateDeleteByIdScript(gremlinSource);
        Assertions.assertEquals(queryList.get(0), "g.V().hasId('123').drop()");
    }

    @Test
    public void testGenerateDeleteAllScript() {
        final List<String> queryList = scriptLiteralVertex.generateDeleteAllScript();
        Assertions.assertEquals(queryList.get(0), "g.V().drop()");
    }

    @Test
    public void testGenerateDeleteAllByClassScript() {
        final List<String> queryList = new GremlinScriptLiteralVertex().generateDeleteAllByClassScript(gremlinSource);
        Assertions.assertEquals(queryList.get(0), "g.V().has(label, 'label-person').drop()");
    }

    @Test
    public void testInvalidDeleteAllByClassScript() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinScriptLiteralVertex().generateDeleteAllByClassScript(new GremlinSourceEdge<>()));
    }

    @Test
    public void testInvalidFindAllScript() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinScriptLiteralVertex().generateFindAllScript(new GremlinSourceEdge<>()));
    }

    @Test
    public void testInvalidDeleteById() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinScriptLiteralVertex().generateDeleteByIdScript(new GremlinSourceEdge<>()));
    }
}
