package com.jc.gateway.config;


import com.jc.gateway.filter.LoginFilter;
import com.jc.gateway.realm.DbShiroRealm;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.mgt.SessionStorageEvaluator;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSessionStorageEvaluator;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.jc.gateway.service.UserService;
import org.apache.shiro.mgt.SecurityManager;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;

import java.util.Arrays;
import java.util.Map;

@Configuration
public class ShiroConfig {

    /**
     * 初始化Authenticator
     */
    @Bean
    public Authenticator authenticator() {
        ModularRealmAuthenticator authenticator = new ModularRealmAuthenticator();
        //设置两个Realm，一个用于用户登录验证和访问权限获取；一个用于jwt token的认证
        authenticator.setRealms(Arrays.asList(dbShiroRealm()));
        //设置多个realm认证策略，一个成功即跳过其它的
        authenticator.setAuthenticationStrategy(new FirstSuccessfulStrategy());
        return authenticator;
    }

    @Bean
    public Authorizer authorizer() {
        ModularRealmAuthorizer authorizer = new ModularRealmAuthorizer();
        return authorizer;
    }



    /**
     * 注册shiro的Filter，拦截请求
     */
    @Bean
    public FilterRegistrationBean<Filter> filterRegistrationBean(SecurityManager securityManager, UserService userService) throws Exception{
        FilterRegistrationBean<Filter> filterRegistration = new FilterRegistrationBean<Filter>();
        filterRegistration.setFilter((Filter)shiroFilter(securityManager, userService).getObject());
        filterRegistration.addInitParameter("targetFilterLifecycle", "true");
        filterRegistration.setAsyncSupported(true);
        filterRegistration.setEnabled(true);
        filterRegistration.setDispatcherTypes(DispatcherType.REQUEST);

        return filterRegistration;
    }


    /**
     * 禁用session, 不保存用户登录状态。保证每次请求都重新认证。
     * 需要注意的是，如果用户代码里调用Subject.getSession()还是可以用session，如果要完全禁用，要配合下面的noSessionCreation的Filter来实现
     */
    @Bean
    protected SessionStorageEvaluator sessionStorageEvaluator(){
        DefaultWebSessionStorageEvaluator sessionStorageEvaluator = new DefaultWebSessionStorageEvaluator();
        sessionStorageEvaluator.setSessionStorageEnabled(false);
        return sessionStorageEvaluator;
    }

    /**
     * 用于用户名密码登录时认证的realm
     */
    @Bean("dbRealm")
    public Realm dbShiroRealm() {
        DbShiroRealm myShiroRealm = new DbShiroRealm();
        return myShiroRealm;
    }

    /**
     * 用于JWT token认证的realm
     */
//    @Bean("jwtRealm")
//    public Realm jwtShiroRealm(UserService userService) {
//        JWTShiroRealm myShiroRealm = new JWTShiroRealm(userService);
//        return myShiroRealm;
//    }

    /**
     * 设置过滤器 将自定义的Filter加入
     */
    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager, UserService userService) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager);
        Map<String, Filter> filterMap = factoryBean.getFilters();
        filterMap.put("authcToken", createAuthFilter());
        factoryBean.setFilters(filterMap);
        factoryBean.setFilterChainDefinitionMap(shiroFilterChainDefinition().getFilterChainMap());

        return factoryBean;
    }
    @Bean
    protected ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();
        //login不做认证，noSessionCreation的作用是用户在操作session时会抛异常
        chainDefinition.addPathDefinition("/user/login", "noSessionCreation,anon");
        chainDefinition.addPathDefinition("/schedule-center/websocketLog/**", "noSessionCreation,anon");
        //数据中心bs-hive websocket接口放开认证
        chainDefinition.addPathDefinition("/bs-hive/hiveWebSocketServer/**", "noSessionCreation,anon");
        //放开bi登录认证
        chainDefinition.addPathDefinition("/bi/userLogin", "noSessionCreation,anon");
        //圆心惠宝放开校验
        chainDefinition.addPathDefinition("/yxhb/free/**", "noSessionCreation,anon");
        //放开元数据登录认证
        chainDefinition.addPathDefinition("/**/login", "noSessionCreation,anon");
        chainDefinition.addPathDefinition("/**/downloadRecord/**", "noSessionCreation,anon");
        //做用户认证，permissive参数的作用是当token无效时也允许请求访问，不会返回鉴权未通过的错误
        chainDefinition.addPathDefinition("/user/logout", "noSessionCreation,authcToken[permissive]");
        //chainDefinition.addPathDefinition("/image/**", "anon");
        //chainDefinition.addPathDefinition("/admin/**", "noSessionCreation,authcToken,anyRole[admin,manager]"); //只允许admin或manager角色的用户访问
        //chainDefinition.addPathDefinition("/article/list", "noSessionCreation,authcToken");
        //chainDefinition.addPathDefinition("/article/*", "noSessionCreation,authcToken[permissive]");
        //获取用户信息 只需要token 不需要角色校验
        /*chainDefinition.addPathDefinition("/user/getUserInfo", "noSessionCreation,authcToken");
        chainDefinition.addPathDefinition("/umc/helper/**", "noSessionCreation,authcToken");
        chainDefinition.addPathDefinition("/umc/user/getUserRouteList", "noSessionCreation,authcToken");*/
        // 默认进行用户鉴权
        //chainDefinition.addPathDefinition("/umc/route/**", "noSessionCreation,anon");
        chainDefinition.addPathDefinition("/**", "noSessionCreation,authcToken");
        return chainDefinition;
    }

    //注意不要加@Bean注解，不然spring会自动注册成filter
    protected LoginFilter createAuthFilter(){
        return new LoginFilter();
    }



}
