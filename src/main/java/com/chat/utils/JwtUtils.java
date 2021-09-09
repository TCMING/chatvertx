package com.chat.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
//import com.chat.exception.AuthException;
//import com.chat.exception.AuthException2;
//import com.chat.exception.AuthException3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Lehr
 * @create: 2020-02-04
 */
public class JwtUtils {

    private static Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    /**
     签发对象：这个用户的id
     签发时间：现在
     有效时间：30分钟
     载荷内容：暂时设计为：这个人的名字，这个人的昵称
     加密密钥：这个人的id加上一串字符串
     */
    public static String createToken(String username) {

        return JWT.create().withAudience(username)   //签发对象
//                .withIssuedAt(new Date())    //发行时间
//                .withExpiresAt(expiresDate)  //有效时间
                .sign(Algorithm.HMAC256(username+"hello"));   //加密
    }

    public static String getAudience(String token) throws RuntimeException {
        try {
            return JWT.decode(token).getAudience().get(0);
        } catch (JWTDecodeException j) {
            return null;
        }
    }

    public static String parseUsername(String authorization){
        try {
            if (authorization == null) {
                return null;
            }
            String token = authorization.split(" ")[1];
            return JwtUtils.getAudience(token);
        } catch (Exception e) {
            return null;
        }

    }
}