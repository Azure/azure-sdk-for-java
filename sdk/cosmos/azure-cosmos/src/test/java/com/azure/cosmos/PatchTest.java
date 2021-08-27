// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class PatchTest extends TestSuiteBase {

    private CosmosClient client;
    private CosmosContainer container;

    @Factory(dataProvider = "clientBuilders")
    public PatchTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_PatchTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().contentResponseOnWriteEnabled(true).buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {  "emulator"  }, timeOut = TIMEOUT * 100)
    public void itemConditionalPatchSuccess() {
        ToDoActivity testItem = ToDoActivity.createRandomItem(this.container);

        int originalTaskNum = testItem.taskNum;
        int newTaskNum = originalTaskNum + 1;

        assertThat(testItem.children[1].status).isNull();

        com.azure.cosmos.models.CosmosPatchOperations cosmosPatchOperations = com.azure.cosmos.models.CosmosPatchOperations.create();
        cosmosPatchOperations.add("/children/1/CamelCase", "patched");
        cosmosPatchOperations.remove("/description");
        cosmosPatchOperations.replace("/taskNum", newTaskNum);
        cosmosPatchOperations.set("/valid", false);

        CosmosPatchItemRequestOptions optionsFalse = new CosmosPatchItemRequestOptions();
        int numFalse = testItem.taskNum+1;
        optionsFalse.setFilterPredicate("from root where root.taskNum = " + numFalse);
        try {
            CosmosItemResponse<ToDoActivity> responseFail = this.container.patchItem(
                testItem.id,
                new PartitionKey(testItem.status),
                cosmosPatchOperations,
                optionsFalse,
                ToDoActivity.class);

            fail("Patch operation should fail in case of pre-condition failure.");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpResponseStatus.PRECONDITION_FAILED.code());
        }

        CosmosPatchItemRequestOptions optionsTrue = new CosmosPatchItemRequestOptions();
        int numTrue = testItem.taskNum;
        optionsTrue.setFilterPredicate("from root where root.taskNum = " + numTrue);
        CosmosItemResponse<ToDoActivity> responsePass = this.container.patchItem(
            testItem.id,
            new PartitionKey(testItem.status),
            cosmosPatchOperations,
            optionsTrue,
            ToDoActivity.class);

        assertThat(responsePass.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());

        ToDoActivity patchedItem = responsePass.getItem();
        assertThat(patchedItem).isNotNull();

        assertThat(patchedItem.children[1].camelCase).isEqualTo("patched");
        assertThat(patchedItem.description).isNull();
        assertThat(patchedItem.taskNum).isEqualTo(newTaskNum);
        assertThat(patchedItem.valid).isEqualTo(false);

        // read resource to validate the patch operation
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        responsePass = this.container.readItem(
            testItem.id,
            new PartitionKey(testItem.status),
            options, ToDoActivity.class);

        assertThat(responsePass.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(responsePass.getItem()).isEqualTo(patchedItem);
    }

    @Test(groups = {  "emulator"  }, timeOut = TIMEOUT * 100)
    public void itemPatchSuccess() {
        ToDoActivity testItem = ToDoActivity.createRandomItem(this.container);

        int originalTaskNum = testItem.taskNum;
        int newTaskNum = originalTaskNum + 1;

        assertThat(testItem.children[1].status).isNull();

        com.azure.cosmos.models.CosmosPatchOperations cosmosPatchOperations = com.azure.cosmos.models.CosmosPatchOperations.create();
        cosmosPatchOperations.add("/children/1/CamelCase", "patched");
        cosmosPatchOperations.remove("/description");
        cosmosPatchOperations.replace("/taskNum", newTaskNum);
        cosmosPatchOperations.set("/valid", false);

        CosmosPatchItemRequestOptions options = new CosmosPatchItemRequestOptions();
        CosmosItemResponse<ToDoActivity> response = this.container.patchItem(
            testItem.id,
            new PartitionKey(testItem.status),
            cosmosPatchOperations,
            options,
            ToDoActivity.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());

        ToDoActivity patchedItem = response.getItem();
        assertThat(patchedItem).isNotNull();

        assertThat(patchedItem.children[1].camelCase).isEqualTo("patched");
        assertThat(patchedItem.description).isNull();
        assertThat(patchedItem.taskNum).isEqualTo(newTaskNum);
        assertThat(patchedItem.valid).isEqualTo(false);

        // read resource to validate the patch operation
        response = this.container.readItem(
            testItem.id,
            new PartitionKey(testItem.status),
            options, ToDoActivity.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(response.getItem()).isEqualTo(patchedItem);
    }

    @Test(groups = {  "emulator"  }, timeOut = TIMEOUT * 100)
    public void itemPatchContentResponseOnWriteEnabled() {
        ToDoActivity testItem = ToDoActivity.createRandomItem(this.container);

        int originalTaskNum = testItem.taskNum;
        int newTaskNum = originalTaskNum + 10;

        assertThat(testItem.children[1].status).isNull();

        com.azure.cosmos.models.CosmosPatchOperations cosmosPatchOperations = com.azure.cosmos.models.CosmosPatchOperations.create();
        cosmosPatchOperations.add("/children/1/CamelCase", "alpha");
        cosmosPatchOperations.remove("/description");
        cosmosPatchOperations.replace("/taskNum", newTaskNum);
        cosmosPatchOperations.set("/cost", 100);

        CosmosPatchItemRequestOptions options = new CosmosPatchItemRequestOptions();
        options.setContentResponseOnWriteEnabled(false);

        CosmosItemResponse<ToDoActivity> response = this.container.patchItem(
            testItem.id,
            new PartitionKey(testItem.status),
            cosmosPatchOperations,
            options,
            ToDoActivity.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(response.getItem()).isNull(); // skip content is true

        // Right now
        com.azure.cosmos.models.CosmosPatchOperations cosmosPatchOperations2 = com.azure.cosmos.models.CosmosPatchOperations.create();
        cosmosPatchOperations2.set("/valid", false);

        CosmosItemResponse<ToDoActivity> response2 = this.container.patchItem(
            testItem.id,
            new PartitionKey(testItem.status),
            cosmosPatchOperations2,
            ToDoActivity.class);

        assertThat(response2.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        ToDoActivity patchedItem = response2.getItem();
        assertThat(patchedItem).isNotNull();

        // Both the patch's result.
        assertThat(patchedItem.children[1].camelCase).isEqualTo("alpha");
        assertThat(patchedItem.description).isNull();
        assertThat(patchedItem.taskNum).isEqualTo(newTaskNum);
        assertThat(patchedItem.cost).isEqualTo(100);
        assertThat(patchedItem.valid).isEqualTo(false);

        // read resource to validate the patch operation
        response = this.container.readItem(
            testItem.id,
            new PartitionKey(testItem.status),
            options, ToDoActivity.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(response.getItem()).isEqualTo(patchedItem);
    }

    @Test(groups = {  "emulator"  }, timeOut = TIMEOUT)
    public void itemPatchFailure() {
        // Create an item
        ToDoActivity testItem = ToDoActivity.createRandomItem(this.container);
        com.azure.cosmos.models.CosmosPatchOperations cosmosPatchOperations = com.azure.cosmos.models.CosmosPatchOperations.create();
        cosmosPatchOperations.add("/nonExistentParent/child", "bar");
        cosmosPatchOperations.remove("/cost");

        // item does not exist - 404 Resource Not Found error
        try {
            CosmosItemResponse<ToDoActivity> patchItemResponse = this.container.patchItem(
                UUID.randomUUID().toString(),
                new PartitionKey(testItem.status),
                cosmosPatchOperations,
                ToDoActivity.class);
            fail("Update operation should fail if the item doesn't exist.");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpResponseStatus.NOT_FOUND.code());
            assertThat(ex.getMessage()).contains("Resource Not Found");
        }

        // adding a child when parent / ancestor does not exist - 400 BadRequest response
        try {
            CosmosItemResponse<ToDoActivity> patchItemResponse = this.container.patchItem(
                testItem.id,
                new PartitionKey(testItem.status),
                cosmosPatchOperations,
                ToDoActivity.class);

            fail("Update operation should fail for malformed PatchSpecification.");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
            assertThat(ex.getMessage())
                .contains("Add Operation only support adding a leaf node of an existing node(array or object), no path found beyond: 'nonExistentParent'");
        }

        // precondition failure - 412 response
        CosmosPatchItemRequestOptions requestOptions = new CosmosPatchItemRequestOptions();
        requestOptions.setIfMatchETag(UUID.randomUUID().toString());

        try {
            CosmosItemResponse<ToDoActivity> patchItemResponse = this.container.patchItem(
                testItem.id,
                new PartitionKey(testItem.status),
                cosmosPatchOperations,
                requestOptions,
                ToDoActivity.class);

            fail("Update operation should fail in case of pre-condition failure.");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpResponseStatus.PRECONDITION_FAILED.code());
            assertThat(ex.getMessage()).contains("One of the specified pre-condition is not met");
        }
    }

    public static class ToDoActivity {

        public String id;
        public int taskNum;
        public double cost;
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

        public static ToDoActivity createRandomItem(CosmosContainer container) {

            String pk = "TBD" + UUID.randomUUID().toString();
            ToDoActivity toDoActivity = ToDoActivity.createRandomToDoActivity(pk);

            CosmosItemResponse<ToDoActivity> response = container.createItem(toDoActivity, new PartitionKey(toDoActivity.status), null);

            assertThat(response.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
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
