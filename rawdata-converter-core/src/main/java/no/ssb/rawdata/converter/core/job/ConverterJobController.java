package no.ssb.rawdata.converter.core.job;

import avro.shaded.com.google.common.collect.Sets;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import no.ssb.rawdata.converter.core.pseudo.report.PseudoReport;
import no.ssb.rawdata.converter.util.Json;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// TODO Make conditional on property
@Controller("/jobs")
@RequiredArgsConstructor
public class ConverterJobController {
    private final ConverterJobScheduler jobScheduler;

    /**
     * Schedule a converter job using overrides from the specified JSON
     */
    @Post(consumes = MediaType.APPLICATION_JSON)
    public HttpResponse<ScheduleJobResponse> scheduleJob(StartConverterJobRequest request) {

        // Honor the activeByDefault flag if it is explicitly set, else assume we want to start the job immediately
        if (request.getJobConfig().getActiveByDefault() == null) {
            request.getJobConfig().setActiveByDefault(true);
        }
        jobScheduler.schedulePartial(request.getJobConfig());

        return HttpResponse.ok(
          ScheduleJobResponse.builder()
            .jobId(request.getJobConfig().getJobId())
            .build()
        );
    }

/*
    TODO: Implement resume
    @Post("/{jobId}/resume")
    public void resumeJob(String jobId) {
        jobScheduler.resumeFromLast(ULID.parseULID(jobId));
    }
*/

    private Set<Predicate<ConverterJob>> jobStatusPredicatesOf(Collection<String> jobStatusFilterList) {
        Set<String> jobStatusFilter = (jobStatusFilterList == null)
          ? Set.of()
          : new LinkedHashSet<>(jobStatusFilterList);

        Set<Predicate<ConverterJob>> predicates = new LinkedHashSet<>();
        for (String status : jobStatusFilter) {
            predicates.add(job ->
                status.equalsIgnoreCase(String.valueOf(job.getExecutionSummary().get("job.status")))
            );
        }

        return predicates;
    }

    /**
     * List all converter job execution summaries
     */
    @Get("/execution-summary{?status,include-dryrun}")
    public HttpResponse<String> getJobExecutionSummary(@Nullable @QueryValue("status") Set<String> includedStatuses,
                                                       @Nullable @QueryValue("include-dryrun") Boolean includeDryrun,
                                                       @Nullable @QueryValue("started-before") String startedBeforeOffset,
                                                       @Nullable @QueryValue("started-after") String startedAfterOffset,
                                                       @Nullable @QueryValue("stopped-before") String stoppedBeforeOffset,
                                                       @Nullable @QueryValue("stopped-after") String stoppedAfterOffset) {
        Predicate<ConverterJob> filter = new ConverterJobFilterBuilder()
          .jobStatus(includedStatuses)
          .dryrun(includeDryrun)
          .startedBefore(startedBeforeOffset)
          .startedAfter(startedAfterOffset)
          .stoppedBefore(stoppedBeforeOffset)
          .stoppedAfter(stoppedAfterOffset)
          .build();

        Map<String, ConverterJob> filteredJobs = jobScheduler.getJobs(filter);

        return HttpResponse.ok(Json.prettyFrom(
          filteredJobs.values().stream()
            .sorted(Comparator.comparing(ConverterJob::getStartedTime).reversed())
            .map(job -> job.getExecutionSummary())
            .collect(Collectors.toList()))
        );
    }

    /**
     * List effective converter job configurations for registered jobs
     */
    @Get("/config")
    public HttpResponse<String> getJobConfigs() {
        return HttpResponse.ok(Json.prettyFrom(
          jobScheduler.getJobs().values().stream()
            .collect(Collectors.toMap(
              ConverterJob::jobId,
              ConverterJob::getJobConfig
            )))
        );
    }

    /**
     * Pause all converter jobs
     */
    @Post("/pause")
    public void pauseAllJobs() {
        jobScheduler.pauseAll();
    }

    /**
     * Resume all converter jobs
     */
    @Post("/resume")
    public void resumeAllJobs() {
        jobScheduler.resumeAll();
    }

    /**
     * Stop all converter jobs
     */
    @Post("/stop")
    public void stopAllJobs() {
        jobScheduler.stopAll();
    }

    /**
     * Pause a specific converter job
     */
    @Post("/{jobId}/pause")
    public void pauseJob(String jobId) {
        jobScheduler.pause(jobId);
    }

    /**
     * Resume a specific converter job
     */
    @Post("/{jobId}/resume")
    public void resumeJob(String jobId) {
        jobScheduler.resume(jobId);
    }

    /**
     * Stop a specific converter job
     */
    @Post("/{jobId}/stop")
    public void stopJob(@PathVariable String jobId) {
        jobScheduler.stop(jobId);
    }

    /**
     * Retrieve the effective job config for a specific job
     */
    @Get("/{jobId}/config")
    public HttpResponse<String> getJobConfig(String jobId) {
        return HttpResponse.ok(Json.prettyFrom(jobScheduler.getJob(jobId).getJobConfig()));
    }

    /**
     * <p>Retrieve the dataset metadata deduced for a specific job.
     * </p>
     * <p>This can be used as a baseline for manual publishing of dataset metadata.
     * </p>
     */
    @Get("/{jobId}/dataset-meta")
    public HttpResponse<String> getJobDatasetMeta(String jobId) {
        return HttpResponse.ok(Json.prettyFrom(jobScheduler.getJob(jobId).createDatasetMetadataEvent()));
    }

    /**
     * Retrieve the execution summary for a specific job
     */
    @Get("/{jobId}/execution-summary")
    public HttpResponse<String> getJobExecutionSummary(String jobId) {
        ConverterJob job;
        try {
            job = jobScheduler.getJob(jobId);
        } catch (NoSuchElementException e) {
            return HttpResponse.notFound();
        }
        return HttpResponse.ok(Json.prettyFrom(job.getExecutionSummary()));
    }

    /**
     * For a specific job, print a hierarchical representation of the target schema with type and pseudo details
     * about each field of the schema. This can useful e.g. as a baseline for validating how pseudonymization rules are
     * being applied.
     */
    @Get("/{jobId}/reports/pseudo-schema-hierarchy")
    @Produces(MediaType.TEXT_PLAIN)
    public HttpResponse<String> getReportPseudoSchemaReport(String jobId) {
        return HttpResponse.ok(
          jobScheduler.getJob(jobId).getPseudoReport().getTargetSchemaHierachy()
        );
    }

    /**
     * For a specific job, print a hierarchical representation of the target schema with type and pseudo details
     * about each field of the schema. This can useful e.g. as a baseline for validating how pseudonymization rules are
     * being applied.
     */
    @Get("/{jobId}/reports/pseudo")
    public HttpResponse<PseudoReport> getTargetSchemaReport(String jobId) {
        return HttpResponse.ok(
          jobScheduler.getJob(jobId).getPseudoReport()
        );
    }

    @Post("/simulation")
    public HttpResponse<ScheduleJobResponse> scheduleSimulatedJob(PseudoReportRequest request) {
        ConverterJobConfig jobConfig = request.getJobConfig();
        jobConfig.setActiveByDefault(true);
        jobConfig.getDebug().setDryrun(true);
        if (jobConfig.getConverterSettings().getMaxRecordsTotal() == null) {
            jobConfig.getConverterSettings().setMaxRecordsTotal(1L);
        }
        jobScheduler.schedulePartial(jobConfig);

        return HttpResponse.ok(
          ScheduleJobResponse.builder()
            .jobId(jobConfig.getJobId())
            .build()
        );
    }

    @Error
    public HttpResponse<JsonError> converterJobFilterError(HttpRequest request, ConverterJobFilterBuilder.InvalidConverterJobFilterException e) {
        JsonError error = new JsonError(e.getMessage())
          .link(Link.SELF, Link.of(request.getUri()));

        return HttpResponse.<JsonError>status(HttpStatus.BAD_REQUEST, e.getMessage())
          .body(error);
    }

    @Data
    public static class StartConverterJobRequest {
        private ConverterJobConfig jobConfig;
    }

    @Data
    public static class PseudoReportRequest {
        private ConverterJobConfig jobConfig;
    }

    @Data
    @Builder
    public static class ScheduleJobResponse {
        private String jobId;
    }

}
