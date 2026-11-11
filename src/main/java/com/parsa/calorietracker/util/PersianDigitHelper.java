package com.parsa.calorietracker.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("pd")
public class PersianDigitHelper {

    public String d(String latin) {
        return PersianDigits.toPersian(latin);
    }

    public String n(BigDecimal value) {
        return PersianDigits.format(value);
    }

    public String n1(BigDecimal value) {
        return PersianDigits.format(value, 1);
    }

    public String i(int value) {
        return PersianDigits.formatInt(value);
    }

    public String pct(BigDecimal value) {
        return PersianDigits.formatPercent(value);
    }

    public boolean isPartialEdible(BigDecimal ediblePortionPct) {
        return ediblePortionPct != null
                && ediblePortionPct.compareTo(new BigDecimal("100")) < 0;
    }

    public String stateLabel(String state) {
        if (state == null) {
            return "";
        }
        return switch (state) {
            case "RAW" -> "خام";
            case "COOKED" -> "پخته";
            case "PACKAGED" -> "بسته‌بندی‌شده";
            default -> state;
        };
    }
}
