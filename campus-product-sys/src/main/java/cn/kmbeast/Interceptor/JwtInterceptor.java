package cn.kmbeast.Interceptor;

import cn.kmbeast.context.LocalThreadHolder;
import cn.kmbeast.pojo.api.ApiResult;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.utils.JwtUtil;
import com.alibaba.fastjson2.JSONObject;
import io.jsonwebtoken.Claims;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;

/**
 * JWT interceptor that validates request identity before entering controllers.
 */
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LocalThreadHolder.clear();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return writeAuthError(response, "authentication failed, please login first");
        }

        Claims claims = JwtUtil.fromToken(token);
        if (claims == null) {
            return writeAuthError(response, "authentication failed, please login first");
        }

        Integer userId = JwtUtil.claimAsInteger(claims.get("id"));
        Integer roleId = JwtUtil.claimAsInteger(claims.get("role"));
        if (userId == null || roleId == null) {
            return writeAuthError(response, "authentication failed, invalid token payload");
        }
        LocalThreadHolder.setUserId(userId, roleId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LocalThreadHolder.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (StringUtils.hasText(token)) {
            return token.trim();
        }
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        return null;
    }

    private boolean writeAuthError(HttpServletResponse response, String message) throws Exception {
        Result<String> error = ApiResult.error(message);
        response.setContentType("application/json;charset=UTF-8");
        Writer writer = response.getWriter();
        writer.write(JSONObject.toJSONString(error));
        writer.flush();
        writer.close();
        return false;
    }
}
