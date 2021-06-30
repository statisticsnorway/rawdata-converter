package no.ssb.rawdata.converter.core.job;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.Async;
import io.micronaut.scheduling.annotation.ExecuteOn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.ssb.rawdata.converter.core.convert.RawdataConverterFactory;
import no.ssb.rawdata.converter.core.crypto.RawdataDecryptorFactory;
import no.ssb.rawdata.converter.core.pseudo.report.PseudoReportFactory;
import no.ssb.rawdata.converter.core.rawdatasource.RawdataConsumerFactory;
import no.ssb.rawdata.converter.core.rawdatasource.RawdataConsumers;
import no.ssb.rawdata.converter.core.storage.DatasetStorageFactory;
import no.ssb.rawdata.converter.core.storage.StorageType;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Singleton
@Slf4j
@RequiredArgsConstructor
public class ConverterJobScheduler {

    private final Map<String, ConverterJob> jobs = new ConcurrentHashMap<>();

    private final ConverterJobSchedulerConfig jobSchedulerConfig;
    private final ConverterJobConfigFactory effectiveConverterJobConfigFactory;
    private final RawdataConverterFactory rawdataConverterFactory;
    private final RawdataConsumerFactory rawdataConsumerFactory;
    private final RawdataDecryptorFactory rawdataDecryptorFactory;
    private final DatasetStorageFactory datasetStorageFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final PrometheusMeterRegistry prometheusMeterRegistry;
    private final PseudoReportFactory pseudoReportFactory;

    public void schedulePartial(ConverterJobConfig partialJobConfig) {
        ConverterJobConfig jobConfig = effectiveConverterJobConfigFactory.effectiveConverterJobConfigOf(partialJobConfig);
        this.schedule(jobConfig);
    }

    @Async
    @ExecuteOn(TaskExecutors.IO)
    public void schedule(ConverterJobConfig jobConfig) {
        if (canAcceptJobs()) {
            RawdataConsumers rawdataConsumers = rawdataConsumerFactory.rawdataConsumersOf(jobConfig);
            ConverterJob job = ConverterJob.builder()
              .jobConfig(jobConfig)
              .rawdataConverter(rawdataConverterFactory.newRawdataConverter(jobConfig))
              .rawdataConsumers(rawdataConsumers)
              .rawdataDecryptor(rawdataDecryptorFactory.rawdataDecryptorOf(rawdataConsumers.getMetadataClient().get(), jobConfig)) //TODO: Support rawdataDecryptor=null
              .datasetStorage(datasetStorageFactory.datasetStorageOf(StorageType.of(jobConfig.getTargetStorage().getRoot()), jobConfig.getTargetStorage().getSaKeyFile()))
              .localStorage(new ConverterJobLocalStorage(jobConfig, eventPublisher)) // TODO: Initialize this internally instead?
              .jobMetrics(new ConverterJobMetrics(prometheusMeterRegistry, jobConfig)) // TODO: Initialize this internally instead?
              .eventPublisher(eventPublisher)
              .pseudoReportFactory(pseudoReportFactory)
              .build();

            jobs.put(job.jobId(), job);
            job.init();
        }
        else {
            throw new ConverterJobException("Not ready to start new converter job - request was ignored. Jobs started=" + jobs.size() + ", max jobs=" + jobSchedulerConfig.getMaxConcurrentJobs());
        }
    }

    public void resumeFromLast(String jobId) {
        getJob(jobId).resumeFromLast();
    }

    public Map<String, ConverterJob> getJobs() {
        return jobs;
    }

    public Map<String, ConverterJob> getJobs(Predicate<ConverterJob> converterJobFilter) {
        return jobs.values().stream()
          .filter(converterJobFilter)
          .collect(Collectors.toMap(
            j -> j.getJobConfig().getJobId(),
            Function.identity())
          );
    }

    public ConverterJob getJob(String jobId) {
        return Optional.ofNullable(jobs.get(jobId))
          .orElseThrow(() -> new NoSuchElementException("Unable to find job with id=" + jobId));
    }

    public boolean canAcceptJobs() {
        long startedJobsCount = jobs.values().stream().filter(job -> ! job.runtime().isStopped()).count();
        return startedJobsCount < jobSchedulerConfig.getMaxConcurrentJobs();
    }

    public void resume(String jobId) {
        getJob(jobId).resume();
    }

    public void resumeAll() {
        jobs.values().stream().filter(job -> job.runtime().isPaused()).forEach(job -> job.resume());
    }

    public void pause(String jobId) {
        getJob(jobId).pause();
    }

    public void pauseAll() {
        jobs.values().stream().filter(job -> job.runtime().isPauseable()).forEach(job -> job.pause());
    }

    public void stop(String jobId) {
        getJob(jobId).stop();
    }

    public void stopAll() {
        jobs.values().stream().filter(job -> ! job.runtime().isStopped()).forEach(job -> job.stop());
    }

}
