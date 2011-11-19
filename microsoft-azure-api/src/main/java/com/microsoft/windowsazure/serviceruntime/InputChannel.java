/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

import java.io.InputStream;

/**
 * 
 */
interface InputChannel {
    public abstract InputStream getInputStream(String name);
}
