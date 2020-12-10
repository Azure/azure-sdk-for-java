package com.azure.storage.blob.nio;

/**
 * Test class to validate functionality of methods on static helper classes like Paths and Files.
 */
public class CompositeTests extends  {
    def "Paths test"() {
        when:
        def fileSystem = createFS(config)
        def dirs = fileSystem.getPath( 'mydir1/mydir2/mydir3')
        Files.createDirectories(dirs)

        then:
        Files.isDirectory(fileSystem.getPath('mydir1'))
        Files.isDirectory(fileSystem.getPath('mydir1/mydir2'))
        Files.isDirectory(fileSystem.getPath('mydir1/mydir2/mydir3'))
    }
}
