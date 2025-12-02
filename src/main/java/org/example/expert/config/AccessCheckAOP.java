package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.example.expert.domain.common.dto.AuthUser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AccessCheckAOP {
    @Pointcut("@annotation(org.example.expert.config.OnlyAdmin)")
    public void adminApi() {}

    @Around("adminApi()")
    public Object accessLogToInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        StringBuilder logInfo = new StringBuilder();

        //유저 ID, 요청 URI 정보는 시그니처에 없어서 RequestContextHolder의 getRequestAttributes 메서드를 사용
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        Long userId = (Long) request.getAttribute("userId");
        logInfo.append(String.format("유저 ID : %s/ ",userId));

        LocalDateTime accessTime = LocalDateTime.now();
        logInfo.append(String.format("요청 시각: %s/ ",accessTime));

        logInfo.append(String.format("요청 URI : %s/ ",request.getRequestURI()));

        //request body 가져오기
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg == null) continue;
            if (arg instanceof String || arg instanceof Number) continue;
            if (arg instanceof AuthUser) continue;
            //이 시점에는 @RequestBody 확률 올라감
            String packageName = arg.getClass().getPackageName();
            if (packageName.contains("dto.request")) {
                logInfo.append(String.format("Request Body : %s",getRequestBody(arg)));
            }
        }
        log.info(logInfo.toString());
        // 실제 메서드 실행 -> Filter에서 doFilter 와 비슷함.
        return joinPoint.proceed();
    }

    public String getRequestBody(Object arg) throws IOException {
        //JSON으로 직력화해서 로그로 남김
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(arg);
    }


}
