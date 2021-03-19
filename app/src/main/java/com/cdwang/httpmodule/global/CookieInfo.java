package com.cdwang.httpmodule.global;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;

/**
 * copyright (C), 2020, 运达科技有限公司
 * fileName CookieInfo
 *
 * @author 王玺权
 * date 2020-11-30 13:44
 * description
 * history
 */
public class CookieInfo {
    //session缓存，----重要----
    public static HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    public static class CookieWrapper implements Serializable {
        public String domain;
        public String path;
        public String name;
        public String value;
        public long expiresAt;
        public boolean secure;
        public boolean httpOnly;
        public boolean persistent; // True if 'expires' or 'max-age' is present.

        public CookieWrapper() {
        }

        public CookieWrapper(Cookie cookie) {
            this.domain = cookie.domain();
            this.path = cookie.path();
            this.name = cookie.name();
            this.value = cookie.value();
            this.expiresAt = cookie.expiresAt();
            this.secure = cookie.secure();
            this.httpOnly = cookie.httpOnly();
            this.persistent = cookie.persistent();
        }

        public Cookie getOkhttpCookie() {
            Cookie.Builder builder = new Cookie.Builder().domain(domain)
                    .path(path)
                    .name(name)
                    .value(value)
                    .expiresAt(expiresAt);
            if (secure) {
                builder.secure();
            }
            if (httpOnly) {
                builder.httpOnly();
            }
            return builder.build();
        }
    }


}
