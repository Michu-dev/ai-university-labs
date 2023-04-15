package com.example.bigdata;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import net.datafaker.Faker;
import net.datafaker.Photography;
import net.datafaker.fileformats.Format;

import java.sql.Array;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class EsperClient {
    public static void main(String[] args) throws InterruptedException {
        int noOfRecordsPerSec;
        int howLongInSec;
        if (args.length < 2) {
            noOfRecordsPerSec = 10;
            howLongInSec = 5;
        } else {
            noOfRecordsPerSec = Integer.parseInt(args[0]);
            howLongInSec = Integer.parseInt(args[1]);
        }

        Configuration config = new Configuration();
        CompilerArguments compilerArgs = new CompilerArguments(config);
//        create table MeanComparison (city string primary key, avgVelocity avg(integer));
//
//        into table CityAverages
//        select city, avg(velocity) as avgVelocity
//        from PhotoEvent#length(5) group by city;
        // Compile the EPL statement
        EPCompiler compiler = EPCompilerProvider.getCompiler();
        EPCompiled epCompiled;
        try {
            epCompiled = compiler.compile("""
                                @public @buseventtype create json schema
                                PhotoEvent(camera string, genre string, iso int, width int, height int, ets string, its string);
                                create window BeautyPhotos#length(10) as PhotoEvent;
                                insert into BeautyPhotos select * from PhotoEvent where genre = 'Beauty';
                                @name('result') SELECT * FROM BeautyPhotos MATCH_RECOGNIZE
                                                        (PARTITION BY genre
                                                         MEASURES STRT.its AS its_start,
                                                         LAST(DOWN.ets) AS its_end
                                                         ALL MATCHES
                                                         PATTERN (STRT DOWN+)
                                                         DEFINE
                                                         DOWN AS DOWN.iso < PREV(DOWN.iso)
                                                         );
                            """,
                    compilerArgs
            );
        }
        catch (EPCompileException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }

        // Connect to the EPRuntime server and deploy the statement
        EPRuntime runtime = EPRuntimeProvider.getRuntime("http://localhost:port", config);
        EPDeployment deployment;
        try {
            deployment = runtime.getDeploymentService().deploy(epCompiled);
        }
        catch (EPDeployException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }

        EPStatement resultStatement = runtime.getDeploymentService().getStatement(deployment.getDeploymentId(), "result");

        // Add a listener to the statement to handle incoming events
        resultStatement.addListener( (newData, oldData, stmt, runTime) -> {
            for (EventBean eventBean : newData) {
                System.out.printf("R: %s%n", eventBean.getUnderlying());
            }
        });

        Faker faker = new Faker();
        Photography photographyFaker = faker.photography();
        List<String> genres = Arrays.asList(
                "Beauty", "Underwater", "Architecture", "Fauna", "Wedding",
                "Food", "Sports", "Space", "Nude", "Macro"
        );
        Random random = new Random();
        final int MIN_PIXELS = 50;
        final int MAX_PIXELS = 10000;
        final int MAX_DELAY_IN_SECONDS = 45;

        String record;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + (1000L * howLongInSec)) {
            for (int i = 0; i < noOfRecordsPerSec; i++) {
                String camera = photographyFaker.camera();
                String genre = genres.get(random.nextInt(genres.size()));
                String ISO = photographyFaker.iso();
                String height = String.valueOf(faker.number().numberBetween(MIN_PIXELS, MAX_PIXELS));
                String width = String.valueOf(faker.number().numberBetween(MIN_PIXELS, MAX_PIXELS));

                Timestamp pubTimestamp = faker.date().past(MAX_DELAY_IN_SECONDS, TimeUnit.SECONDS);
                Timestamp regTimestamp = new Timestamp(System.currentTimeMillis());

                record = Format.toJson()
                        .set("camera", () -> camera)
                        .set("genre", () -> genre)
                        .set("iso", () -> ISO)
                        .set("width", () -> width)
                        .set("height", () -> height)
                        .set("ets", () -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pubTimestamp))
                        .set("its", () -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(regTimestamp))
                        .build().generate();
                runtime.getEventService().sendEventJson(record, "PhotoEvent");
            }
            waitToEpoch();
        }
    }

    static void waitToEpoch() throws InterruptedException {
        long millis = System.currentTimeMillis();
        Instant instant = Instant.ofEpochMilli(millis) ;
        Instant instantTrunc = instant.truncatedTo( ChronoUnit.SECONDS ) ;
        long millis2 = instantTrunc.toEpochMilli() ;
        TimeUnit.MILLISECONDS.sleep(millis2+1000-millis);
    }
}