package io.acriox.playlisthelper.api;

import io.acriox.playlisthelper.api.dto.SpotifyTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/v1/spotify/oauth")
@Slf4j
public class SpotifyOAuthController {

    private final List<String> API_SCOPES = List.of(
            "user-read-email",
            "playlist-modify-public",
            "playlist-modify-private",
            "playlist-read-private",
            "playlist-read-collaborative",
            "user-library-modify",
            "user-library-read");
    private final String SPOTIFY_AUTHORIZATION_URL = "https://accounts.spotify.com/authorize";
    private final String SPOTIFY_TOKEN_URL = "https://accounts.spotify.com/api/token";

    private final String clientId;
    private final String clientSecret;
    private final String callbackUrl;

    private final RestTemplate restTemplate;

    public SpotifyOAuthController(
            @Value("${spotify.client-id}") String clientId,
            @Value("${spotify.client-secret}") String clientSecret,
            @Value("${spotify.callback-url}") String callbackUrl,
            RestTemplate restTemplate) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.callbackUrl = callbackUrl;
        this.restTemplate = restTemplate;
    }

    @GetMapping("login")
    public View login() {
        return new RedirectView(buildAuthorizationUrl());
    }

    private String buildAuthorizationUrl() {
        return SPOTIFY_AUTHORIZATION_URL
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&scope=" + getUrlencoded(Strings.join(API_SCOPES, ' '))
                + "&redirect_uri=" + getUrlencoded(callbackUrl);
    }

    @GetMapping("callback")
    public View callback(@RequestParam String code) {

        /* Configure Spotify authentication */
        restTemplate.getInterceptors()
                .add(new BasicAuthenticationInterceptor(clientId, clientSecret));

        MultiValueMap<String, String> postBody = new LinkedMultiValueMap<>();
        postBody.add("grant_type", "authorization_code");
        postBody.add("code", code);
        postBody.add("redirect_uri", callbackUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);


        log.info("Sending API token request");

        ResponseEntity<SpotifyTokenResponse> response =
                restTemplate.exchange(SPOTIFY_TOKEN_URL, HttpMethod.POST, new HttpEntity<>(postBody, headers), SpotifyTokenResponse.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.warn("Spotify API request failed: {}", response);
        }

        var tokenResponse = response.getBody();
        log.info("Spotify API token: {}", tokenResponse);

        return new RedirectView("/");
    }

    private String getUrlencoded(String source) {
        return UriUtils.encodeQueryParam(source, StandardCharsets.UTF_8);
    }
}
