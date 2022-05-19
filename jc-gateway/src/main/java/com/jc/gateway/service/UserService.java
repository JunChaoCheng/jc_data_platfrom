package com.jc.gateway.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.jc.gateway.entity.dto.UserDto;
import com.jc.gateway.util.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    RedisTemplate redisTemplate;

    private static final String encryptSalt = "123";
    private static final String LOG_QUEUE = "log_queue";
    private static  final DateTimeFormatter FM_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);


    /**
     * 保存user登录信息，返回token
     */
    public String generateJwtToken(String username ,char [] password) {
        //String salt = "12345";//JwtUtils.generateSalt();
        String salt = JwtUtils.generateSalt();
        /**
         * @todo 将salt保存到数据库或者缓存中
         * redisTemplate.opsForValue().set("token:"+username, salt, 3600*2*8, TimeUnit.SECONDS);
         */
//        redisTemplate.opsForValue().set("token:"+username, salt, 3600*2*8, TimeUnit.SECONDS);
        //将生成的token保存到redis
        String token = JwtUtils.sign(username, salt, 3600*2*8);
        //将盐和username 生产key
//        try {
//            LOGGER.info("set token the key is {}",getMD5Str(salt+username));
//            redisTemplate.opsForValue().set(token, username, 3600*2*8, TimeUnit.SECONDS);
//            username = getMD5Str(username+String.valueOf(password));
//            redisTemplate.opsForValue().set(getMD5Str(salt+username), token, 3600*2*8, TimeUnit.SECONDS);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return token; //生成jwt token，设置过期时间为1小时
    }

    /**
     * 获取上次token生成时的salt值和登录用户信息
     * @param username
     * @return
     */
    public UserDto getJwtTokenInfo(String username) {
        //String salt = "12345";
        /**
         * @todo 从数据库或者缓存中取出jwt token生成时用的salt
         * salt = redisTemplate.opsForValue().get("token:"+username);
         */
//        String salt = (String) redisTemplate.opsForValue().get("token:"+username);
        String salt = "12345";
        UserDto user = getUserInfo(username);
        user.setSalt(salt);
        return user;
    }

    /**
     * 判断用户是否登录
     * @param username
     * @return
     */
    public String checkoutUser(String username, char [] password){
        String salt = (String) redisTemplate.opsForValue().get("token:"+username);
        LOGGER.info( "the salt is {}",salt);
        if (StringUtils.isEmpty(salt)){
            return  null;
        }
        try {
            username = getMD5Str(username+String.valueOf(password));
            LOGGER.info("get token the key is {}",getMD5Str(salt+username));
            String token = (String) redisTemplate.opsForValue().get(getMD5Str(salt+username));

            return token;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 清除token信息
     */
    public void deleteLoginInfo(String username,char [] password) {
        /**
         * @todo 删除数据库或者缓存中保存的salt
         * redisTemplate.delete("token:"+username);
         */
        String salt = (String) redisTemplate.opsForValue().get("token:"+username);
        try {
            //推出登录 删除token 和盐
            redisTemplate.delete("token:"+username);
            username = getMD5Str(username+String.valueOf(password));
            redisTemplate.delete(getMD5Str(salt+username));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取数据库中保存的用户信息，主要是加密后的密码
     * @param userName
     * @return
     */
    public UserDto getUserInfo(String userName) {
        UserDto user = new UserDto();
        user.setUserId(1L);
        user.setUsername(userName);
        //user.setEncryptPwd(new Sha256Hash("123456", encryptSalt).toHex());
        return user;
    }


    /**
     * 获取用户角色列表，强烈建议从缓存中获取
     * @param userId
     * @return
     */
    public List<String> getUserRoles(Long userId){
        System.out.println("122222222222");
        return Arrays.asList("admin");
    }

    /**
     *
     * @param str
     * @return
     * @throws Exception
     */
    public String getMD5Str(String str) throws Exception {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            throw new Exception("MD5加密出现错误，"+e.toString());
        }
    }


    public List<String> getNoAuthApis() {
        List<String> noAuthApiList = new ArrayList<>();
        String noAuthApi = (String)redisTemplate.opsForValue().get("noAuthApi");
        noAuthApiList = JSONObject.parseArray(noAuthApi, String.class);
        return noAuthApiList == null ? new ArrayList<String>() : noAuthApiList;
    }
    public List<String> getSuperUser() {
        List<String> superUserList = new ArrayList<>();
        String superUser = (String)redisTemplate.opsForValue().get("superUser");
        superUserList = JSONObject.parseArray(superUser, String.class);
        return superUserList == null ? new ArrayList<String>() : superUserList;
    }


    public void saveLoginLog(String userName,String token,HttpServletRequest request){
        JSONObject jsonObject = getRequestParam(request);
        String uuid = request.getAttribute("uuid").toString();
        jsonObject.put("token",token);
        jsonObject.put("method",request.getMethod());
        jsonObject.put("userName",userName);
        jsonObject.put("uuid", uuid);
        jsonObject.put("params","{\"userName\":\""+userName+"\",\"password\":\"******\"}");
        //kafkaSendMsgUtil.senMsg(jsonObject.toJSONString());
        redisTemplate.opsForValue().set("log_"+uuid,jsonObject.toJSONString(),30,TimeUnit.MINUTES);
        //redisTemplate.opsForList().leftPush(LOG_QUEUE,jsonObject.toJSONString());
        //ApiLog apiLog = JSONObject.parseObject(jsonObject.toJSONString(), ApiLog.class);
        LOGGER.info("send success{}",jsonObject.toJSONString());
        //apiLogMapper.insertSelective(apiLog);

    }

    public JSONObject getRequestParam(HttpServletRequest request){
        JSONObject jsonObject = new JSONObject();
        try {
            String uuid = request.getAttribute("uuid").toString();
            Long requestTime = Long.parseLong(request.getAttribute("request_time").toString());
            String requestURI = request.getRequestURI();
            String requestURL = request.getRequestURL().toString();
            String userAgent = request.getHeader("user-agent");

            String ip = request.getHeader("X-Real-IP");
            if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            LOGGER.info("the url is {}", requestURI);
            String token = request.getHeader("x-auth-token");
            String referer = request.getHeader("referer");
            Map<String ,String> result = new HashMap<>();
            //当天
            jsonObject.put("uuid", uuid);
            jsonObject.put("token", token);
            jsonObject.put("url", requestURL);
            jsonObject.put("uri", requestURI);
            jsonObject.put("userAgent", userAgent);
            jsonObject.put("clientIp", ip);
            jsonObject.put("referer", referer);
            jsonObject.put("requestTime", requestTime);
            jsonObject.put("apiName",result == null ? "":result.get("api_name"));
            jsonObject.put("moduleName",result == null ? "":result.get("module_name"));
            return jsonObject;
        }catch (Exception e){
            LOGGER.info("the exception is {}",e);
            return jsonObject;
        }
    }


    public UserDto authenticate() {
        UserDto userDto = new UserDto();
        userDto.setUsername("111");
        userDto.setEncryptPwd(new Sha256Hash("111", encryptSalt).toHex());
        userDto.setSalt("123");
        return userDto;
    }
}
