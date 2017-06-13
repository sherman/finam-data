package org.ontslab.data.finam.loader;

import com.google.common.base.Charsets;
import com.google.common.util.concurrent.AbstractService;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.ontslab.data.finam.domain.SymbolSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.io.CharStreams.readLines;
import static com.google.common.io.Files.newReader;

/**
 * @author Denis Gabaydulin
 * @since 11.06.17
 */
@Singleton
public class DataServiceImpl extends AbstractService implements DataService {
    private static final Logger log = LoggerFactory.getLogger(DataServiceImpl.class);

    private static final String FINAM_URL = "http://export.finam.ru/%symbol%.txt?market=%market%&em=%em%&code=%symbol%&apply=0&df=%day_from%&mf=%month_from%&yf=%year_from%&from=%from%&dt=%day_to%&mt=%month_to%&yt=%year_to%&to=%to%&p=%period%&f=%symbol%&e=.txt&cn=%symbol%&dtf=1&tmf=1&MSOR=1&mstime=on&mstimever=1&sep=1&sep2=1&datf=5";
    private static final LocalDate startDate = new LocalDate("2008-01-01");
    private static final int MAX_YEARS = 2;

    private AsyncHttpClient client;

    @Override
    public void load(@NotNull SymbolSpec symbolSpec, @NotNull File dataFile) {
        LocalDate start = getLastDateTime(dataFile, elts -> LocalDate.parse(elts[0],
                new DateTimeFormatterBuilder()
                        .appendPattern("yyyyMMdd")
                        .toFormatter()
        )).map(date -> date.minusDays(1))
                .orElse(startDate);

        LocalDateTime startDateTime = getLastDateTime(dataFile, elts -> LocalDateTime.parse(elts[0] + elts[1],
                new DateTimeFormatterBuilder()
                        .appendPattern("yyyyMMddHHmmss")
                        .toFormatter()
        )).orElse(start.minusYears(10).toLocalDateTime(new LocalTime("00:00:00")));

        LocalDate finish = start.plusDays(symbolSpec.getDays());

        // can't pass next year :-/
        if (finish.getYear() == LocalDate.now(DateTimeZone.UTC).getYear() + 1) {
            finish = new LocalDate(LocalDate.now(DateTimeZone.UTC).getYear() + "-12-31");
        }

        String dataUrl = FINAM_URL
                .replace("%symbol%", symbolSpec.getName())
                .replace("%market%", String.valueOf(symbolSpec.getMarketId()))
                .replace("%em%", String.valueOf(symbolSpec.getEm()))
                .replace("%symbol%", symbolSpec.getName())
                .replace("%year_from%", String.valueOf(start.getYear()))
                .replace("%month_from%", String.valueOf(start.getMonthOfYear() - 1))
                .replace("%day_from%", String.valueOf(start.getDayOfMonth()))
                .replace("%year_to%", String.valueOf(finish.getYear()))
                .replace("%month_to%", String.valueOf(finish.getMonthOfYear() - 1))
                .replace("%day_to%", String.valueOf(finish.getDayOfMonth()))
                .replace("%from%", start.toString("dd.MM.yyyy"))
                .replace("%to%", finish.toString("dd.MM.yyyy"))
                .replace("%period%", String.valueOf(symbolSpec.getPeriod().getId()));


        client.prepareGet(dataUrl).execute()
                .toCompletableFuture()
                .thenAccept(response -> {
                    log.info("Status", response.getStatusCode());

                    try {
                        Stream<String> lines = readLines(new InputStreamReader(response.getResponseBodyAsStream())).stream()
                                .filter(
                                        line -> {
                                            String[] elts = line.split(",");
                                            LocalDateTime dateTime = LocalDateTime.parse(elts[0] + elts[1],
                                                    new DateTimeFormatterBuilder()
                                                            .appendPattern("yyyyMMddHHmmss")
                                                            .toFormatter());

                                            return dateTime.isAfter(startDateTime);
                                        }
                                );

                        Files.write(dataFile.toPath(), (Iterable<String>) lines::iterator, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        log.error("Can't read data", e);
                    }
                }).join();
    }

    private <T> Optional<T> getLastDateTime(File file, Function<String[], T> parserFunction) {
        try {
            return newReader(file, Charsets.UTF_8).lines()
                    .reduce((first, second) -> second)
                    .map(last -> {
                        String[] elts = last.split(",");

                        T lastDate = parserFunction.apply(elts);

                        log.info("Last date: {}", lastDate);

                        return lastDate;
                    });
        } catch (FileNotFoundException e) {
            log.error("Can't read file {}", file, e);
            return Optional.<T>empty();
        }
    }

    @Override
    protected void doStart() {
        client = new DefaultAsyncHttpClient(
                new DefaultAsyncHttpClientConfig.Builder()
                        .setFollowRedirect(true)
                        .setKeepAlive(true)
                        .setConnectionTtl(5000)
                        .setRequestTimeout(180000)
                        .setMaxRequestRetry(3)
                        .build()
        );
        notifyStarted();
    }

    @Override
    protected void doStop() {
        try {
            client.close();
        } catch (IOException e) {
            log.error("Can't stop a http client", e);
        }
        notifyStopped();
    }
}
