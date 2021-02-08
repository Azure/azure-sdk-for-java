// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository;

import com.azure.spring.data.gremlin.common.TestRepositoryConfiguration;
import com.azure.spring.data.gremlin.common.TestUtils;
import com.azure.spring.data.gremlin.common.domain.Group;
import com.azure.spring.data.gremlin.common.domain.GroupOwner;
import com.azure.spring.data.gremlin.common.domain.Student;
import com.azure.spring.data.gremlin.common.repository.GroupOwnerRepository;
import com.azure.spring.data.gremlin.common.repository.GroupRepository;
import com.azure.spring.data.gremlin.common.repository.StudentRepository;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfiguration.class)
public class GroupRepositoryIT {

    private static final Long STUDENT_ID_0 = 1111L;
    private static final Long STUDENT_ID_1 = 1234L;
    private static final Long STUDENT_ID_2 = 2345L;
    private static final Long STUDENT_ID_3 = 4823L;

    private static final String STUDENT_NAME_0 = "student-0";
    private static final String STUDENT_NAME_1 = "student-1";
    private static final String STUDENT_NAME_2 = "student-2";
    private static final String STUDENT_NAME_3 = "student-3";

    private static final String GROUP_OWNER_ID_0 = "owner-0";
    private static final String GROUP_OWNER_ID_1 = "owner-1";

    private static final Integer GROUP_OWNER_EXPIRE_DAYS_0 = 90;
    private static final Integer GROUP_OWNER_EXPIRE_DAYS_1 = 120;

    private static final Student STUDENT_0 = new Student(STUDENT_ID_0, STUDENT_NAME_0);
    private static final Student STUDENT_1 = new Student(STUDENT_ID_1, STUDENT_NAME_1);
    private static final Student STUDENT_2 = new Student(STUDENT_ID_2, STUDENT_NAME_2);
    private static final Student STUDENT_3 = new Student(STUDENT_ID_3, STUDENT_NAME_3);

    private static final GroupOwner GROUP_OWNER_0 = new GroupOwner(GROUP_OWNER_ID_0, GROUP_OWNER_EXPIRE_DAYS_0);
    private static final GroupOwner GROUP_OWNER_1 = new GroupOwner(GROUP_OWNER_ID_1, GROUP_OWNER_EXPIRE_DAYS_1);

    private static final Group GROUP_0 = new Group(STUDENT_0, GROUP_OWNER_0);
    private static final Group GROUP_1 = new Group(STUDENT_1, GROUP_OWNER_0);
    private static final Group GROUP_2 = new Group(STUDENT_2, GROUP_OWNER_0);
    private static final Group GROUP_3 = new Group(STUDENT_3, GROUP_OWNER_0);
    private static final Group GROUP_4 = new Group(STUDENT_0, GROUP_OWNER_1);
    private static final Group GROUP_5 = new Group(STUDENT_1, GROUP_OWNER_1);

    private static final List<Student> STUDENTS = Arrays.asList(STUDENT_0, STUDENT_1, STUDENT_2, STUDENT_3);
    private static final List<GroupOwner> GROUP_OWNERS = Arrays.asList(GROUP_OWNER_0, GROUP_OWNER_1);
    private static final List<Group> GROUPS = Arrays.asList(GROUP_0, GROUP_1, GROUP_2, GROUP_3, GROUP_4, GROUP_5);

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupOwnerRepository groupOwnerRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Before
    public void setup() {
        this.groupOwnerRepository.deleteAll();
        this.studentRepository.deleteAll();
        this.groupRepository.deleteAll();

        this.studentRepository.saveAll(STUDENTS);
        this.groupOwnerRepository.saveAll(GROUP_OWNERS);
    }

    @After
    public void cleanup() {
        this.groupOwnerRepository.deleteAll();
        this.studentRepository.deleteAll();
        this.groupRepository.deleteAll();
    }

    @Test
    public void testGeneratedIdFindById() {
        final Group expect = this.groupRepository.save(GROUP_0);

        Assert.assertNotNull(expect.getId());

        final Optional<Group> optional = this.groupRepository.findById(expect.getId());

        Assert.assertTrue(optional.isPresent());
        Assert.assertEquals(expect, optional.get());
    }

    @Test
    public void testGeneratedIdFindAll() {
        final List<Group> expect = Lists.newArrayList(this.groupRepository.saveAll(GROUPS));
        final List<Group> actual = Lists.newArrayList(this.groupRepository.findAll());

        TestUtils.assertEntitiesEquals(expect, actual);
    }

    @Test
    public void testGeneratedIdDeleteById() {
        final Group group = this.groupRepository.save(GROUP_0);

        this.groupRepository.deleteById(group.getId());

        Assert.assertFalse(this.groupRepository.findById(group.getId()).isPresent());
        Assert.assertFalse(this.groupRepository.existsById(group.getId()));
    }
}
