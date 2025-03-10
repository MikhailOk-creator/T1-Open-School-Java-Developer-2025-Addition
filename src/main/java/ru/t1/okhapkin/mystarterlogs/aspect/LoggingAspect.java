package ru.t1.okhapkin.mystarterlogs.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.t1.okhapkin.mystarterlogs.component.HttpLoggingProperties;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class.getName());

    private final HttpLoggingProperties properties;

    public LoggingAspect(HttpLoggingProperties properties) {
        this.properties = properties;
    }

    // Логирование перед выполнением метода
    @Before("@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void logBefore(JoinPoint joinPoint) {
        if (properties.enabled()) {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();
            generateLog(String.format("The method %s.%s was called with arguments: %s", className, methodName, Arrays.toString(args)));
        }
    }

    // Логирование после успешного выполнения метода
    @AfterReturning(pointcut = "@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void logAfterReturning(JoinPoint joinPoint) {
        if (properties.enabled()) {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            generateLog(String.format("The method %s.%s was successfully completed", className, methodName));
        }
    }


    // Логирование при изменении задания
    @AfterReturning(pointcut = "@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void logAfterUpdateTask(JoinPoint joinPoint) {
        if (properties.enabled()) {
            Object[] args = joinPoint.getArgs();
            generateLog(String.format("The task was updated with id: %s. Arguments with updates: %s", joinPoint.getArgs()[0], Arrays.toString(args)));
        }
    }

    // Логирование при возникновении исключения
    @AfterThrowing(pointcut =
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)",
            throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        if (properties.enabled()) {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            generateLog(String.format("The method %s.%s was completed with error: %s", className, methodName, exception.getMessage()));
        }
    }

    // Замер времени выполнения метода
    @Around("@annotation(ru.t1.okhapkin.mystarterlogs.aspect.annotaion.CustomTimeTracking)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.enabled()) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        try {
            Object result = joinPoint.proceed(); // Выполнение метода
            long endTime = System.currentTimeMillis();
            generateLog(String.format("The method %s.%s was completed for %d ms", className, methodName, endTime - startTime));
            return result;
        } catch (Exception e) {
            generateLog(String.format("The method %s.%s was completed with error", className, methodName));
            throw e;
        }
    }

    private void generateLog (String message) {
        String level = properties.level().toUpperCase();

        switch (level) {
            case "DEBUG": logger.debug(message); break;
            case "WARN": logger.warn(message); break;
            case "ERROR": logger.error(message); break;
            default: logger.info(message);
        }

    }

}
