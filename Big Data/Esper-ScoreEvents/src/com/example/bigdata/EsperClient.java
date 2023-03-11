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
import net.datafaker.fileformats.Format;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EsperClient {
    public static void main(String[] args) throws InterruptedException {
        int noOfRecordsPerSec;
        int howLongInSec;
        if (args.length < 2) {
            noOfRecordsPerSec = 2;
            howLongInSec = 3;
        } else {
            noOfRecordsPerSec = Integer.parseInt(args[0]);
            howLongInSec = Integer.parseInt(args[1]);
        }

        Configuration config = new Configuration();
        CompilerArguments compilerArgs = new CompilerArguments(config);

        // Compile the EPL statement
        EPCompiler compiler = EPCompilerProvider.getCompiler();
        EPCompiled epCompiled;
//        @public @buseventtype create json schema ScoreEvent(house string, character string, score int, ts string);
//        @name('result') SELECT s.score as score, s.character as character, s.house as house, (SELECT AVG(w.score) FROM ScoreEvent.win:time_batch(10 sec) w WHERE s.house = w.house) as avgscore from ScoreEvent s WHERE score > (SELECT AVG(w.score) FROM ScoreEvent.win:time_batch(10 sec) w WHERE s.house = w.house);
        try {
            epCompiled = compiler.compile("""
                    @public @buseventtype create json schema FormulaEvent(team string, driver string, result int, points int, place string, ts string);
                    @name('result') SELECT * FROM FormulaEvent;""", compilerArgs);
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
        String record;

        Map<Integer, Integer> placeToPoints = new HashMap<Integer, Integer>();
        placeToPoints.put(1, 25);
        placeToPoints.put(2, 18);
        placeToPoints.put(3, 15);
        placeToPoints.put(4, 12);
        placeToPoints.put(5, 10);
        placeToPoints.put(6, 8);
        placeToPoints.put(7, 6);
        placeToPoints.put(8, 4);
        placeToPoints.put(9, 2);
        placeToPoints.put(10, 1);
        for (int i = 11; i <= 20; i++)
            placeToPoints.put(i, 0);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + (1000L * howLongInSec)) {
            for (int i = 0; i < noOfRecordsPerSec; i++) {
//                String house = faker.harryPotter().house();
//                Timestamp timestamp = faker.date().past(30, TimeUnit.SECONDS);
//                record = Format.toJson()
//                        .set("house", () -> house)
//                        .set("character", () -> faker.harryPotter().character())
//                        .set("score", () -> String.valueOf(faker.number().randomDigitNotZero()))
//                        .set("ts", () -> timestamp.toString())
//                        .build().generate();
//                runtime.getEventService().sendEventJson(record, "ScoreEvent");

                String team = faker.formula1().team();

                Integer position = faker.number().numberBetween(1, 20);
                Timestamp timestamp = faker.date().past(365, TimeUnit.DAYS);
                Integer points = placeToPoints.get(position);


                record = Format.toJson()
                        .set("team", () -> team)
                        .set("driver", () -> faker.formula1().driver())
                        .set("result", () -> position)
                        .set("points", () -> points)
                        .set("place", () -> faker.formula1().grandPrix())
                        .set("ts", () -> timestamp.toString())
                        .build().generate();
                runtime.getEventService().sendEventJson(record, "FormulaEvent");

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

