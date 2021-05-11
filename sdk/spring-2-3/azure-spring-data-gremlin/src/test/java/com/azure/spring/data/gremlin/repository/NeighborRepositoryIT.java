// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository;

import com.azure.spring.data.gremlin.common.GremlinEntityType;
import com.azure.spring.data.gremlin.common.TestRepositoryConfiguration;
import com.azure.spring.data.gremlin.common.domain.Neighbor;
import com.azure.spring.data.gremlin.common.domain.Student;
import com.azure.spring.data.gremlin.common.repository.NeighborRepository;
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
public class NeighborRepositoryIT {

    private static final Long STUDENT_ID_0 = 12349274637234L;
    private static final Long STUDENT_ID_1 = 1L;
    private static final Long STUDENT_ID_2 = 2L;
    private static final Long DISTANCE_0 = 1234L;
    private static final Long DISTANCE_1 = 3422L;
    private static final Long NEIGHBOR_ID_0 = 3L;
    private static final Long NEIGHBOR_ID_1 = 4L;
    private static final Long NO_EXIST_ID = -1L;

    private static final String NAME_0 = "name-0";
    private static final String NAME_1 = "name-1";
    private static final String NAME_2 = "name-2";

    private static final Student STUDENT_0 = new Student(STUDENT_ID_0, NAME_0);
    private static final Student STUDENT_1 = new Student(STUDENT_ID_1, NAME_1);
    private static final Student STUDENT_2 = new Student(STUDENT_ID_2, NAME_2);

    private static final Neighbor NEIGHBOR_0 = new Neighbor(NEIGHBOR_ID_0, DISTANCE_0, STUDENT_0, STUDENT_1);
    private static final Neighbor NEIGHBOR_1 = new Neighbor(NEIGHBOR_ID_1, DISTANCE_1, STUDENT_2, STUDENT_1);

    private static final List<Student> STUDENTS = Arrays.asList(STUDENT_0, STUDENT_1, STUDENT_2);
    private static final List<Neighbor> NEIGHBORS = Arrays.asList(NEIGHBOR_0, NEIGHBOR_1);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private NeighborRepository neighborRepository;

    @Before
    public void setup() {
        this.studentRepository.deleteAll();
        this.neighborRepository.deleteAll();
    }

    private void assertDomainListEquals(@NonNull List<Neighbor> found, @NonNull List<Neighbor> expected) {
        found.sort(Comparator.comparing(Neighbor::getId));
        expected.sort(Comparator.comparing(Neighbor::getId));

        Assert.assertEquals(found.size(), expected.size());
        Assert.assertEquals(found, expected);
    }

    @Test
    public void testDeleteAll() {
        neighborRepository.deleteAll();

        Assert.assertFalse(neighborRepository.findAll().iterator().hasNext());

        studentRepository.saveAll(STUDENTS);
        neighborRepository.saveAll(NEIGHBORS);

        Assert.assertTrue(neighborRepository.findAll().iterator().hasNext());
    }

    @Test
    public void testDeleteAllOnType() {
        neighborRepository.deleteAll(GremlinEntityType.EDGE);

        Assert.assertFalse(neighborRepository.findAll().iterator().hasNext());

        studentRepository.saveAll(STUDENTS);
        neighborRepository.saveAll(NEIGHBORS);

        Assert.assertTrue(neighborRepository.findAll().iterator().hasNext());
    }

    @Test
    public void testDeleteAllOnDomain() {
        studentRepository.saveAll(STUDENTS);
        neighborRepository.saveAll(NEIGHBORS);

        Assert.assertTrue(neighborRepository.findAll().iterator().hasNext());

        neighborRepository.deleteAll(Neighbor.class);

        Assert.assertFalse(neighborRepository.findAll().iterator().hasNext());
    }

    @Test
    public void testSave() {
        studentRepository.saveAll(STUDENTS);
        neighborRepository.save(NEIGHBOR_0);

        Assert.assertTrue(neighborRepository.findById(NEIGHBOR_0.getId()).isPresent());
        Assert.assertFalse(neighborRepository.findById(NEIGHBOR_1.getId()).isPresent());
    }

    @Test
    public void testSaveAll() {
        studentRepository.saveAll(STUDENTS);
        neighborRepository.saveAll(NEIGHBORS);

        final List<Neighbor> found = Lists.newArrayList(neighborRepository.findAll());

        assertDomainListEquals(found, NEIGHBORS);
    }

    @Test
    public void testFindById() {
        studentRepository.saveAll(STUDENTS);
        neighborRepository.saveAll(NEIGHBORS);

        Optional<Neighbor> optional = neighborRepository.findById(NEIGHBOR_0.getId());

        Assert.assertTrue(optional.isPresent());
        Assert.assertEquals(optional.get(), NEIGHBOR_0);

        optional = neighborRepository.findById(NO_EXIST_ID);

        Assert.assertFalse(optional.isPresent());
    }

    @Test
    public void testExistsById() {
        studentRepository.saveAll(STUDENTS);
        neighborRepository.saveAll(NEIGHBORS);

        Assert.assertTrue(neighborRepository.existsById(NEIGHBOR_0.getId()));
        Assert.assertFalse(neighborRepository.existsById(NO_EXIST_ID));
    }

    @Test
    public void testFindAllById() {
        studentRepository.saveAll(STUDENTS);
        neighborRepository.saveAll(NEIGHBORS);

        final List<Long> ids = Arrays.asList(NEIGHBOR_0.getId(), NEIGHBOR_1.getId());
        final List<Neighbor> found = Lists.newArrayList(neighborRepository.findAllById(ids));

        assertDomainListEquals(found, NEIGHBORS);

        Assert.assertFalse(neighborRepository.findAllById(Collections.singleton(NO_EXIST_ID)).iterator().hasNext());
    }

    @Test
    public void testCount() {
        studentRepository.saveAll(STUDENTS);
        neighborRepository.saveAll(NEIGHBORS);

        Assert.assertEquals(neighborRepository.count(), STUDENTS.size() + NEIGHBORS.size());

        neighborRepository.deleteAll();

        Assert.assertEquals(neighborRepository.count(), 0);
    }

    @Test
    public void testDeleteById() {
        studentRepository.saveAll(STUDENTS);
        neighborRepository.saveAll(NEIGHBORS);

        Assert.assertTrue(neighborRepository.findById(NEIGHBOR_0.getId()).isPresent());

        neighborRepository.deleteById(NEIGHBOR_0.getId());

        Assert.assertFalse(neighborRepository.findById(NEIGHBOR_0.getId()).isPresent());
    }

    @Test
    public void testEdgeCount() {
        studentRepository.saveAll(STUDENTS);
        neighborRepository.saveAll(NEIGHBORS);

        Assert.assertEquals(neighborRepository.edgeCount(), NEIGHBORS.size());
    }
}
