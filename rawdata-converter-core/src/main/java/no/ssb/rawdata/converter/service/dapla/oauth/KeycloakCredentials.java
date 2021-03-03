package no.ssb.rawdata.converter.service.dapla.oauth;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

@ConfigurationProperties(KeycloakCredentials.PREFIX)
@Data
@Slf4j
public class KeycloakCredentials {

    public static final String PREFIX = "sm.keycloak.rawdataconverter";

    private String clientid;

    private String clientsecret;

    public String asBase64Encoded() {
        if (clientid == null || clientsecret == null) {
            log.warn(PREFIX + ".clientid and " + PREFIX + ".clientsecret are not specified. Will not be able to obtain auth token to invoke other services.");
        }
        return Base64.getEncoder().encodeToString((clientid + ":" + clientsecret).getBytes());
    }

}
