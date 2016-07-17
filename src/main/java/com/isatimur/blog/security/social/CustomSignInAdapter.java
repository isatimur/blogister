package com.isatimur.blog.security.social;

import com.isatimur.blog.config.JHipsterProperties;
import com.isatimur.blog.security.jwt.TokenProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.context.request.ServletWebRequest;
import javax.servlet.http.Cookie;

import javax.inject.Inject;

public class CustomSignInAdapter implements SignInAdapter {

    @SuppressWarnings("unused")
    private final Logger log = LoggerFactory.getLogger(CustomSignInAdapter.class);

    @Inject
    private UserDetailsService userDetailsService;

    @Inject
    private JHipsterProperties jHipsterProperties;

    @Inject
    private TokenProvider tokenProvider;

    @Override
    public String signIn(String userId, Connection<?> connection, NativeWebRequest request){
        try {
            UserDetails user = userDetailsService.loadUserByUsername(userId);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            String jwt = tokenProvider.createToken(authenticationToken, false);
            ServletWebRequest servletWebRequest = (ServletWebRequest) request;
            servletWebRequest.getResponse().addCookie(getSocialAuthenticationCookie(jwt));
        } catch (AuthenticationException exception) {
            log.error("Social authentication error");
        }
        return jHipsterProperties.getSocial().getRedirectAfterSignIn();
    }

    private Cookie getSocialAuthenticationCookie(String token) {
        Cookie socialAuthCookie = new Cookie("social-authentication", token);
        socialAuthCookie.setPath("/");
        socialAuthCookie.setMaxAge(10);
        return socialAuthCookie;
    }
}
