package org.example.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jdk.nashorn.internal.runtime.Debug;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.BizException;
import org.example.model.LoginUser;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * JWT工具类
 */
@Slf4j
public class JwtUtil {

    /**
     * 过期时间，正常时7天，在这设置1天
     */
    private static final long TOKEN_EXPIRE = 1000 * 60 * 60 * 24 * 7;

    /**
     * 密钥
     */
    private static final String SECRET = "gaokd_class";

    /**
     * token前缀
     */
    private static final String TOKEN_PREFIX = "gaokd_class";

    /**
     * 颁布者subject
     */
    private static final String SUBJECT = "gaokd";

    /**
     * 生成token
     *
     * @param loginUser
     * @return
     */
    public static String geneJsonWebToken(LoginUser loginUser, HttpServletRequest request) {
        if (loginUser == null) {
            //空指针异常
            throw new NullPointerException();
        }
        Long userId = loginUser.getId();
        String token = Jwts.builder().setSubject(SUBJECT)
                .claim("id", userId)
                .claim("name", loginUser.getName())
                .claim("head_img", loginUser.getHeadImg())
                .claim("mail", loginUser.getMail())
                //当前时间时间
                .setIssuedAt(new Date())
                //过期时间：当前时间+过期时间
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRE))
                //如果需要更加安全的时候，可以在SECRET后绑定一个ip（CommonUtils.CommonUtil.getIpAddr()）
                .signWith(SignatureAlgorithm.HS256, SECRET+CommonUtil.getIpAddr(request))
                //返回字符串
                .compact();
        token = TOKEN_PREFIX + token;
        return token;
    }

    /**
     * 校验token
     */

    public static Claims checkJWT(String token,HttpServletRequest request) {
        try {

            //parser进行解析的方法
            Claims claims = Jwts.parser().setSigningKey(SECRET+CommonUtil.getIpAddr(request))
                    //把前缀替换掉然后再解析成claim
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody();
            log.info("claims:"+claims.toString());
            return claims;
        } catch (Exception e) {
            log.info("token解密失败");
            throw e;
        }
    }
}
