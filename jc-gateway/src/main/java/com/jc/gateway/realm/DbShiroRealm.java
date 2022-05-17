package com.jc.gateway.realm;


import com.jc.gateway.entity.User;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DbShiroRealm extends AuthorizingRealm {
    private final Logger log = LoggerFactory.getLogger(DbShiroRealm.class);
    //数据库存储的用户密码的加密salt，正式环境不能放在源代码里
    private static final String encryptSalt = "F12839WhsnnEV$#23b";


    public DbShiroRealm() {
        //因为数据库中的密码做了散列，所以使用shiro的散列Matcher
//        this.setCredentialsMatcher(new SimpleCredentialsMatcher());
    }
    /**
     *  找它的原因是这个方法返回true
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordToken;
    }

    /**
     *  这一步我们根据token给的用户名，去数据库查出加密过用户密码，然后把加密后的密码和盐值一起发给shiro，让它做比对
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken userpasswordToken = (UsernamePasswordToken)token;
//        String username = userpasswordToken.getUsername();
//        String password = String.valueOf(userpasswordToken.getPassword());
        User user = new User();
        user.setUsername("");
        user.setPassword("");

        //用户登录成功 调取刷新api接口
        //mscUserService.saveUserApis(username);
        return new SimpleAuthenticationInfo(user, user.getPassword(), ByteSource.Util.bytes(encryptSalt), "dbRealm");
    }


    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        return simpleAuthorizationInfo;
    }



}
