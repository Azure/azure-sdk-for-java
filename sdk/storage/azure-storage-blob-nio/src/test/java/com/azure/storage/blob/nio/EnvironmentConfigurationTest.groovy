package com.azure.storage.blob.nio

import com.azure.core.util.BinaryData
import com.azure.core.util.Configuration
import com.azure.storage.common.implementation.Constants

import spock.lang.Isolated

import java.nio.file.FileSystemNotFoundException
import java.nio.file.Files
import java.nio.file.Paths

import static com.azure.storage.blob.nio.AzureFileSystem.EnvironmentConfigurationConstants

@Isolated
class EnvironmentConfigurationTest  extends APISpec {
    Map<String, String> environmentConfig
    String blobName

    def setup() {
        environmentConfig = [
            (EnvironmentConfigurationConstants.DEFAULT_BLOB_ENDPOINT)             : "https://${environment.primaryAccount.name}.blob.core.windows.net",
            (EnvironmentConfigurationConstants.SHARED_KEY_CREDENTIAL_ACCOUNT_NAME): environment.primaryAccount.name,
            (EnvironmentConfigurationConstants.SHARED_KEY_CREDENTIAL_ACCOUNT_KEY) : environment.primaryAccount.key,
            (EnvironmentConfigurationConstants.FILE_STORES)                       : cc.getBlobContainerName(),
            (EnvironmentConfigurationConstants.AUTO_CREATE_FILESYSTEMS)           : "true",
            (EnvironmentConfigurationConstants.SKIP_CONTAINER_CHECK)              : "true",
        ]
        blobName = generateBlobName()

        cc.create()
        cc.getBlobClient(blobName).upload(data.defaultInputStream, data.defaultDataSize)

        def globalConfig = Configuration.getGlobalConfiguration()
        environmentConfig.each { key, val -> globalConfig.put(key, val)}
    }

    def cleanup() {
        def globalConfig = Configuration.getGlobalConfiguration()
        environmentConfig.each { key, val -> globalConfig.remove(key)}

        cc.deleteIfExists()
    }

    def "Path auto resolves"() {
        when:
        // account name not present in uri
        def path = Paths.get(new URI("azb://${cc.getBlobContainerName()}:/$blobName"))

        then:
        notThrown(FileSystemNotFoundException)
        path.toString() == blobName
    }

    def "Explicit create with env configs"() {
        when:
        new AzureFileSystemProvider().newFileSystem(new URI("azb://?endpoint=https://foo.blob.core.windows.net"), null)

        then:
        // insufficient configuration will throw. no throw means env config was picked up
        notThrown(Exception)
    }
}
