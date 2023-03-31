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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class EsperClient {

    public static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }

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
//        car_type string, fuel_type string,
//        @public @buseventtype create json schema ScoreEvent(house string, character string, score int, ts string);
//        @name('result') SELECT s.score as score, s.character as character, s.house as house, (SELECT AVG(w.score) FROM ScoreEvent.win:time_batch(10 sec) w WHERE s.house = w.house) as avgscore from ScoreEvent s WHERE score > (SELECT AVG(w.score) FROM ScoreEvent.win:time_batch(10 sec) w WHERE s.house = w.house);
        try {
            epCompiled = compiler.compile("""
                    @public @buseventtype create json schema TrafficEvent(car string, manufacturer string, city string, car_owner string, velocity int, fine int, penalty_points int, ets string, its string);
                    create window TrafficEventWindow#length(10) as TrafficEvent;
                    insert into TrafficEventWindow select * from TrafficEvent;
                    @name('result') select * from TrafficEventWindow match_recognize (
                                    partition by city
                                    measures st.city as car_owner,
                                    st.velocity as startVelocity,
                                    LAST(low.velocity) as lowVelocity,
                                    LAST(high.velocity) as highVelocity,
                                    st.ets as startEts,
                                    LAST(high.ets) as stopEts
                                    pattern (st low+ high+)
                                    define
                                    st as st.velocity >= 100,
                                    low as low.velocity < PREV(low.velocity) AND low.velocity <= 50,
                                    high as high.velocity > PREV(high.velocity) AND high.velocity >= 100
                                   );""", compilerArgs);
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

        int numberOfPeople = 400;

        // Add a listener to the statement to handle incoming events
        resultStatement.addListener( (newData, oldData, stmt, runTime) -> {
            for (EventBean eventBean : newData) {
                System.out.printf("R: %s%n", eventBean.getUnderlying());
            }
        });

        Faker faker = new Faker(new Locale("pl"));
        String record;

        Map<String, String[]> modelsDict = new HashMap<String, String[]>();
        Map<Integer, String> polishCities = new HashMap<Integer, String>();
        Map<Integer, String> people = new HashMap<Integer, String>();

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

        polishCities.put(1, "Warszawa");
        polishCities.put(2, "Kraków");
        polishCities.put(3, "Wrocław");
        polishCities.put(4, "Łódź");
        polishCities.put(5, "Poznań");
        polishCities.put(6, "Gdańsk");
        polishCities.put(7, "Szczecin");
        polishCities.put(8, "Bydgoszcz");
        polishCities.put(9, "Lublin");
        polishCities.put(10, "Białystok");
        polishCities.put(11, "Toruń");
        polishCities.put(12, "Gorzów Wielkopolski");
        polishCities.put(13, "Zielona Góra");
        polishCities.put(14, "Opole");
        polishCities.put(15, "Rzeszów");
        polishCities.put(16, "Katowice");
        polishCities.put(17, "Kielce");
        polishCities.put(18, "Olsztyn");



        for (int i = 1; i <= numberOfPeople; i++) {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            String fullName = firstName + " " + lastName;
            people.put(i, fullName);
        }


        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + (1000L * howLongInSec)) {
            for (int i = 0; i < noOfRecordsPerSec; i++) {

                Integer velocity = faker.number().numberBetween(1, 150);
                Integer cityIndex = faker.number().numberBetween(1, 18);
                Integer personIndex = faker.number().numberBetween(1, numberOfPeople);
                String timestamp = faker.date().past(60, TimeUnit.SECONDS, "YYYY-MM-dd hh:mm:ss");

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YYYY-MM-dd hh:mm:ss");
                LocalDateTime now = LocalDateTime.now();


                Integer fine = 0, penalty_points = 0;

                if(isBetween(velocity, 51, 60)) {
                    fine = 50;
                    penalty_points = 1;
                } else if (isBetween(velocity, 61, 65)) {
                    fine = 100;
                    penalty_points = 2;
                } else if (isBetween(velocity, 66, 70)) {
                    fine = 200;
                    penalty_points = 3;
                } else if (isBetween(velocity, 71, 75)) {
                    fine = 300;
                    penalty_points = 5;
                } else if (isBetween(velocity, 76, 80)) {
                    fine = 400;
                    penalty_points = 7;
                } else if (isBetween(velocity, 81, 90)) {
                    fine = 800;
                    penalty_points = 9;
                } else if (isBetween(velocity, 91, 100)) {
                    fine = 1000;
                    penalty_points = 11;
                } else if (isBetween(velocity, 101, 110)) {
                    fine = 1500;
                    penalty_points = 13;
                } else if (isBetween(velocity, 111, 120)) {
                    fine = 2000;
                    penalty_points = 14;
                } else if (velocity > 121) {
                    fine = 2500;
                    penalty_points = 15;
                }

                Vehicle vehicle = faker.vehicle();

                String car = vehicle.model();
                String manufacturer = new String();

                String carOwner = people.get(personIndex);

                Iterator<Map.Entry<String, String[]>> iterator = modelsDict.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String[]> entry = iterator.next();
                    List<String> list = Arrays.asList(entry.getValue());

                    if(list.contains(car)) {
                        manufacturer = entry.getKey();
                        break;
                    }

                }

                String city = polishCities.get(cityIndex);


                String finalManufacturer = manufacturer;
                Integer finalFine = fine;
                Integer finalPenaltyPoints = penalty_points;
                record = Format.toJson()
                        .set("car", () -> car)
                        .set("manufacturer", () -> finalManufacturer)
                        .set("city", () -> city)
                        .set("car_owner", () -> carOwner)
                        .set("velocity", () -> velocity)
                        .set("fine", () -> finalFine)
                        .set("penalty_points", () -> finalPenaltyPoints)
                        .set("ets", () -> timestamp)
                        .set("its", () -> dtf.format(now))
                        .build().generate();
                runtime.getEventService().sendEventJson(record, "TrafficEvent");

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

