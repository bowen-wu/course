package com.personal.course.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetKeyFromUrlUtil {
    public static Pattern KEY_PATTERN = Pattern.compile("com\\/.*\\?");

    public static String getKeyFromUrl(String url) {
        String result = "";
        Matcher matcher = KEY_PATTERN.matcher(url);
        if (matcher.find()) {
            result = matcher.group(0);
        }
        return result.substring(result.indexOf("/") + 1, result.indexOf("?"));
    }
}
