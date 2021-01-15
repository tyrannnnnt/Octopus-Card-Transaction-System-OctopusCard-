package comp2026.OctopusCard.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtil {
    private static final String DateFormat = "dd-MM-yyyy@HH:mm:ss";

    public static String dateTime2Str(Date date) {
        return new SimpleDateFormat(DateFormat, Locale.ENGLISH).format(date);
    }

    public static Date str2DateTime(String dateTimeStr) throws ParseException {
        return new SimpleDateFormat(DateFormat, Locale.ENGLISH).parse(dateTimeStr);
    }
}
