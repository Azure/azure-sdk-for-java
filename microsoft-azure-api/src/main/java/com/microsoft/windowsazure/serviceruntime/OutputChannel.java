/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

import java.io.OutputStream;

/**
 * 
 */
interface OutputChannel {
    public abstract OutputStream getOutputStream(String name);
}
