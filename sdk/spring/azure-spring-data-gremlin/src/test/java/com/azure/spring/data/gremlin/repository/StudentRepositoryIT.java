// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository;

import com.azure.spring.data.gremlin.common.GremlinEntityType;
import com.azure.spring.data.gremlin.common.TestRepositoryConfiguration;
import com.azure.spring.data.gremlin.common.domain.Student;
import com.azure.spring.data.gremlin.common.repository.StudentRepository;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfiguration.class)
public class StudentRepositoryIT {

    private static final Long ID_0 = 12349274637234L;
    private static final Long ID_1 = 1L;
    private static final Long ID_2 = 2L;
    private static final Long NO_EXIST_ID = -1L;

    private static final String NAME_0 = "name-0";
    private static final String NAME_1 = "name-1";
    private static final String NAME_2 = "name-2";
    private static final String NO_EXIST_NAME = "no-exist-name";

    private static final Student STUDENT_0 = new Student(ID_0, NAME_0);
    private static final Student STUDENT_1 = new Student(ID_1, NAME_1);
    private static final Student STUDENT_2 = new Student(ID_2, NAME_2);

    private static final List<Student> STUDENTS = Arrays.asList(STUDENT_0, STUDENT_1, STUDENT_2);

    @Autowired
    private StudentRepository repository;

    @Before
    public void setup() {
        this.repository.deleteAll();
    }

    private void assertDomainListEquals(@NonNull List<Student> found, @NonNull List<Student> expected) {
        found.sort(Comparator.comparing(Student::getId));
        expected.sort(Comparator.comparing(Student::getId));

        Assert.assertEquals(found.size(), expected.size());
        Assert.assertEquals(found, expected);
    }

    @Test
    public void testDeleteAll() {
        repository.saveAll(STUDENTS);

        Assert.assertTrue(repository.findAll().iterator().hasNext());

        repository.deleteAll();

        Assert.assertFalse(repository.findAll().iterator().hasNext());
    }

    @Test
    public void testDeleteAllOnType() {
        repository.saveAll(STUDENTS);

        Assert.assertTrue(repository.findAll().iterator().hasNext());

        repository.deleteAll(GremlinEntityType.EDGE);

        Assert.assertTrue(repository.findAll().iterator().hasNext());

        repository.deleteAll(GremlinEntityType.VERTEX);

        Assert.assertFalse(repository.findAll().iterator().hasNext());
    }

    @Test
    public void testDeleteAllOnDomain() {
        repository.saveAll(STUDENTS);

        Assert.assertTrue(repository.findAll().iterator().hasNext());

        repository.deleteAll(Student.class);

        Assert.assertFalse(repository.findAll().iterator().hasNext());
    }

    @Test
    public void testSave() {
        repository.save(STUDENT_0);

        Assert.assertTrue(repository.findById(STUDENT_0.getId()).isPresent());
        Assert.assertFalse(repository.findById(STUDENT_1.getId()).isPresent());
    }

    @Test
    public void testSaveAll() {
        repository.saveAll(STUDENTS);

        final List<Student> found = Lists.newArrayList(repository.findAll());

        assertDomainListEquals(found, STUDENTS);
    }

    @Test
    public void testFindById() {
        repository.saveAll(STUDENTS);

        Optional<Student> optional = repository.findById(STUDENT_0.getId());

        Assert.assertTrue(optional.isPresent());
        Assert.assertEquals(optional.get(), STUDENT_0);

        optional = repository.findById(NO_EXIST_ID);

        Assert.assertFalse(optional.isPresent());
    }

    @Test
    public void testExistsById() {
        repository.saveAll(STUDENTS);

        Assert.assertTrue(repository.existsById(STUDENT_0.getId()));
        Assert.assertFalse(repository.existsById(NO_EXIST_ID));
    }

    @Test
    public void testFindAllById() {
        final List<Student> expected = Arrays.asList(STUDENT_0, STUDENT_1);
        final List<Long> ids = Arrays.asList(STUDENT_0.getId(), STUDENT_1.getId(), NO_EXIST_ID);

        repository.saveAll(STUDENTS);

        final List<Student> found = Lists.newArrayList(repository.findAllById(ids));

        assertDomainListEquals(found, expected);

        Assert.assertFalse(repository.findAllById(Collections.singleton(NO_EXIST_ID)).iterator().hasNext());
    }

    @Test
    public void testCount() {
        repository.saveAll(STUDENTS);

        Assert.assertEquals(repository.count(), STUDENTS.size());

        repository.deleteAll();

        Assert.assertEquals(repository.count(), 0);
    }

    @Test
    public void testDeleteById() {
        repository.saveAll(STUDENTS);

        Assert.assertTrue(repository.findById(STUDENT_0.getId()).isPresent());

        repository.deleteById(STUDENT_0.getId());

        Assert.assertFalse(repository.findById(STUDENT_0.getId()).isPresent());
    }

    @Test
    public void testVertexCount() {
        repository.saveAll(STUDENTS);

        Assert.assertEquals(repository.vertexCount(), STUDENTS.size());
    }

    @Test
    public void testEdgeCount() {
        repository.saveAll(STUDENTS);

        Assert.assertEquals(repository.edgeCount(), 0);
    }

    @Test
    public void testFindByName() {
        repository.saveAll(STUDENTS);

        Assert.assertTrue(repository.findByName(NO_EXIST_NAME).isEmpty());
        Assert.assertEquals(repository.findByName(STUDENT_0.getName()).size(), 1);
        Assert.assertEquals(repository.findByName(STUDENT_0.getName()).get(0), STUDENT_0);
    }
}
