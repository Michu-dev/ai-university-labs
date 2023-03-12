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
import net.datafaker.Vehicle;
import net.datafaker.fileformats.Format;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class EsperClient {
    public static void main(String[] args) throws InterruptedException {
        int noOfRecordsPerSec;
        int howLongInSec;
        if (args.length < 2) {
            noOfRecordsPerSec = 20;
            howLongInSec = 30;
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
                    @public @buseventtype create json schema MotorSportEvent(car string, team string, driver string, car_type string, fuel_type string, result int, points int, place string, ts string);
                    @name('result') SELECT * FROM MotorSportEvent;""", compilerArgs);
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
        Map<String, String[]> modelsDict = new HashMap<String, String[]>();
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

        modelsDict.put("Audi", new String[] { "A4", "A5", "S5", "A7", "A8" });
        modelsDict.put("BMW", new String[] { "328i", "M3", "M5", "X1", "X3", "X5" });
        modelsDict.put("Buick", new String[] { "Enclave", "Regal", "LaCrosse", "Verano", "Encore", "Riveria" });
        modelsDict.put("Chevy", new String[] { "Camero", "Silverado", "Malibu" });
        modelsDict.put("Citroën", new String[] { "C3", "C4", "C5" });
        modelsDict.put("Dacia", new String[] { "Duster", "Sandero", "Spring" });
        modelsDict.put("Dodge", new String[] { "Ram", "Challenger", "Charger", "Durango" });
        modelsDict.put("Fiat", new String[] { "500", "Panda", "Tipo" });
        modelsDict.put("Ford", new String[] { "Mustang", "F150", "Focus", "Fiesta", "Puma" });
        modelsDict.put("Honda", new String[] { "Accord", "Civic", "CR-V", "Odyssey" });
        modelsDict.put("Hyundai", new String[] { "i10", "i20", "i30", "IONIQ 5", "Kona", "Tucson" });
        modelsDict.put("Kia", new String[] { "Ceed", "EV6", "Niro", "Picanto", "Sportage" });
        modelsDict.put("Lincoln", new String[] { "Navigator", "MKZ", "MKX", "MKS" });
        modelsDict.put("Mazda", new String[] { "CX-3", "CX-30", "CX-5", "CX-8", "CX-9", "MX-5", "BT-50" });
        modelsDict.put("Mercedes-Benz", new String[] { "A-Class", "B-Class", "C-Class", "E-Class" });
        modelsDict.put("Nio", new String[] { "ET5", "ET7", "ES6", "ES7", "ES8", "EC6", "EP9" });
        modelsDict.put("Nissan", new String[] { "Rogue", "Juke", "Cube", "Pathfiner", "Versa", "Altima", "Micra", "Qashqai" });
        modelsDict.put("Opel", new String[] { "Astra", "Corsa", "Crossland", "Mokka" });
        modelsDict.put("Peugeot", new String[] { "108", "208", "2008", "308", "3008", "408", "508" });
        modelsDict.put("Renault", new String[] { "Captur", "Clio", "Megane", "Twingo", "Zoe" });
        modelsDict.put("Seat", new String[] { "Arona", "Ateca", "Ibiza", "Leon", "Tarraco" });
        modelsDict.put("Skoda", new String[] { "Enyaq", "Fabia", "Kodiaq", "Octavia", "Superb" });
        modelsDict.put("Tesla", new String[] { "Model S", "Model 3", "Model X", "Model Y", "Roadster" });
        modelsDict.put("Toyota", new String[] { "Prius", "Camry", "Corolla", "Yaris" });
        modelsDict.put("Volkswagen", new String[] { "Golf", "ID.3", "ID.4", "Polo", "T-Roc", "Tiguan", "Up" });
        modelsDict.put("Volvo", new String[] { "C40", "XC40", "XC60", "S60", "S90", "V60", "V90" });
        modelsDict.put("Xpeng", new String[] { "G9", "G3i", "P7", "P5"});


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

//                String team = faker.formula1().team();

                Integer position = faker.number().numberBetween(1, 20);
                Timestamp timestamp = faker.date().past(365, TimeUnit.DAYS);
                Integer points = placeToPoints.get(position);

                Vehicle vehicle = faker.vehicle();

                String car = vehicle.model();
                String team = new String();
                String engine = vehicle.engine();

                String driver = faker.name().name();

                Iterator<Map.Entry<String, String[]>> iterator = modelsDict.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String[]> entry = iterator.next();
                    List<String> list = Arrays.asList(entry.getValue());

                    if(list.contains(car)) {
                        team = entry.getKey();
                        break;
                    }

                }

                String carType = vehicle.carType();
                String fuelType = vehicle.fuelType();


                String finalTeam = team;
                record = Format.toJson()
                        .set("car", () -> car)
                        .set("team", () -> finalTeam)
                        .set("driver", () -> driver)
                        .set("car_type", () -> carType)
                        .set("fuel_type", () -> fuelType)
                        .set("result", () -> position)
                        .set("points", () -> points)
                        .set("place", () -> faker.formula1().grandPrix())
                        .set("ts", () -> timestamp.toString())
                        .build().generate();
                runtime.getEventService().sendEventJson(record, "MotorSportEvent");

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

