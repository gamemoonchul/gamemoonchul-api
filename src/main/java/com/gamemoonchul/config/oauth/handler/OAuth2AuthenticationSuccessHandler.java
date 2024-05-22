package com.gamemoonchul.config.oauth.handler;

import com.gamemoonchul.application.MemberService;
import com.gamemoonchul.common.exception.BadRequestException;
import com.gamemoonchul.common.exception.InternalServerException;
import com.gamemoonchul.common.util.CookieUtils;
import com.gamemoonchul.config.jwt.TokenDto;
import com.gamemoonchul.config.jwt.TokenHelper;
import com.gamemoonchul.config.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.gamemoonchul.config.oauth.OAuth2UserPrincipal;
import com.gamemoonchul.domain.status.Oauth2Status;
import com.gamemoonchul.config.oauth.user.AppleOAuth2UserInfo;
import com.gamemoonchul.config.oauth.user.OAuth2Provider;
import com.gamemoonchul.config.oauth.user.OAuth2UserUnlinkManager;
import com.gamemoonchul.domain.entity.Member;
import com.gamemoonchul.application.converter.MemberConverter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import static com.gamemoonchul.config.oauth.HttpCookieOAuth2AuthorizationRequestRepository.MODE_PARAM_COOKIE_NAME;
import static com.gamemoonchul.config.oauth.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;


/**
 * 인증에 성공했을 경우를 담당
 * <p>
 * 로그인 플로우: onAuthenticationSuccess -> handleLoginOrSignUp -> signInOrUp -> determineTargetUrl -> clearAuthenticationAttributes -> sendRedirect
 * <p>
 * unlink 플로우: onAuthenticationSuccess -> handleLoginOrSignUp -> unlink -> determineTargetUrl -> clearAuthenticationAttributes -> sendRedirect
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final OAuth2UserUnlinkManager oAuth2UserUnlinkManager;
    private final TokenHelper tokenProvider;
    private final MemberService memberService;

    private final String TOKEN_DTO = "tokenDto";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // 로그인 또는 회원가입 처리
        handleSignIn(request, response, authentication);

        // 리다이렉트 URL 결정
        String targetUrl = determineTargetUrl(request);

        // 응답이 이미 커밋된 경우 리다이렉트를 수행할 수 없으므로 로그 남기고 종료
        if (response.isCommitted()) {
            log.error(Oauth2Status.EXPIRED_LOGIN.getMessage());
            throw new InternalServerException(Oauth2Status.EXPIRED_LOGIN);
        }

        clearAuthenticationAttributes(request, response);
        // 리다이렉트 수행
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    // 로그인 or 회원가입 처리
    private void handleSignIn(HttpServletRequest request, HttpServletResponse response,
                              Authentication authentication) {
        OAuth2UserPrincipal principal = getOAuth2UserPrincipal(authentication);

        if (principal == null) {
            log.error(Oauth2Status.LOGIN_FAILED.getMessage());
            throw new InternalServerException(Oauth2Status.LOGIN_FAILED);
        }

        String mode = CookieUtils.getCookie(request, MODE_PARAM_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse("");

        if ("login".equalsIgnoreCase(mode)) {
            try {
                System.out.println(principal.getUserInfo().getIdentifier().getBytes("UTF-8").length);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            TokenDto tokenDto = signIn(principal);
            request.setAttribute(TOKEN_DTO, tokenDto);  // 리다이렉트 URL에 토큰 정보 추가
        } else if ("unlink".equalsIgnoreCase(mode)) {
            unlink(principal);
        } else {
            log.error(Oauth2Status.LOGIN_FAILED.getMessage());
            throw new BadRequestException(Oauth2Status.LOGIN_FAILED);
        }
    }

    // 리다이렉트될 대상 URL을 결정
    protected String determineTargetUrl(HttpServletRequest request) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
        TokenDto tokenDto = (TokenDto) request.getAttribute(TOKEN_DTO);
        if (tokenDto != null) {
            targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("accessToken", tokenDto.getAccessToken())
                    .queryParam("refreshToken", tokenDto.getRefreshToken())
                    .build().toUriString();
        }
        return targetUrl;
    }

    private void unlink(OAuth2UserPrincipal principal) {
        String accessToken = principal.getUserInfo().getAccessToken();
        OAuth2Provider provider = principal.getUserInfo().getProvider();

        // TODO: Redis 리프레시 토큰 삭제
        oAuth2UserUnlinkManager.unlink(provider, accessToken);
        memberService.deactivateAccount(principal.getUserInfo().getEmail(), provider, principal.getUserInfo().getIdentifier());
    }

    private TokenDto signIn(OAuth2UserPrincipal principal) {
        // TODO: 리프레시 토큰 DB 저장
        log.info("email={}, name={}, nickname={}, accessToken={}", principal.getUserInfo().getEmail(),
                principal.getUserInfo().getName(),
                principal.getUserInfo().getNickname(),
                principal.getUserInfo().getAccessToken()
        );
        Member member = MemberConverter.toEntity(principal.getUserInfo());
        memberService.signIn(member);

        TokenDto tokenDto = tokenProvider.generateToken(principal.getUserInfo());
        return tokenDto;
    }

    private OAuth2UserPrincipal getOAuth2UserPrincipal(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2UserPrincipal) {
            return (OAuth2UserPrincipal) principal;
        } else if(principal instanceof OidcUser) {
            AppleOAuth2UserInfo appleOAuth2UserInfo = new AppleOAuth2UserInfo(((OidcUser) principal).getAttributes());
            return new OAuth2UserPrincipal(appleOAuth2UserInfo);
        }
        return null;
    }

    /**
     * 사용자의 인증과정에서 생성된 임시데이터나 쿠키를 정리하는 역할을 수행
     *
     * @param request
     * @param response
     */
    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
