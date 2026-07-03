package com.icampus.infra.crawler;

import com.icampus.domain.model.CrawlerJobStatus;
import com.icampus.domain.spi.CrawlerManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LocalCrawlerManager implements CrawlerManager {

    private static final Logger log = LoggerFactory.getLogger(LocalCrawlerManager.class);

    private final Path scriptPath;
    private final Path statePath;
    private final Path logPath;
    private final boolean defaultEnabled;
    private final int defaultIntervalHours;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            daemonThreadFactory("crawler-scheduler"));
    private final ExecutorService watcher = Executors.newSingleThreadExecutor(
            daemonThreadFactory("crawler-watcher"));

    private boolean enabled;
    private int intervalHours;
    private LocalDateTime lastStartedAt;
    private LocalDateTime lastFinishedAt;
    private Integer lastExitCode;
    private Process currentProcess;
    private ScheduledFuture<?> scheduledFuture;

    public LocalCrawlerManager(
            @Value("${crawler.script-path:/opt/icampus/spider/run_server.sh}") String scriptPath,
            @Value("${crawler.state-path:/opt/icampus/spider/data/crawler-admin.properties}") String statePath,
            @Value("${crawler.log-path:/opt/icampus/spider/logs/admin-crawler.log}") String logPath,
            @Value("${crawler.default-enabled:true}") boolean defaultEnabled,
            @Value("${crawler.default-interval-hours:24}") int defaultIntervalHours) {
        this.scriptPath = Path.of(scriptPath);
        this.statePath = Path.of(statePath);
        this.logPath = Path.of(logPath);
        this.defaultEnabled = defaultEnabled;
        this.defaultIntervalHours = Math.max(1, Math.min(defaultIntervalHours, 168));
    }

    @PostConstruct
    public synchronized void initialize() {
        loadState();
        reschedule();
    }

    @Override
    public synchronized CrawlerJobStatus getStatus() {
        return buildStatus(null);
    }

    @Override
    public synchronized CrawlerJobStatus trigger() {
        if (!Files.isRegularFile(scriptPath)) {
            return buildStatus("爬虫脚本不存在，无法执行");
        }
        if (currentProcess != null && currentProcess.isAlive()) {
            return buildStatus("爬虫任务正在运行");
        }

        try {
            Path parent = logPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", scriptPath.toString());
            if (scriptPath.getParent() != null) {
                builder.directory(scriptPath.getParent().toFile());
            }
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(logPath.toFile()));
            currentProcess = builder.start();
            lastStartedAt = LocalDateTime.now();
            lastFinishedAt = null;
            lastExitCode = null;
            persistState();

            Process startedProcess = currentProcess;
            watcher.submit(() -> waitForCompletion(startedProcess));
            return buildStatus("爬虫任务已启动");
        } catch (IOException exception) {
            log.error("启动爬虫任务失败", exception);
            return buildStatus("启动失败：" + exception.getMessage());
        }
    }

    @Override
    public synchronized CrawlerJobStatus configure(boolean enabled, int intervalHours) {
        this.enabled = enabled;
        this.intervalHours = Math.max(1, Math.min(intervalHours, 168));
        persistState();
        reschedule();
        return buildStatus(enabled ? "定时爬虫已启用" : "定时爬虫已停用");
    }

    private void waitForCompletion(Process process) {
        try {
            int exitCode = process.waitFor();
            synchronized (this) {
                if (currentProcess == process) {
                    lastFinishedAt = LocalDateTime.now();
                    lastExitCode = exitCode;
                    currentProcess = null;
                    persistState();
                }
            }
            log.info("爬虫任务结束 [exitCode={}]", exitCode);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("等待爬虫任务结束时被中断");
        }
    }

    private void runScheduledTask() {
        try {
            CrawlerJobStatus status = trigger();
            log.info("定时爬虫触发结果: {}", status.getMessage());
        } catch (RuntimeException exception) {
            log.error("定时爬虫触发失败", exception);
        }
    }

    private synchronized void reschedule() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
        }
        if (enabled) {
            scheduledFuture = scheduler.scheduleAtFixedRate(
                    this::runScheduledTask,
                    intervalHours,
                    intervalHours,
                    TimeUnit.HOURS);
        }
    }

    private CrawlerJobStatus buildStatus(String message) {
        CrawlerJobStatus status = new CrawlerJobStatus();
        status.setAvailable(Files.isRegularFile(scriptPath));
        status.setRunning(currentProcess != null && currentProcess.isAlive());
        status.setEnabled(enabled);
        status.setIntervalHours(intervalHours);
        status.setLastStartedAt(lastStartedAt);
        status.setLastFinishedAt(lastFinishedAt);
        status.setLastExitCode(lastExitCode);
        status.setMessage(message);
        if (enabled && scheduledFuture != null) {
            long delaySeconds = Math.max(0, scheduledFuture.getDelay(TimeUnit.SECONDS));
            status.setNextRunAt(LocalDateTime.now().plusSeconds(delaySeconds));
        }
        return status;
    }

    private void loadState() {
        enabled = defaultEnabled;
        intervalHours = defaultIntervalHours;
        if (!Files.isRegularFile(statePath)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(statePath)) {
            properties.load(input);
            enabled = Boolean.parseBoolean(properties.getProperty("enabled", String.valueOf(defaultEnabled)));
            intervalHours = Math.max(1, Math.min(
                    Integer.parseInt(properties.getProperty("intervalHours", String.valueOf(defaultIntervalHours))),
                    168));
            lastStartedAt = parseDateTime(properties.getProperty("lastStartedAt"));
            lastFinishedAt = parseDateTime(properties.getProperty("lastFinishedAt"));
            String exitCode = properties.getProperty("lastExitCode");
            lastExitCode = exitCode == null || exitCode.isBlank() ? null : Integer.valueOf(exitCode);
        } catch (Exception exception) {
            log.warn("读取爬虫任务配置失败，使用默认配置", exception);
            enabled = defaultEnabled;
            intervalHours = defaultIntervalHours;
        }
    }

    private void persistState() {
        Properties properties = new Properties();
        properties.setProperty("enabled", String.valueOf(enabled));
        properties.setProperty("intervalHours", String.valueOf(intervalHours));
        setProperty(properties, "lastStartedAt", lastStartedAt);
        setProperty(properties, "lastFinishedAt", lastFinishedAt);
        if (lastExitCode != null) {
            properties.setProperty("lastExitCode", String.valueOf(lastExitCode));
        }

        try {
            Path parent = statePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream output = Files.newOutputStream(statePath)) {
                properties.store(output, "ICAMPUS crawler admin state");
            }
        } catch (IOException exception) {
            log.warn("保存爬虫任务配置失败", exception);
        }
    }

    private static LocalDateTime parseDateTime(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value);
    }

    private static void setProperty(Properties properties, String key, LocalDateTime value) {
        if (value != null) {
            properties.setProperty(key, value.toString());
        }
    }

    private static java.util.concurrent.ThreadFactory daemonThreadFactory(String prefix) {
        AtomicInteger counter = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + "-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    @PreDestroy
    public void shutdown() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        scheduler.shutdownNow();
        watcher.shutdownNow();
    }
}
