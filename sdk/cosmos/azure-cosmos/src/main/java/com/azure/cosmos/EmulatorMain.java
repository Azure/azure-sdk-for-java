package com.azure.cosmos;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.UUID;

public class EmulatorMain {
    // Emulator
    private static String HOST = "https://localhost:8081/";
    private static String KEY = "C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==";
//        private static String HOST = "https://cosmos-sdk-tests.documents.azure.com:443/";
//    private static String KEY = "hoTaUW11OCQ16P4Qp7tUM3C4xk20vTQZbh0eSTZHwTPyAQNMwlYqim63ltJJLuhUeDLC081r34vHOn8PHlGmug==";
    private static String DATABASE = "testdb";
    private static String CONTAINER = "container";


    public static void main(String[] args) throws IOException {
        int length = 1024 * 20;
        CosmosClient client = new CosmosClientBuilder()
            .endpoint(HOST)
            .key(KEY)
            //.gatewayMode()
            .buildClient();
        client.createDatabaseIfNotExists(DATABASE);
        CosmosDatabase db = client.getDatabase(DATABASE);
        db.createContainerIfNotExists(new CosmosContainerProperties(CONTAINER, "/mypk"));
        CosmosContainer container = db.getContainer(CONTAINER);

        // init data
        SampleData data = new SampleData();
        data.setId(UUID.randomUUID().toString());
        data.setMessages("a".repeat(length));
        data.setMypk("mypk1");
        // check data size
        ObjectMapper mapper = new ObjectMapper();
        byte[] bytes = mapper.writeValueAsBytes(data);
        System.out.format("Data size = %d\n", bytes.length);
        // upsert item
        CosmosItemResponse response = container.upsertItem(data);
        System.out.format("Status code = %d\n", response.getStatusCode());

        //String res = container.upsertItem(data);
        client.close();
    }
    public static class SampleData {
        private String id;
        private String message;
        private String mypk;
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getMessages() {
            return message;
        }
        public void setMessages(String message) {
            this.message = message;
        }

        public String getMypk() {
            return mypk;
        }

        public void setMypk(String mypk) {
            this.mypk = mypk;
        }
    }
}
