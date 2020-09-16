// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.patch.PatchOperation;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class PatchTest extends TestSuiteBase {

    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "simpleClientBuildersWithDirect")
    public PatchTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_PatchTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().contentResponseOnWriteEnabled(true).buildAsyncClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client);
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void itemPatchSuccessTest() {
        ToDoActivity testItem = ToDoActivity.createRandomItem(this.container);

        int originalTaskNum = testItem.taskNum;
        int newTaskNum = originalTaskNum + 1;

        Assert.assertNull(testItem.children[1].status);

        List<PatchOperation<?>> patchOperations = new ArrayList<>();
        patchOperations.add(PatchOperation.createAddOperation("/children/1/CamelCase", "patched"));
        patchOperations.add(PatchOperation.createRemoveOperation("/description"));
        patchOperations.add(PatchOperation.createReplaceOperation("/taskNum", newTaskNum));

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        CosmosItemResponse<ToDoActivity> response = this.container.patchItem(
            testItem.id,
            new PartitionKey(testItem.status),
            patchOperations,
            options,
            ToDoActivity.class).block();

        assertEquals(HttpResponseStatus.OK.code(), response.getStatusCode());

        ToDoActivity patchedItem = response.getItem();
        Assert.assertNotNull(patchedItem);
        // read resource to validate the patch operation
        response = this.container.readItem(
            testItem.id,
            new PartitionKey(testItem.status),
            options, ToDoActivity.class).block();

        assertEquals(HttpResponseStatus.OK.code(), response.getStatusCode());
        Assert.assertNotNull(response.getItem());
        assertEquals("patched", response.getItem().children[1].camelCase);
        Assert.assertNull(response.getItem().description);
        assertEquals(newTaskNum, response.getItem().taskNum);

        assertEquals(patchedItem, response.getItem());
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void itemPatchFailureTest() {
        // Create an item
        ToDoActivity testItem = ToDoActivity.createRandomItem(this.container);
        List<PatchOperation<?>> patchOperations = new ArrayList<>();
        patchOperations.add(PatchOperation.createAddOperation("/nonExistentParent/child", "bar"));
        patchOperations.add(PatchOperation.createRemoveOperation("/cost"));

        // item does not exist - 404 Resource Not Found error
        try {
            CosmosItemResponse<ToDoActivity> patchItemResponse = this.container.patchItem(
                UUID.randomUUID().toString(),
                new PartitionKey(testItem.status),
                patchOperations,
                ToDoActivity.class).block();
            Assert.fail("Patch operation should fail if the item doesn't exist.");
        }
        catch (CosmosException ex) {
            assertEquals(HttpResponseStatus.NOT_FOUND.code(), ex.getStatusCode());
            assertTrue(ex.getMessage().contains("Resource Not Found"), ex.getMessage());
        }

        // adding a child when parent / ancestor does not exist - 400 BadRequest response
        try {
            CosmosItemResponse<ToDoActivity> patchItemResponse = this.container.patchItem(
                testItem.id,
                new PartitionKey(testItem.status),
                patchOperations,
                ToDoActivity.class).block();

            Assert.fail("Patch operation should fail for malformed PatchSpecification.");
        }
        catch (CosmosException ex) {
            assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ex.getStatusCode());
            assertTrue(ex.getMessage().contains(
                "Add Operation only support adding a leaf node of an existing node(array or object), no path found beyond: 'nonExistentParent'"), ex.getMessage());
        }

        // precondition failure - 412 response
        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setIfMatchETag(UUID.randomUUID().toString());

        try {
            CosmosItemResponse<ToDoActivity> patchItemResponse = this.container.patchItem(
                testItem.id,
                new PartitionKey(testItem.status),
                patchOperations,
                requestOptions,
                ToDoActivity.class).block();

            Assert.fail("Patch operation should fail in case of pre-condition failure.");
        } catch (CosmosException ex) {
            assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ex.getStatusCode());
            assertTrue(ex.getMessage().contains("One of the specified pre-condition is not met"), ex.getMessage());
        }
    }

    public static class ToDoActivity {

        public String id;
        public int taskNum;
        public double cost ;
        public String description;

        @JsonProperty("mypk")
        public String status;

        @JsonProperty("CamelCase")
        public String camelCase;

        public boolean valid;
        public ToDoActivity[] children;

        public ToDoActivity() {
        }

        public ToDoActivity(String id, int taskNum)
        {
            this.id = id;
            this.taskNum = taskNum;
        }

        public ToDoActivity(String id, String description, String status, int taskNum, double cost, String camelCase, ToDoActivity[] children, boolean valid) {
            this.id = id;
            this.description = description;
            this.status = status;
            this.taskNum = taskNum;
            this.cost = cost;
            this.camelCase = camelCase;
            this.children = children;
            this.valid = valid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ToDoActivity that = (ToDoActivity) o;
            return taskNum == that.taskNum &&
                Double.compare(that.cost, cost) == 0 &&
                valid == that.valid &&
                Objects.equals(id, that.id) &&
                Objects.equals(description, that.description) &&
                Objects.equals(status, that.status) &&
                Objects.equals(camelCase, that.camelCase) &&
                Arrays.equals(children, that.children);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(id, taskNum, cost, description, status, camelCase, valid);
            result = 31 * result + Arrays.hashCode(children);
            return result;
        }

        public static ToDoActivity createRandomItem(CosmosAsyncContainer container) {

            String pk = "TBD" + UUID.randomUUID().toString();
            ToDoActivity toDoActivity = ToDoActivity.createRandomToDoActivity(pk);

            CosmosItemResponse<ToDoActivity> response = container.createItem(toDoActivity, new PartitionKey(toDoActivity.status), null).block();

            Assert.assertEquals(response.getStatusCode(), HttpResponseStatus.CREATED.code());
            return toDoActivity;
        }

        public static ToDoActivity createRandomToDoActivity(String pk) {

            String id = UUID.randomUUID().toString();

            return new ToDoActivity(
                id,
                "createRandomToDoActivity",
                pk,
                42,
                Double.MAX_VALUE,
                "camelCase",
                new ToDoActivity[]
                    {
                        new ToDoActivity(id = "child1", 30 ),
                        new ToDoActivity( "child2", 40)
                    },
                true
            );
        }
    }
}
