package org.ovirt.engine.core.sso.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.api.extensions.aaa.Authn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum NonInteractiveAuth {
   b {
        @Override
        public String getName() {
            return "BasicAccept";
        }

        @Override
        public AuthResult doAuth(HttpServletRequest request, HttpServletResponse response) throws Exception {
            log.debug("Performing Basic Auth");
            Credentials credentials = SsoUtils.getUserCredentialsFromHeader(request);
            AuthResult retVal = new AuthResult();
            if (credentials == null || !SsoUtils.areCredentialsValid(request, credentials)) {
                retVal.setStatus(Authn.AuthResult.CREDENTIALS_INVALID);
            } else {
                retVal.setCredentials(credentials);
                AuthenticationUtils.handleCredentials(
                        SsoUtils.getSsoContext(request),
                        request,
                        credentials);
                retVal.setToken((String) request.getAttribute(SsoConstants.HTTP_REQ_ATTR_ACCESS_TOKEN));
                if (retVal.getToken() != null) {
                    log.debug("Basic authentication succeeded");
                    retVal.setStatus(Authn.AuthResult.SUCCESS);
                } else {
                    retVal.setStatus(Authn.AuthResult.CREDENTIALS_INVALID);
                }
            }
            return retVal;
        }
    },
    N {
        @Override
        public String getName() {
            return "Negotiate";
        }

        @Override
        public AuthResult doAuth(HttpServletRequest request, HttpServletResponse response) throws Exception {
            log.debug("Performing Negotiate Auth");
            NegotiateAuthUtils negoAuthUtils = SsoUtils.getSsoContext(request).getNegotiateAuthUtils();
            return negoAuthUtils.doAuth(request, response);
        }
    },
    OIDC {
        @Override
        public String getName() {
            return "OIDCBasicAccept";
        }

        @Override
        public AuthResult doAuth(HttpServletRequest request, HttpServletResponse response) throws Exception {
            log.debug("Performing OIDC Basic Auth");
            Credentials credentials = SsoUtils.getUserCredentialsFromHeader(request);
            if (credentials == null) {
                // get credentials from request parameters
                credentials = SsoUtils.getCredentials(request);
            }
            AuthResult retVal = new AuthResult();
            if (!SsoUtils.areCredentialsValid(request, credentials)) {
                retVal.setStatus(Authn.AuthResult.CREDENTIALS_INVALID);
            } else {
                retVal.setCredentials(credentials);
                ExternalOIDCUtils.handleCredentials(
                        SsoUtils.getSsoContext(request),
                        request,
                        credentials);
                retVal.setToken((String) request.getAttribute(SsoConstants.HTTP_REQ_ATTR_ACCESS_TOKEN));
                if (retVal.getToken() != null) {
                    log.debug("External OIDC Basic authentication succeeded");
                    retVal.setStatus(Authn.AuthResult.SUCCESS);
                } else {
                    retVal.setStatus(Authn.AuthResult.CREDENTIALS_INVALID);
                }
            }
            return retVal;
        }
    };

    private static Logger log = LoggerFactory.getLogger(NonInteractiveAuth.class);

    public abstract String getName();
    public abstract AuthResult doAuth(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
