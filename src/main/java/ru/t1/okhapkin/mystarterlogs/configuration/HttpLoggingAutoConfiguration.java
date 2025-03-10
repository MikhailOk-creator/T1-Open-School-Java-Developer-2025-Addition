package ru.t1.okhapkin.mystarterlogs.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.t1.okhapkin.mystarterlogs.aspect.LoggingAspect;
import ru.t1.okhapkin.mystarterlogs.component.HttpLoggingProperties;

@Configuration
@EnableConfigurationProperties(HttpLoggingProperties.class)
public class HttpLoggingAutoConfiguration {

    private final HttpLoggingProperties httpLoggingProperties;

    public HttpLoggingAutoConfiguration(HttpLoggingProperties httpLoggingProperties) {
        this.httpLoggingProperties = httpLoggingProperties;
    }

    @Bean
    @ConditionalOnProperty(name = "http.logging.enable", havingValue = "true", matchIfMissing = true)
    public LoggingAspect httpLoggingAspect() {
        return new LoggingAspect(httpLoggingProperties);
    }

}
