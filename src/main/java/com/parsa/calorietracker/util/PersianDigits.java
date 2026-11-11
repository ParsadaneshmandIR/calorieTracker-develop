package com.parsa.calorietracker.util;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Display-only conversion to Persian digits. Storage and APIs always use Latin digits.
 */
public final class PersianDigits {

    private static final char[] PERSIAN = {'۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'};

    private PersianDigits() {
    }

    public static String toPersian(String latin) {
        if (latin == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(latin.length());
        for (int i = 0; i < latin.length(); i++) {
            char c = latin.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append(PERSIAN[c - '0']);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String format(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return toPersian(value.stripTrailingZeros().toPlainString());
    }

    public static String format(BigDecimal value, int scale) {
        if (value == null) {
            return "";
        }
        return toPersian(value.setScale(scale, java.math.RoundingMode.HALF_UP).toPlainString());
    }

    public static String formatInt(int value) {
        return toPersian(Integer.toString(value));
    }

    public static String formatPercent(BigDecimal pct) {
        if (pct == null) {
            return "";
        }
        return toPersian(pct.stripTrailingZeros().toPlainString()) + "٪";
    }

    public static String formatLocaleNumber(Number number) {
        if (number == null) {
            return "";
        }
        return toPersian(String.format(Locale.ROOT, "%s", number));
    }
}
