package no.ssb.rawdata.converter.service.secret;


import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@ConfigurationProperties(SecretServiceConfig.PREFIX)
public class SecretServiceConfig {

    public static final String PREFIX = "services.secrets";

    public enum Impl {
        GCP, LOCAL, MOCK;
    }

    private Impl impl = Impl.MOCK;

    Map<String, byte[]> overrides = new LinkedHashMap<>();

}
