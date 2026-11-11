package com.parsa.calorietracker.config;

import com.parsa.calorietracker.util.PersianDigitHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.Set;

@Configuration
public class ThymeleafConfig {

    @Bean
    public PersianDigitsDialect persianDigitsDialect(PersianDigitHelper helper) {
        return new PersianDigitsDialect(helper);
    }

    public static final class PersianDigitsDialect extends AbstractDialect implements IExpressionObjectDialect {

        private final PersianDigitHelper helper;

        public PersianDigitsDialect(PersianDigitHelper helper) {
            super("persianDigits");
            this.helper = helper;
        }

        @Override
        public IExpressionObjectFactory getExpressionObjectFactory() {
            return new IExpressionObjectFactory() {
                @Override
                public Set<String> getAllExpressionObjectNames() {
                    return Set.of("pd");
                }

                @Override
                public Object buildObject(IExpressionContext context, String expressionObjectName) {
                    return helper;
                }

                @Override
                public boolean isCacheable(String expressionObjectName) {
                    return true;
                }
            };
        }
    }
}
