package org.noear.solon.extend.sessionstate.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.noear.solon.Utils;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.SessionStateDefault;

/**
 * @author noear
 * @since 1.3
 */
public class JwtSessionState extends SessionStateDefault {
    /**
     * 单位：秒
     * */
    private static int _expiry = 60 * 60 * 2;
    private static String _domain = null;

    static {
        if (JwtProp.session_timeout > 0) {
            _expiry = JwtProp.session_timeout;
        }

        if (JwtProp.session_state_domain != null) {
            _domain = JwtProp.session_state_domain;
        }
    }

    private Context ctx;

    protected JwtSessionState(Context ctx) {
        this.ctx = ctx;
    }

    //
    // cookies control
    //

    public String cookieGet(String key) {
        return ctx.cookie(key);
    }

    public void cookieSet(String key, String val) {
        if (JwtProp.session_state_domain_auto) {
            if (_domain != null) {
                if (ctx.uri().getHost().indexOf(_domain) < 0) { //非安全域
                    ctx.cookieSet(key, val, null, _expiry);
                    return;
                }
            }
        }

        ctx.cookieSet(key, val, _domain, _expiry);
    }


    //
    // session control
    //

    @Override
    public String sessionId() {
        if (JwtProp.session_jwt_allowUseHeader) {
            return "";
        }

        String _sessionId = ctx.attr("sessionId", null);

        if (_sessionId == null) {
            _sessionId = sessionId_get();
            ctx.attrSet("sessionId", _sessionId);
        }

        return _sessionId;
    }

    private String sessionId_get() {
        String skey = cookieGet(SESSIONID_KEY);
        String smd5 = cookieGet(SESSIONID_MD5());

        if (Utils.isEmpty(skey) == false && Utils.isEmpty(smd5) == false) {
            if (Utils.md5(skey + SESSIONID_salt).equals(smd5)) {
                return skey;
            }
        }

        skey = Utils.guid();
        cookieSet(SESSIONID_KEY, skey);
        cookieSet(SESSIONID_MD5(), Utils.md5(skey + SESSIONID_salt));
        return skey;
    }

    private Claims sessionMap;

    protected Claims sessionMap() {
        if (sessionMap == null) {
            synchronized (this) {
                if (sessionMap == null) {
                    String sesId = sessionId();
                    String token = jwtGet();

                    if (Utils.isNotEmpty(token) && token.contains(".")) {
                        Claims claims = JwtUtils.parseJwt(token);

                        if(claims != null) {
                            if (JwtProp.session_jwt_allowUseHeader || sesId.equals(claims.getId())) {
                                if (JwtProp.session_jwt_allowExpire) {
                                    if (claims.getExpiration() != null &&
                                            claims.getExpiration().getTime() > System.currentTimeMillis()) {
                                        sessionMap = claims;
                                    }
                                } else {
                                    sessionMap = claims;
                                }
                            }
                        }
                    }

                    if (sessionMap == null) {
                        sessionMap = new DefaultClaims();
                    }

                    sessionToken = null;
                }
            }
        }

        return sessionMap;
    }


    @Override
    public Object sessionGet(String key) {
        return sessionMap().get(key);
    }

    @Override
    public void sessionSet(String key, Object val) {
        sessionMap().put(key, val);
        sessionToken = null;
    }

    @Override
    public void sessionClear() {
        sessionMap().clear();
        sessionToken = null;
    }

    @Override
    public void sessionRefresh() {
        if (JwtProp.session_jwt_allowUseHeader) {
            return;
        }

        String skey = cookieGet(SESSIONID_KEY);

        if (Utils.isNotEmpty(skey)) {
            cookieSet(SESSIONID_KEY, skey);
            cookieSet(SESSIONID_MD5(), Utils.md5(skey + SESSIONID_salt));
        }
    }

    @Override
    public void sessionPublish() {
        if (JwtProp.session_jwt_allowPublish) {
            String token = sessionToken();

            if (Utils.isNotEmpty(token)) {
                jwtSet(token);
            }
        }
    }

    private String sessionToken;
    @Override
    public String sessionToken() {
        if (sessionToken == null) {
            Claims tmp = sessionMap();

            if (tmp != null) {
                if (JwtProp.session_jwt_allowUseHeader && tmp.size() == 0) {
                    sessionToken = "";
                }

                if (sessionToken == null) {
                    String skey = sessionId();

                    if (JwtProp.session_jwt_allowUseHeader || Utils.isNotEmpty(skey)) {
                        tmp.setId(skey);

                        if (JwtProp.session_jwt_allowExpire) {
                            sessionToken = JwtUtils.buildJwt(tmp, _expiry * 1000L);
                        } else {
                            sessionToken = JwtUtils.buildJwt(tmp, 0);
                        }
                    }
                }
            }
        }

        return sessionToken;
    }

    @Override
    public boolean replaceable() {
        return false;
    }


    protected String jwtGet() {
        if (JwtProp.session_jwt_allowUseHeader) {
            return ctx.header(JwtProp.session_jwt_name);
        } else {
            return cookieGet(JwtProp.session_jwt_name);
        }
    }

    protected void jwtSet(String token) {
        if (JwtProp.session_jwt_allowUseHeader) {
            ctx.headerSet(JwtProp.session_jwt_name, token);
        } else {
            cookieSet(JwtProp.session_jwt_name, token);
        }

        ctx.attrSet(JwtProp.session_jwt_name, token);
    }
}
