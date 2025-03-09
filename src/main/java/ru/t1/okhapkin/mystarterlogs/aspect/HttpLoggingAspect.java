package ru.t1.okhapkin.mystarterlogs.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.t1.okhapkin.mystarterlogs.component.HttpLoggingProperties;

@Aspect
@Component
public class HttpLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(HttpLoggingAspect.class);

    private final HttpLoggingProperties properties;

    public HttpLoggingAspect(HttpLoggingProperties properties) {
        this.properties = properties;
    }

    @Before("@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void logRequest(JoinPoint joinPoint) {
        if (properties.enabled()) {
            String methodName = joinPoint.getSignature().getName();
            log.info("Request received: method {}", methodName);
        }
    }

    @AfterReturning(pointcut =
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)",
            returning = "result")
    public void logResponse(JoinPoint joinPoint, Object result) {
        if (properties.enabled()) {
            String methodName = joinPoint.getSignature().getName();
            log.info("Response sent: method {}, result: {}", methodName, result);
        }
    }

    @Around("@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object logHttpRequests(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.enabled()) {
            return joinPoint.proceed();
        }

        String level = properties.level().toUpperCase();
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long elapsedTime = System.currentTimeMillis() - start;
        String message = String.format("Executed method: %s, Time: %d ms", joinPoint.getSignature(), elapsedTime);

        switch (level) {
            case "DEBUG": log.debug(message); break;
            case "WARN": log.warn(message); break;
            case "ERROR": log.error(message); break;
            default: log.info(message);
        }

        return result;
    }

}
