package com.azure.storage.blob.nio

import com.azure.core.util.BinaryData
import com.azure.core.util.Configuration
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.implementation.Constants.ConfigurationConstants.Nio

import spock.lang.Isolated

import java.nio.file.Files
import java.nio.file.Paths

@Isolated
class EnvironmentConfigurationTest  extends APISpec {
    Map<String, String> environmentConfig
    String blobName
    int blobSize

    def setup() {
        environmentConfig = [
            (Nio.ENVIRONMENT_DEFAULT_BLOB_ENDPOINT) : "https://${environment.primaryAccount.name}.blob.core.windows.net",
            (Nio.ENVIRONMENT_DEFAULT_ACCOUNT_NAME)  : environment.primaryAccount.name,
            (Nio.ENVIRONMENT_DEFAULT_ACCOUNT_KEY)   : environment.primaryAccount.key,
            (Nio.ENVIRONMENT_DEFAULT_FILE_STORES)   : cc.getBlobContainerName(),
            (Nio.ENVIRONMENT_AUTO_CREATE_FILESYSTEM): "true",
        ]
        blobName = generateBlobName()
        blobSize = Constants.KB

        cc.create()
        cc.getBlobClient(blobName).upload(BinaryData.fromBytes(getRandomByteArray(blobSize)))

        def globalConfig = Configuration.getGlobalConfiguration()
        environmentConfig.each { key, val -> globalConfig.put(key, val)}
    }

    def cleanup() {
        def globalConfig = Configuration.getGlobalConfiguration()
        environmentConfig.each { key, val -> globalConfig.remove(key)}
    }

    def "Path auto resolves"() {
        when:
        // account name not present in uri
        def path = Paths.get(new URI("azb://${cc.getBlobContainerName()}:/$blobName"))

        then:
        path != null
        Files.readAllBytes(path).length == blobSize
    }
}
