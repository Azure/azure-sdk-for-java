package com.azure.storage.blob.nio

import java.nio.file.Files

/**
 * This test class is for testing static helper methods provided by the JDK. Customers often rely on these methods
 * rather than using the file system or provider methods directly, so if a customer reports a scenario leverages one of
 * these methods and we need to add support for it, we should capture that here.
 */
class CompositeTest extends APISpec {
    def config = new HashMap<String, Object>()

    def setup() {
        config = initializeConfigMap()
    }

    def "Files createDirs"() {
        setup:
        def fs = createFS(config)

        when:
        def dirs = fs.getPath('mydir1/mydir2/mydir3')
        Files.createDirectories(dirs)

        then:
        Files.isDirectory(fs.getPath('mydir1'))
        Files.isDirectory(fs.getPath('mydir1/mydir2'))
        Files.isDirectory(fs.getPath('mydir1/mydir2/mydir3'))
    }
}
