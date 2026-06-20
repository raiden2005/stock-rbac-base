package com.stock.rbac.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "rbac")
public class RbacConfigUtil {

    private List<String> whitelist = new ArrayList<>();

    private String loginPage = "/login";

    private JwtConfig jwt = new JwtConfig();

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    public boolean isWhitelist(String uri) {
        if (uri == null || whitelist == null) {
            return false;
        }
        for (String pattern : whitelist) {
            if (MATCHER.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    public String getLoginPage() {
        return loginPage;
    }

    public void setLoginPage(String loginPage) {
        this.loginPage = loginPage;
    }

    public JwtConfig getJwt() {
        return jwt;
    }

    public void setJwt(JwtConfig jwt) {
        this.jwt = jwt;
    }

    public static class JwtConfig {
        private String secret;
        private long expire;
        private String header = "Authorization";
        private String prefix = "Bearer ";

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpire() {
            return expire;
        }

        public void setExpire(long expire) {
            this.expire = expire;
        }

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }
}
