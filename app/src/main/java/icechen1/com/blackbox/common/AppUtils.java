package icechen1.com.blackbox.common;

/**
 * Created by yuchen.hou on 15-07-11.
 */
public class AppUtils {

    public static long getBufferSavedTime(long start, long end, long bufferSize){
        long diff = end - start;
        long maxTime = bufferSize * 1000; //s -> ms
        return diff <= maxTime ? diff : maxTime;
    }
}
