/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

/**
 * 
 */
interface RuntimeClientFactory {
    public String getVersion();

    public RuntimeClient createRuntimeClient(String path);
}
