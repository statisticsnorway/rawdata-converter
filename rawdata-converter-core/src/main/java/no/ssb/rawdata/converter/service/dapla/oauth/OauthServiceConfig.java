package no.ssb.rawdata.converter.service.dapla.oauth;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Base64;

@ConfigurationProperties(OauthServiceConfig.PREFIX)
@Data
@Slf4j
public class OauthServiceConfig {

    public static final String PREFIX = "services.dapla-oauth";

    private String host;

    private String tokenEndpointPath;

    private String clientIdKey = "keycloak_rawdata_converter_clientid";

    private String clientSecretKey = "keycloak_rawdata_converter_clientsecret";

    // TODO: Make this joining more robust
    public URI getTokenUrl() {
        return URI.create(host + tokenEndpointPath);
    }

}
