package ru.t1.okhapkin.mystarterlogs.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
public class LoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class.getName());

    // Логирование перед выполнением метода
    @Before("execution(* ru.t1.okhapkin.taskmanager.service.*..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        logger.info("The method {}.{} was called with arguments: {}", className, methodName, Arrays.toString(args));
    }

    // Логирование после успешного выполнения метода
    @AfterReturning(pointcut = "execution(* ru.t1.okhapkin.taskmanager.service.*..*(..))")
    public void logAfterReturning(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        logger.info("The method {}.{} was successfully completed", className, methodName);
    }

    // Логирование при создании нового задания
    @AfterReturning(pointcut = "execution(* ru.t1.okhapkin.taskmanager.service.TaskService.createTask(..))", returning = "result")
    public void logAfterCreationNewTask(Task result) {
        logger.info("A new task was created with id: {}", result.getId());
    }

    // Логирование при изменении задания
    @AfterReturning(pointcut = "execution(* ru.t1.okhapkin.taskmanager.service.TaskService.updateTask(..))", returning = "result")
    public void logAfterUpdateTask(JoinPoint joinPoint, Task result) {
        Object[] args = joinPoint.getArgs();
        logger.info("The task was updated with id: {}. Arguments with updates: {}", result.getId(), Arrays.toString(args));
    }

    // Логирование при удалении задания
    @AfterReturning(pointcut = "execution(* ru.t1.okhapkin.taskmanager.service.TaskService.deleteTask(..))")
    public void logAfterDeletedTask(JoinPoint joinPoint) {
        logger.info("The task deleted with id: {}", joinPoint.getArgs()[0]);
    }

    // Логирование при возникновении исключения
    @AfterThrowing(pointcut = "execution(* ru.t1.okhapkin.taskmanager.service.*..*(..))", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        logger.error("The method {}.{} was completed with error: {}", className, methodName, exception.getMessage());
    }

    // Замер времени выполнения метода
    @Around("@annotation(ru.t1.okhapkin.taskmanager.aspect.annotaion.CustomTracking)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        try {
            Object result = joinPoint.proceed(); // Выполнение метода
            long endTime = System.currentTimeMillis();
            logger.info("The method {}.{} was completed for {} ms", className, methodName, endTime - startTime);
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("The method {}.{} was completed with error for {} ms", className, methodName, endTime - startTime);
            throw e;
        }
    }

}
