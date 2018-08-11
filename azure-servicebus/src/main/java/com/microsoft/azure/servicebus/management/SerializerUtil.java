package com.microsoft.azure.servicebus.management;

import java.time.Duration;

// Fields that require special serializations
public class SerializerUtil {
    
    public static String serializeDuration(Duration duration)
    {
        if(duration == null || duration.isNegative() || duration.isZero())
        {
            return "";
        }
        Duration remainingTime = duration;
        StringBuffer sb = new StringBuffer("P");
        long days = remainingTime.toDays();
        if(days > 0)
        {
            sb.append(days).append("D");
            remainingTime = duration.minusDays(days);
        }
        if(!remainingTime.isZero())
        {
            sb.append("T");
            long hours = remainingTime.toHours();
            if(hours > 0)
            {
                sb.append(hours).append("H");
                remainingTime = duration.minusHours(hours);
            }
            
            long minutes = remainingTime.toMinutes();
            if(minutes > 0)
            {
                sb.append(minutes).append("M");
                remainingTime = duration.minusMinutes(minutes);
            }
            
            long seconds = remainingTime.getSeconds();
            if(seconds > 0)
            {
                sb.append(seconds).append("S");
            }
        }
        
        return sb.toString();
    }
}
