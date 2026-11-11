package com.parsa.calorietracker.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PersianDigitsTest {

    @Test
    void converts_latin_digits_for_display_only() {
        assertThat(PersianDigits.toPersian("52.0")).isEqualTo("۵۲.۰");
        assertThat(PersianDigits.format(new BigDecimal("87"))).isEqualTo("۸۷");
    }
}
