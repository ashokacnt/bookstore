package com.cg.config;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RepositoryLoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(RepositoryLoggingAspect.class);

    @Around("execution(* com.example.bookstorev6..*Repository+.*(..))")
    public Object log(org.aspectj.lang.ProceedingJoinPoint p) throws Throwable {
        long s = System.currentTimeMillis();
        try {
            return p.proceed();
        } finally {
            log.info("repository method={} durationMs={}", p.getSignature().toShortString(), System.currentTimeMillis() - s);
        }
    }
}
