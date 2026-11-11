package com.parsa.calorietracker.csv;

import com.parsa.calorietracker.domain.FoodState;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IranianFoodCsvLoaderTest {

    @Test
    void loads_classpath_csv_with_at_least_40_rows_and_raw_cooked_rice() throws Exception {
        try (var in = getClass().getResourceAsStream("/data/iranian-foods.csv")) {
            assertThat(in).isNotNull();
            List<IranianFoodCsvLoader.CsvFoodRow> rows = IranianFoodCsvLoader.load(in);
            assertThat(rows).hasSizeGreaterThanOrEqualTo(40);

            var rawRice = rows.stream()
                    .filter(r -> r.food().getNameFa().equals("برنج خام"))
                    .findFirst()
                    .orElseThrow();
            var cookedRice = rows.stream()
                    .filter(r -> r.food().getNameFa().equals("برنج پخته"))
                    .findFirst()
                    .orElseThrow();

            assertThat(rawRice.food().getState()).isEqualTo(FoodState.RAW);
            assertThat(cookedRice.food().getState()).isEqualTo(FoodState.COOKED);
            assertThat(rawRice.food().getKcalPer100g()).isEqualByComparingTo("360");
            assertThat(cookedRice.food().getKcalPer100g()).isEqualByComparingTo("130");
        }
    }

    @Test
    void rejects_negative_nutrient() {
        String csv = """
                nameFa,name,kcalPer100g,protein,carb,fat,state,defaultPortionLabelFa,defaultPortionGrams,notes
                بد,Bad,-1,0,0,0,RAW,برش,30,bad
                """;
        assertThatThrownBy(() -> IranianFoodCsvLoader.load(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Negative");
    }

    @Test
    void splitCsv_handles_commas_inside_quotes() {
        String[] cols = IranianFoodCsvLoader.splitCsv("a,\"b,c\",d");
        assertThat(cols).containsExactly("a", "b,c", "d");
    }
}
