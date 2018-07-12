package com.tabio.tabioapp.util;

import java.util.regex.Pattern;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/26/16.
 */
public class StringUtils {
    public static final String TAG = makeLogTag(StringUtils.class);

    public static final String getWithYen(int price) {
        return String.format("¥%1$,3d", price);
    }

    private static final String EMAIL_PATTERN =
            "([a-zA-Z0-9])+([a-zA-Z0-9\\.\\+_-])*@([a-zA-Z0-9_-])+([a-zA-Z0-9\\._-]+)+";
    public static boolean isValidEmail(final String email) {
        if (!email.trim().matches(EMAIL_PATTERN)) {
            return false;
        }
        return true;
    }

    public static boolean isValidPassword(final String password) {
        if (password.length() >= 6 && password.length() <= 50) {
            return true;
        }
        return false;
    }
}
