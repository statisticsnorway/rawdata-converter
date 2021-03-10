package no.ssb.rawdata.converter.core.rawdatasource;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Properties;

@Context
@Data @Accessors(chain = true)
@EachProperty(value = "rawdata.sources")
public class RawdataSourceConfig {

    public RawdataSourceConfig(@Parameter String name) {
        this.name = name;
    }

    private String name;
    private Properties rawdataClient = new Properties();

}
