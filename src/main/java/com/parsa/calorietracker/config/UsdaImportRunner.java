package com.parsa.calorietracker.config;

import com.parsa.calorietracker.service.ImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class UsdaImportRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsdaImportRunner.class);

    private final boolean enabled;
    private final String query;
    private final ImportService importService;

    public UsdaImportRunner(@Value("${app.import.usda:false}") boolean enabled,
                            @Value("${app.import.usda-query:apple}") String query,
                            ImportService importService) {
        this.enabled = enabled;
        this.query = query;
        this.importService = importService;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean flag = enabled || args.containsOption("import.usda");
        if (!flag) {
            return;
        }
        String q = args.containsOption("import.usda.query")
                ? args.getOptionValues("import.usda.query").getFirst()
                : query;
        log.info("Running USDA import job for query='{}'", q);
        var imported = importService.importUsda(q);
        log.info("USDA import finished: {} foods upserted", imported.size());
    }
}
