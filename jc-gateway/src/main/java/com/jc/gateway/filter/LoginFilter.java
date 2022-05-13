package com.jc.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.jc.gateway.enums.ResultCodes;
import org.apache.http.HttpStatus;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginFilter extends AuthorizationFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginFilter.class);

    @Override
    protected void postHandle(ServletRequest request, ServletResponse response) throws Exception {
        request.setAttribute("jwtShiroFilter.FILTERED", true);
    }


    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        String requestURI = ((HttpServletRequest) servletRequest).getRequestURI();
        System.out.println(requestURI);
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
        httpResponse.sendRedirect("/login");
        return false;
    }
}