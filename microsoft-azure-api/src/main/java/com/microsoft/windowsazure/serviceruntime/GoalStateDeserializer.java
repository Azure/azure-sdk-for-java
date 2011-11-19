/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

import java.io.InputStream;

/**
 * 
 */
interface GoalStateDeserializer {
    public void initialize(InputStream inputStream);

    public GoalState deserialize();
}
