/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * 
 */
class FileInputChannel implements InputChannel {
    public FileInputChannel() {
    }

    public InputStream getInputStream(String name) {
        try {
            return new FileInputStream(name);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
