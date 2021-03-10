package no.ssb.rawdata.converter.service.dapla.oauth;

import no.ssb.rawdata.converter.service.secret.MockSecretService;
import no.ssb.rawdata.converter.service.secret.SecretService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KeycloakAuthTokenProviderTest {

    private final SecretService secretService = new MockSecretService();
    static final JWTMother jwtMother = new JWTMother();
    private KeycloakAuthTokenProvider tokenProvider;

    @BeforeEach
    void init() {
        tokenProvider = Mockito.spy(new KeycloakAuthTokenProvider(new OauthServiceConfig(), secretService));
    }

    @Test
    void givenNoTokenCached_whenGetAuthToken_ThenShouldFetchNewToken() {
        tokenProvider.setAuthToken(null);
        String newToken = newToken();
        doReturn(newToken).when(tokenProvider).fetchAuthToken();
        assertThat(tokenProvider.getAuthToken()).isEqualTo(newToken);
    }

    @Test
    void givenValidTokenCached_whenGetAuthToken_ThenShouldReturnTheCachedToken() {
        String cachedToken = newToken();
        tokenProvider.setAuthToken(cachedToken);
        verify(tokenProvider, Mockito.times(0)).fetchAuthToken();
        assertThat(tokenProvider.getAuthToken()).as("Expected to use cached JWT, but got a new one. Caching mechanism seems to be broken.").isEqualTo(cachedToken);
    }

    @Test
    void givenExpiredTokenCached_whenGetAuthToken_ThenShouldReturnNewValidToken() {
        String cachedAndExpiredToken = newExpiredToken();
        tokenProvider.setAuthToken(cachedAndExpiredToken);
        doReturn(newToken()).when(tokenProvider).fetchAuthToken();
        assertThat(tokenProvider.getAuthToken()).as("Expected to get new JWT, but got the cached (and expired) one. Caching mechanism seems to be broken.").isNotEqualTo(cachedAndExpiredToken);
    }

    @Test
    void givenNearlyExpiredTokenCached_whenGetAuthToken_ThenShouldReturnNewValidToken() {
        String cachedAndNearlyExpiredToken = newNearlyExpiredToken();
        tokenProvider.setAuthToken(cachedAndNearlyExpiredToken);
        doReturn(newToken()).when(tokenProvider).fetchAuthToken();
        assertThat(tokenProvider.getAuthToken()).as("Expected to get new JWT, but got the cached (and expired) one. Caching mechanism seems to be broken.").isNotEqualTo(newNearlyExpiredToken());
    }

    static String newToken() {
        return jwtMother.generateToken(Instant.now().plus(5, ChronoUnit.MINUTES));
    }

    static String newNearlyExpiredToken() {
        return jwtMother.generateToken(Instant.now().plusSeconds(179));
    }

    static String newExpiredToken() {
        return jwtMother.generateToken(Instant.now().minus(1, ChronoUnit.MINUTES));
    }

}