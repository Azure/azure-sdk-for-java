/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * 
 */
class FileOutputChannel implements OutputChannel {
    public FileOutputChannel() {
    }

    public OutputStream getOutputStream(String name) {
        try {
            return new FileOutputStream(name);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
