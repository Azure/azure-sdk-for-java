// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Importance;
import com.azure.spring.data.cosmos.domain.Memo;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.MemoRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class MemoRepositoryIT {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(TestConstants.DATE_FORMAT);

    private static Date memoDate;
    private static Date memoDateBefore;
    private static Date memoDateAfter;
    private static Date futureDate1;
    private static Date futureDate2;

    private static Memo testMemo1;
    private static Memo testMemo2;
    private static Memo testMemo3;

    private static final CosmosEntityInformation<Memo, String> entityInformation =
        new CosmosEntityInformation<>(Memo.class);

    private static CosmosTemplate staticTemplate;
    private static boolean isSetupDone;

    @Autowired
    private CosmosTemplate template;

    @Autowired
    MemoRepository repository;

    @BeforeClass
    public static void init() throws ParseException {
        memoDate = DATE_FORMAT.parse(TestConstants.DATE_STRING);
        memoDateBefore = DATE_FORMAT.parse(TestConstants.DATE_BEFORE_STRING);
        memoDateAfter = DATE_FORMAT.parse(TestConstants.DATE_AFTER_STRING);
        futureDate1 = DATE_FORMAT.parse(TestConstants.DATE_FUTURE_STRING_1);
        futureDate2 = DATE_FORMAT.parse(TestConstants.DATE_FUTURE_STRING_2);
        testMemo1 = new Memo(TestConstants.ID_1, TestConstants.MESSAGE, memoDateBefore, Importance.HIGH);
        testMemo2 = new Memo(TestConstants.ID_2, TestConstants.NEW_MESSAGE, memoDate, Importance.LOW);
        testMemo3 = new Memo(TestConstants.ID_3, TestConstants.NEW_MESSAGE, memoDateAfter, Importance.LOW);
    }

    @Before
    public void setUp() {
        if (!isSetupDone) {
            staticTemplate = template;
            template.createContainerIfNotExists(entityInformation);
        }
        repository.saveAll(Arrays.asList(testMemo1, testMemo2, testMemo3));
        isSetupDone = true;
    }

    @After
    public void cleanup() {
        repository.deleteAll();
    }

    @AfterClass
    public static void afterClassCleanup() {
        staticTemplate.deleteContainer(entityInformation.getContainerName());
    }

    @Test
    public void testFindAll() {
        final List<Memo> result = TestUtils.toList(repository.findAll());

        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    public void testFindByDate() {
        final List<Memo> result = TestUtils.toList(repository.findMemoByDate(memoDate));

        assertThat(result.size()).isEqualTo(1);
        assertMemoEquals(result.get(0), testMemo1);
    }

    @Test
    public void testFindByEnum() {
        final List<Memo> result = TestUtils.toList(repository.findMemoByImportance(testMemo1.getImportance()));

        assertThat(result.size()).isEqualTo(1);
        assertMemoEquals(result.get(0), testMemo1);
    }

    private void assertMemoEquals(Memo actual, Memo expected) {
        assertThat(actual.getId().equals(expected.getId()));
        assertThat(actual.getMessage().equals(expected.getMessage()));
        assertThat(actual.getDate().equals(expected.getDate()));
        assertThat(actual.getImportance().equals(expected.getImportance()));
    }

    @Test
    public void testFindByBefore() {
        List<Memo> memos = TestUtils.toList(this.repository.findByDateBefore(memoDateBefore));

        Assert.assertTrue(memos.isEmpty());

        memos = TestUtils.toList(this.repository.findByDateBefore(memoDate));

        Assert.assertEquals(1, memos.size());
        Assert.assertEquals(testMemo1, memos.get(0));

        memos = TestUtils.toList(this.repository.findByDateBefore(memoDateAfter));
        final List<Memo> reference = Arrays.asList(testMemo1, testMemo2);

        memos.sort(Comparator.comparing(Memo::getId));
        reference.sort(Comparator.comparing(Memo::getId));

        Assert.assertEquals(reference.size(), memos.size());
        Assert.assertEquals(reference, memos);
    }

    @Test
    public void testFindByBeforeWithAndOr() {
        List<Memo> memos = TestUtils.toList(this.repository.findByDateBeforeAndMessage(memoDate,
            TestConstants.NEW_MESSAGE));

        Assert.assertTrue(memos.isEmpty());

        memos = TestUtils.toList(this.repository.findByDateBeforeAndMessage(memoDate, TestConstants.MESSAGE));

        Assert.assertEquals(1, memos.size());
        Assert.assertEquals(testMemo1, memos.get(0));

        memos = TestUtils.toList(this.repository.findByDateBeforeOrMessage(memoDateAfter, TestConstants.MESSAGE));
        final List<Memo> reference = Arrays.asList(testMemo1, testMemo2);

        memos.sort(Comparator.comparing(Memo::getId));
        reference.sort(Comparator.comparing(Memo::getId));

        Assert.assertEquals(reference.size(), memos.size());
        Assert.assertEquals(reference, memos);
    }

    @Test
    public void testFindByAfter() {
        List<Memo> memos = TestUtils.toList(this.repository.findByDateAfter(memoDateAfter));

        Assert.assertTrue(memos.isEmpty());

        memos = TestUtils.toList(this.repository.findByDateAfter(memoDate));

        Assert.assertEquals(1, memos.size());
        Assert.assertEquals(testMemo3, memos.get(0));

        memos = TestUtils.toList(this.repository.findByDateAfter(memoDateBefore));
        final List<Memo> reference = Arrays.asList(testMemo2, testMemo3);

        memos.sort(Comparator.comparing(Memo::getId));
        reference.sort(Comparator.comparing(Memo::getId));

        Assert.assertEquals(reference.size(), memos.size());
        Assert.assertEquals(reference, memos);
    }

    @Test
    public void testFindByAfterWithAndOr() {
        List<Memo> memos = TestUtils.toList(this.repository.findByDateAfterAndMessage(memoDate, TestConstants.MESSAGE));

        Assert.assertTrue(memos.isEmpty());

        memos = TestUtils.toList(this.repository.findByDateAfterAndMessage(memoDate, TestConstants.NEW_MESSAGE));

        Assert.assertEquals(1, memos.size());
        Assert.assertEquals(testMemo3, memos.get(0));

        memos = TestUtils.toList(this.repository.findByDateAfterOrMessage(memoDateBefore, TestConstants.MESSAGE));
        final List<Memo> reference = Arrays.asList(testMemo1, testMemo2, testMemo3);

        memos.sort(Comparator.comparing(Memo::getId));
        reference.sort(Comparator.comparing(Memo::getId));

        Assert.assertEquals(reference.size(), memos.size());
        Assert.assertEquals(reference, memos);
    }

    @Test
    public void testFindByBetween() {
        List<Memo> memos = TestUtils.toList(this.repository
            .findByDateBetween(testMemo1.getDate(), testMemo3.getDate()));
        List<Memo> reference = Arrays.asList(testMemo1, testMemo2, testMemo3);

        assertMemoListEquals(memos, reference);

        memos = TestUtils.toList(this.repository.findByDateBetween(testMemo1.getDate(), testMemo2.getDate()));
        reference = Arrays.asList(testMemo1, testMemo2);

        assertMemoListEquals(memos, reference);

        memos = TestUtils.toList(this.repository.findByDateBetween(futureDate1, futureDate2));
        reference = Collections.emptyList();

        assertMemoListEquals(memos, reference);
    }

    @Test
    public void testFindByBetweenWithAnd() {
        final List<Memo> memos = TestUtils.toList(this.repository
            .findByDateBetweenAndMessage(testMemo1.getDate(), testMemo2.getDate(), TestConstants.MESSAGE));
        assertMemoListEquals(memos, Collections.singletonList(testMemo1));
    }

    @Test
    public void testFindByBetweenWithOr() {
        final List<Memo> memos = TestUtils.toList(this.repository
            .findByDateBetweenOrMessage(testMemo1.getDate(), testMemo2.getDate(), TestConstants.NEW_MESSAGE));
        assertMemoListEquals(memos, Arrays.asList(testMemo1, testMemo2, testMemo3));
    }

    private void assertMemoListEquals(List<Memo> memos, List<Memo> reference) {
        memos.sort(Comparator.comparing(Memo::getId));
        reference.sort(Comparator.comparing(Memo::getId));

        Assert.assertEquals(reference.size(), memos.size());
        Assert.assertEquals(reference, memos);
    }

    @Test(expected = CosmosAccessException.class)
    @Ignore // TODO(pan): Ignore this test case for now, will update this from service update.
    public void testFindByStartsWithWithException() {
        repository.findByMessageStartsWith(testMemo1.getMessage());
    }

    @Test
    public void testFindByStartsWith() {
        final List<Memo> result =
            TestUtils.toList(repository.findByMessageStartsWith(testMemo1.getMessage().substring(0, 10)));
        Assert.assertEquals(testMemo1, result.get(0));
        Assert.assertEquals(1, result.size());
    }

}
