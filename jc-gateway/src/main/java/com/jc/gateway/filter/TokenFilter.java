package com.jc.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.jc.gateway.base.ResultCodes;
import com.jc.gateway.entity.dto.UserDto;
import com.jc.gateway.entity.token.JWTToken;
import com.jc.gateway.entity.token.TokenCache;
import com.jc.gateway.service.UserService;
import com.jc.gateway.util.JwtUtils;
import com.jc.gateway.util.SpringContextUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TokenFilter extends AuthenticatingFilter {
    private static final Logger log = LoggerFactory.getLogger(TokenFilter.class);
    private static final int tokenRefreshInterval = 300;
    private UserService userService;

    public TokenFilter(UserService userService){
        this.userService = userService;
        this.setLoginUrl("/login.html");
    }

    @Override
    protected void postHandle(ServletRequest request, ServletResponse response) throws Exception {
        request.setAttribute("jwtShiroFilter.FILTERED", true);
    }

    /**
     * 这里重写了父类的方法，使用我们自己定义的Token类，提交给shiro。这个方法返回null的话会直接抛出异常，进入isAccessAllowed（）的异常处理逻辑。
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        String jwtToken = getAuthzHeader(servletRequest);
        JSONObject jsonObject = new JSONObject();
        servletResponse.setContentType("application/json;charset=UTF-8");
        if(StringUtils.isBlank(jwtToken)){
            jsonObject.put("code", ResultCodes.UNAUTHORIZED.getCode());
            jsonObject.put("msg",ResultCodes.UNAUTHORIZED.getMsg());
            jsonObject.put("data",ResultCodes.UNAUTHORIZED.getMsg());
            servletResponse.getWriter().write(jsonObject.toJSONString());
        }else if(JwtUtils.isTokenExpired(jwtToken)){
            jsonObject.put("code",ResultCodes.TOKEN_INVALID.getCode());
            jsonObject.put("msg",ResultCodes.TOKEN_INVALID.getMsg());
            jsonObject.put("data",ResultCodes.TOKEN_INVALID.getMsg());
            servletResponse.getWriter().write(jsonObject.toJSONString());
        }
        if(StringUtils.isNotBlank(jwtToken)&&!JwtUtils.isTokenExpired(jwtToken))
            return new JWTToken(jwtToken);

        return null;
    }


    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response,Object mappedValue) {
        ApplicationContext applicationContext = SpringContextUtil.getApplicationContext();
        TokenCache tokenCache = applicationContext.getBean(TokenCache.class);
        List<Map<String, String>> tokens = tokenCache.getTokens();
        String authzHeader = getAuthzHeader(request);
        for (Map<String, String> token : tokens) {
            Set<Map.Entry<String, String>> entries = token.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                if (entry.getValue().equals(authzHeader)){
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 权限校验失败，错误处理
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", ResultCodes.UNAUTHORIZED.getCode());
        jsonObject.put("msg", ResultCodes.UNAUTHORIZED.getMsg());
        jsonObject.put("data",ResultCodes.UNAUTHORIZED.getMsg());
        response.getWriter().write(jsonObject.toJSONString());
        httpResponse.sendRedirect("/login.html");
        return false;
    }

    protected String getAuthzHeader(ServletRequest request) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String header = httpRequest.getHeader("x-auth-token");
        return StringUtils.removeStart(header, "Bearer ");
    }

    protected boolean shouldTokenRefresh(Date issueAt){
        LocalDateTime issueTime = LocalDateTime.ofInstant(issueAt.toInstant(), ZoneId.systemDefault());
        return LocalDateTime.now().minusSeconds(tokenRefreshInterval).isAfter(issueTime);
    }

    protected void fillCorsHeader(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,HEAD");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
    }


}