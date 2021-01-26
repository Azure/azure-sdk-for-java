package com.azure.storage.blob.nio

import java.nio.file.Files
import java.nio.file.StandardCopyOption

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

    def "Files copy"() {
        setup:
        def fs = createFS(config)
        def dest = fs.getPath("dest")
        def resultArr = new byte[defaultDataSize]

        when:
        Files.copy(defaultInputStream.get(), dest)
        fs.provider().newInputStream(dest).read(resultArr)

        then:
        resultArr == defaultData.array()

        when:
        def dest2 = fs.getPath("dest2")
        def outStream = fs.provider().newOutputStream(dest2)
        Files.copy(dest, outStream)
        outStream.close()
        resultArr = new byte[defaultDataSize]
        fs.provider().newInputStream(dest2).read(resultArr)

        then:
        resultArr == defaultData.array()

        when:
        def dest3 = fs.getPath("dest3")
        Files.copy(dest, dest3, StandardCopyOption.COPY_ATTRIBUTES)
        resultArr = new byte[defaultDataSize]
        fs.provider().newInputStream(dest3).read(resultArr)

        then:
        resultArr == defaultData.array()
    }
}
