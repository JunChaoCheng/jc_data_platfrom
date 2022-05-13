package com.jc.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class UserService {
    @Autowired
    RedisTemplate redisTemplate;

    private static final String encryptSalt = "F12839WhsnnEV$#23b";
    private static final String LOG_QUEUE = "log_queue";
    private static  final DateTimeFormatter FM_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);


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



}
