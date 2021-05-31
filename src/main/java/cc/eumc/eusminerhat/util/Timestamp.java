package cc.eumc.eusminerhat.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Timestamp {
    public static long getSecondsSince1970() {
        return System.currentTimeMillis() / 1000;
    }
    public static String toFormattedTime(long secondSince1970) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX").format(new Date(secondSince1970 * 1000));
    }
}
