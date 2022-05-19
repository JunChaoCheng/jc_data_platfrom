package com.jc.gateway.entity.token;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author chengjunchao
 * @Version 1.0.0
 * @Date 2022/5/19
 */
@Component
@Data
public class TokenCache {
    private List<Map<String,String>> tokens = new ArrayList<>();

    public void addTokens(Map<String,String> token){
        tokens.add(token);
    }
}
