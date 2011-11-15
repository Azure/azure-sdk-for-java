/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

import java.io.OutputStream;

/**
 * 
 */
interface CurrentStateSerializer {
    public void serialize(CurrentState state, OutputStream stream);
}
