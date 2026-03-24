package cn.kmbeast.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Authentication and authorization guard annotation.
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Protector {

    /**
     * Built-in role constants used by {@link #roleCode()}.
     */
    int ROLE_ADMIN = 1;
    int ROLE_USER = 2;

    /**
     * Legacy role-name check (kept for backward compatibility).
     */
    String role() default "";

    /**
     * Role-code check.
     * <p>
     * Use {@link #ROLE_ADMIN} / {@link #ROLE_USER}.
     * Value {@code -1} means no role-code check.
     */
    int roleCode() default -1;
}
