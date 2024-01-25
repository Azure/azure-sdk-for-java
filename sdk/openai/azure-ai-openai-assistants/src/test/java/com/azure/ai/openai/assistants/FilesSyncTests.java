package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.FilePurpose;
import com.azure.ai.openai.assistants.models.OpenAIFile;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class FilesSyncTests extends AssistantsClientTestBase {
    private AssistantsClient client;


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void fileOperations(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        uploadFileRunner(() -> {
            List<OpenAIFile> files = Arrays.asList();
//            byte[] fileDataAssistant = BinaryData.fromFile(openResourceFile("java_sdk_tests_assistants.txt")).toBytes();
//            files.add(client.uploadFile(fileDataAssistant, FilePurpose.ASSISTANTS, "java_sdk_tests_assistants.txt"));

            Path assistantFilePath = openResourceFile("java_sdk_tests_assistants.txt");
            files.add(client.uploadFile(assistantFilePath, FilePurpose.ASSISTANTS));

//            byte[] fileDataFineTune = BinaryData.fromString("Fine tune purpose file content").toBytes();
//            files.add(client.uploadFile(fileDataFineTune, FilePurpose.FINE_TUNE));

//            byte[] fileDataNull = BinaryData.fromString("Null purpose file content").toBytes();
//            files.add(client.uploadFile(fileDataNull, null));

            files.forEach(file -> {
                System.out.println("File ID: " + file.getId());
                client.deleteFile(file.getId());
            });

        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void fileOperationsConvenienceMethod(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        uploadFileRunner(() -> {
//            OpenAIFile openAIFile = client.uploadFile(filePath, FilePurpose.ASSISTANTS);
//
//            client.listFiles()

        });
    }


}
