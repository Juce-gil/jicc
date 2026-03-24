package cn.kmbeast.aop;

import cn.kmbeast.context.LocalThreadHolder;
import cn.kmbeast.pojo.api.ApiResult;
import cn.kmbeast.pojo.em.RoleEnum;
import cn.kmbeast.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * Method-level authorization guard.
 */
@Aspect
@Component
public class ProtectorAspect {

    @Around("@annotation(cn.kmbeast.aop.Protector)")
    public Object auth(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return ApiResult.error("authentication context is unavailable");
        }

        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) {
            return ApiResult.error("authentication failed, please login first");
        }

        Claims claims = JwtUtil.fromToken(token);
        if (claims == null) {
            return ApiResult.error("authentication failed, please login first");
        }

        Integer userId = JwtUtil.claimAsInteger(claims.get("id"));
        Integer roleId = JwtUtil.claimAsInteger(claims.get("role"));
        if (userId == null || roleId == null) {
            return ApiResult.error("authentication failed, invalid token payload");
        }

        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Protector protectorAnnotation = signature.getMethod().getAnnotation(Protector.class);
        if (protectorAnnotation == null) {
            return ApiResult.error("authentication failed, missing protector annotation");
        }

        if (!hasRequiredRole(protectorAnnotation, roleId)) {
            return ApiResult.error("permission denied");
        }

        LocalThreadHolder.setUserId(userId, roleId);
        try {
            return proceedingJoinPoint.proceed();
        } finally {
            LocalThreadHolder.clear();
        }
    }

    private boolean hasRequiredRole(Protector protectorAnnotation, Integer currentRoleId) {
        if (currentRoleId == null) {
            return false;
        }

        int requiredRoleCode = protectorAnnotation.roleCode();
        if (requiredRoleCode > 0 && !Objects.equals(requiredRoleCode, currentRoleId)) {
            return false;
        }

        String requiredRoleName = protectorAnnotation.role();
        if (StringUtils.hasText(requiredRoleName)
                && !Objects.equals(RoleEnum.ROLE(currentRoleId), requiredRoleName)) {
            return false;
        }

        return true;
    }
}
