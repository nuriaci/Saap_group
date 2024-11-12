package es.storeapp.web.interceptors;

import es.storeapp.business.entities.User;
import es.storeapp.business.services.UserService;
import es.storeapp.common.Constants;
import es.storeapp.web.cookies.UserInfo;
import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
/*vulnerabilidad - deserilizacion insegura */
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoLoginInterceptor implements HandlerInterceptor {

    private final UserService userService;
    /* vulnerabilidad - deserilizacion insegura */
    private final String secretKey = System.getenv("JWT_SECRET_KEY");
    private static final Logger logger = LoggerFactory.getLogger(AutoLoginInterceptor.class);

    private Claims validateJwt(String jwt) throws JwtException {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody();
    }

    public AutoLoginInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        HttpSession session = request.getSession(true);
        if (session.getAttribute(Constants.USER_SESSION) != null || request.getCookies() == null) {
            return true;
        }
        for (Cookie c : request.getCookies()) {
            if (Constants.PERSISTENT_USER_COOKIE.equals(c.getName())) {
                String jwt = c.getValue();
                /* vulnerabilidad - deserilizacion insegura */
                if (jwt == null) {
                    continue;
                }
                try {
                    Claims claims = validateJwt(jwt);
                    String email = claims.get("email", String.class);
                    if (!"UserInfo".equals(claims.getSubject())) {
                        throw new SecurityException("Invalid object type");
                    }
                    User user = userService.findByEmail(email);
                    if (user != null) {
                        session.setAttribute(Constants.USER_SESSION, user);
                    }
                } catch (JwtException e) {
                    logger.warn("Invalid JWT token");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                    return false;
                }
            }
        }
        return true;
    }

}
