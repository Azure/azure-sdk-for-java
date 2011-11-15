/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

import java.io.InputStream;

/**
 * 
 */
interface RoleEnvironmentDataDeserializer {
    public RoleEnvironmentData deserialize(InputStream stream);
}
