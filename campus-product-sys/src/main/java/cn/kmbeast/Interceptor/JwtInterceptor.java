package cn.kmbeast.Interceptor;

import cn.kmbeast.context.LocalThreadHolder;
import cn.kmbeast.pojo.api.ApiResult;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.utils.JwtUtil;
import com.alibaba.fastjson2.JSONObject;
import io.jsonwebtoken.Claims;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;

/**
 * token 拦截器，负责请求身份校验。
 */
public class JwtInterceptor implements HandlerInterceptor {

    /**
     * 前置拦截。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LocalThreadHolder.clear();

        String requestMethod = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(requestMethod)) {
            return true;
        }

        String token = request.getHeader("token");
        if (token == null || token.trim().isEmpty()) {
            Result<String> error = ApiResult.error("身份认证异常，请先登录");
            response.setContentType("application/json;charset=UTF-8");
            Writer stream = response.getWriter();
            stream.write(JSONObject.toJSONString(error));
            stream.flush();
            stream.close();
            return false;
        }

        Claims claims = JwtUtil.fromToken(token);
        if (claims == null) {
            Result<String> error = ApiResult.error("身份认证异常，请先登录");
            response.setContentType("application/json;charset=UTF-8");
            Writer stream = response.getWriter();
            stream.write(JSONObject.toJSONString(error));
            stream.flush();
            stream.close();
            return false;
        }

        Integer userId = JwtUtil.claimAsInteger(claims.get("id"));
        Integer roleId = JwtUtil.claimAsInteger(claims.get("role"));
        LocalThreadHolder.setUserId(userId, roleId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LocalThreadHolder.clear();
    }
}