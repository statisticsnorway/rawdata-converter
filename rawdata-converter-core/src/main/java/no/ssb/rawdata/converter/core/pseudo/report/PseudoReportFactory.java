package no.ssb.rawdata.converter.core.pseudo.report;

import lombok.RequiredArgsConstructor;
import no.ssb.avro.convert.core.FieldDescriptor;
import no.ssb.avro.convert.core.SchemaBuddy;
import no.ssb.dlp.pseudo.core.FieldPseudonymizer;
import no.ssb.dlp.pseudo.core.PseudoFuncRule;
import no.ssb.dlp.pseudo.core.PseudoFuncRuleMatch;
import no.ssb.rawdata.converter.core.pseudo.FieldPseudonymizerFactory;
import org.apache.avro.Schema;

import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
@RequiredArgsConstructor
public class PseudoReportFactory {

    private final FieldPseudonymizerFactory fieldPseudonymizerFactory;

    public PseudoReport pseudoReportOf(Schema schema, List<PseudoFuncRule> pseudoRules) {
        FieldPseudonymizer fieldPseudonymizer = fieldPseudonymizerFactory.newFieldPseudonymizer(pseudoRules);
        return pseudoReportOf(schema, pseudoRules, fieldPseudonymizer);
    }

    public PseudoReport pseudoReportOf(Schema schema, List<PseudoFuncRule> pseudoRules, FieldPseudonymizer fieldPseudonymizer) {
        AtomicInteger fieldsInTargetSchemaCount = new AtomicInteger();
        PseudoReport.PseudoReportBuilder report = PseudoReport.builder()
          .pseudoRules(pseudoRules)
          .targetSchema(schema);

        SchemaBuddy.parse(schema, c -> {
            if (c.isSimpleType()) {
                fieldsInTargetSchemaCount.incrementAndGet();
                FieldDescriptor field = new FieldDescriptor(c.getPath());
                PseudoFuncRuleMatch match = fieldPseudonymizer.match(field).orElse(null);
                if (match != null) {
                    report.mapping(c.getPath(), match);
                }
            }
        });
        report.fieldsInTargetSchemaCount(fieldsInTargetSchemaCount.get());

        return report.build();
    }

}
