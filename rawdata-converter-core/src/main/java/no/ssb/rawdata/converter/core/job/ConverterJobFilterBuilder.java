package no.ssb.rawdata.converter.core.job;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class ConverterJobFilterBuilder {

    private final Set<Predicate<ConverterJob>> filters = new LinkedHashSet<>();

    public static ConverterJobFilterBuilder of() {
        return new ConverterJobFilterBuilder();
    }

    public ConverterJobFilterBuilder jobStatus(Collection<String> statusItems) {
        if (statusItems == null) {
            return this;
        }

        Set<Predicate<ConverterJob>> predicates = new LinkedHashSet<>();
        for (String status : statusItems) {
            predicates.add(job ->
              status.equalsIgnoreCase(String.valueOf(job.getExecutionSummary().get("job.status")))
            );
        }

        filters.add(anyOf(predicates));
        return this;
    }

    /**
     * Include dryrun jobs only if the include flag is set to true. Always include non-dryrun jobs.
     */
    public ConverterJobFilterBuilder dryrun(Boolean includeDryrun) {
        filters.add(job -> {
            boolean isDryrun = job.getJobConfig().getDebug().getDryrun();
            return !isDryrun || isDryrun && Optional.ofNullable(includeDryrun).orElse(false);
        });

        return this;
    }

    public ConverterJobFilterBuilder startedBefore(String s) {
        if (s != null) {
            filters.add(job -> job.getStartedTime() != null && job.getStartedTime().isBefore(offsetOf(s)));
        }
        return this;
    }

    public ConverterJobFilterBuilder startedAfter(String s) {
        if (s != null) {
            filters.add(job -> job.getStartedTime() != null && job.getStartedTime().isAfter(offsetOf(s)));
        }
        return this;
    }

    public ConverterJobFilterBuilder stoppedBefore(String s) {
        if (s != null) {
            filters.add(job -> job.getStoppedTime() != null && job.getStoppedTime().isBefore(offsetOf(s)));
        }
        return this;
    }

    public ConverterJobFilterBuilder stoppedAfter(String s) {
        if (s != null) {
            filters.add(job -> job.getStoppedTime() != null && job.getStoppedTime().isAfter(offsetOf(s)));
        }
        return this;
    }

    public Predicate<ConverterJob> build() {
        return allOf(filters);
    }

    public static Predicate<ConverterJob> allOf(Collection<Predicate<ConverterJob>> predicates) {
        return predicates.stream().reduce(x -> true, Predicate::and);
    }

    public static Predicate<ConverterJob> anyOf(Collection<Predicate<ConverterJob>> predicates) {
        return predicates.stream().reduce(x -> false, Predicate::or);
    }

    private static Instant offsetOf(String s) {
        final Instant offset;
        if ("today".equalsIgnoreCase(s)) {
            return Instant.now().truncatedTo(ChronoUnit.DAYS);
        }
        else if ("yesterday".equalsIgnoreCase(s)) {
            return Instant.now().truncatedTo(ChronoUnit.DAYS).minus(1L, ChronoUnit.DAYS);
        }
        else {
            try {
                return Instant.parse(s);
            }
            catch (Exception e) {
                throw new InvalidConverterJobFilterException(s, e);
            }
        }
    }

    public static class InvalidConverterJobFilterException extends RuntimeException {
        public InvalidConverterJobFilterException(String jobFilter, Throwable cause) {
            super("Unable to parse job filter: " + jobFilter, cause);
        }
    }
}
