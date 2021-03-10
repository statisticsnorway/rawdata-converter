package no.ssb.rawdata.converter.core.pseudo;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import no.ssb.rawdata.converter.core.convert.RawdataConverterFactory;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest(environments = "test-pseudo")
class FieldPseudonymizerFactoryTest {

    @Inject FieldPseudonymizerFactory fieldPseudonymizerFactory;

    @MockBean(RawdataConverterFactory.class)
    RawdataConverterFactory rawdataConverterFactory() {
        return jobConfig -> null;
    }

    @Test
    void testStuff() {
        System.out.println();
        fieldPseudonymizerFactory.getPseudoSecrets();
        fieldPseudonymizerFactory.newFieldPseudonymizer(null);
    }
}