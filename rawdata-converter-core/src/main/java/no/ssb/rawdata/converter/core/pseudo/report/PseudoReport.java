package no.ssb.rawdata.converter.core.pseudo.report;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import no.ssb.avro.convert.core.SchemaBuddy;
import no.ssb.dlp.pseudo.core.PseudoFuncRule;
import no.ssb.dlp.pseudo.core.PseudoFuncRuleMatch;
import org.apache.avro.Schema;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class PseudoReport {

    @Singular
    private final Map<String, PseudoFuncRuleMatch> mappings;
    private List<PseudoFuncRule> pseudoRules;
    private int fieldsInTargetSchemaCount;

    @NonNull
    private Schema targetSchema;

    public Map<String, Object> getMetrics() {
        return Map.of(
          "totalFieldsCount", fieldsInTargetSchemaCount,
          "pseudonymizedFieldsCount", mappings.size(),
          "totalPseudoRulesCount", pseudoRules.size(),
          "matchedPseudoRulesCount", getMatchedPseudoRules().size(),
          "unmatchedPseudoRulesCount", getUnmatchedPseudoRules().size()
        );
    }

    public Set<PseudoFuncRule> getMatchedPseudoRules() {
        return mappings.values().stream()
          .map(PseudoFuncRuleMatch::getRule)
          .collect(Collectors.toSet());
    }

    public Map<String, Collection<String>> getRuleToFieldMatches() {
        Multimap<String, String> ruleToFieldMatches = TreeMultimap.create();
        mappings.entrySet().forEach(e -> {
            ruleToFieldMatches.put(e.getValue().getRule().getName(), e.getKey());
        });

        return ruleToFieldMatches.asMap();
    }

    public Map<String, String> getFieldToRuleMatches() {
        return mappings.entrySet()
          .stream()
          .collect(Collectors.toMap(
            Map.Entry:: getKey,
            e -> e.getValue().getRule().getName()));
    }

    public Set<PseudoFuncRule> getUnmatchedPseudoRules() {
        Set<PseudoFuncRule> matchedRules = getMatchedPseudoRules();
        return pseudoRules.stream()
          .filter(r -> ! matchedRules.contains(r))
          .collect(Collectors.toSet());
    }

    public String getTargetSchemaHierachy() {
        return SchemaBuddy.parse(targetSchema).toString(true, item -> {
            PseudoFuncRuleMatch match = mappings.get(item.getPath());
            return item.getName() + " "
              + item.getType().getName()
              + (item.isOptional() ? "" : " required")
              + (match == null ? "" : " pseudo:" + match.getRule().getFunc());
        });
    }

}
