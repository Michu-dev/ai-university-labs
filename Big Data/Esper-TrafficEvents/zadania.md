# Charakterystyka danych
Dane prezentują losowe dane uzyskiwane przy użyciu "fotoradarów" (czujników) ustawionych
w różnych miejscach w Polsce. Dla zasady przyjmuję, że dane dotyczące pojazdu, właściciela
itp. są przechowywane nie tylko po przekroczeniu ustawowej prędkości 50 km/h, ale są one
generowane dla wszystkich przejeżdżających w danym miejscu aut. Dlatego "fotoradar" pełni
bardziej funkcję czujnika, aniżeli narzędzia służącego do kontroli przepisów ruchu drogowego.

W strumieniu pojawiają się zdarzenia zgodne ze schematem `TrafficEvent`.

```
create json schema TrafficEvent(car string, manufacturer string, city string, 
car_owner string, velocity int, fine int, penalty_points int, ets string, its string);
```

Przyjmuję, że czujniki są ustawione na obszarach zabudowanych, w wybranych największych
polskich miastach. Dlatego po przekroczeniu ustawowej prędkości 50 km/h dla właścicieli 
pojazdów będą generowane mandaty na podstawie polityki obowiązującej w 2023 roku.

Dane są uzupełnione o dwie etykiety czasowe. 
* Pierwsza (`ets`) związana jest z momentem wykonania pomiaru prędkości. 
  Etykieta ta może się losowo spóźniać w stosunku do czasu systemowego max do 60 sekund.
* Druga (`its`) związana jest z momentem rejestracji zdarzenia zdobycia punktów w systemie.

# Opis atrybutów

Atrybuty w każdym zdarzeniu zgodnym ze schematem `TrafficEvent` mają następujące znaczenie:

* `car` - nazwa marki samochodu
* `manufacturer` - nazwa producenta danego samochodu
* `car_owner` - imię i nazwisko właściciela pojazdu
* `city` - miasto, w którym wykonano pomiar (zdjęcie) prędkości pojazdu
* `velocity` - prędkość pojazdu w punkcie pomiarowym
* `fine` - kara pieniężna za przekroczenie prędkości (w PLN)
* `penalty_points` - punkty karne za złamanie przepisu ruchu drogowego (przekroczenie 
prędkości), przyjmuję że co najmniej 24 punkty oznaczają utratę prawa jazdy
* `ets` - czas rejestracji zdarzenia (pomiaru prędkości wraz z identyfikacją pojazdu)
* `its` - czas rejestracji zdarzenia związanego z pomiarem prędkości w systemie

# Zadania
Opracuj rozwiązania poniższych zadań. 
* Opieraj się strumieniu zdarzeń zgodnych ze schematem `TrafficEvent`
* W każdym rozwiązaniu możesz skorzystać z jednego lub kilku poleceń EPL.
* Ostatnie polecenie będące ostatecznym rozwiązaniem zadania musi 
  * być poleceniem `select` 
  * posiadającym etykietę `answer`, przykładowo:
  ```
    @name('answer') SELECT car, city, sum(penalty_points) as sumPoints, count(*) howMany, ets, its
    from TrafficEvent#ext_timed(java.sql.Timestamp.valueOf(its).getTime(), 3 sec)
  ```

## Zadanie 1
Utrzymuj informacje o maksymalnych zarejestrowanych prędkościach
w poszczególnych miastach w ciągu ostatnich 5 minut.

Wyniki powinny zawierać, następujące kolumny:
- `city` - nazwę miasta
- `max_velocity` - maksymalną zarejestrowaną prędkość w ciągu ostatnich 5 minut.

## Zadanie 2
Wykrywaj przypadki przekroczenia prędkości 100 km/h przez danego kierowcę w danym aucie.

Wyniki powinny zawierać, następujące kolumny:
- `car_owner` - imię i nazwisko właściciela pojazdu
- `manufacturer` - nazwę producenta danego samochodu
- `car` - nazwę marki danego samochodu
- `velocity` - konkretna wartość prędkości danego kierowcy.

## Zadanie 3
Wykrywaj przypadki zdobycia przez właścicieli pojazdów liczby punktów karnych większej
lub równej 24, która oznacza pozbawienie danego kierowcy prawa jazdy.

Wyniki powinny zawierać, następujące kolumny:
- `car_owner` - imię i nazwisko właściciela pojazdu
- `penalty_points_sum` - dokładną liczbę zdobytych punktów karnych.

## Zadanie 4
Zlicz, ilu osobom zabrano prawo jazdy za przekroczenie 100 km/h w terenie zabudowanym
w ciągu ostatnich 5 minut (określonego czasu) w poszczególnych miastach. Wykorzystaj
nazwane okno do wybrania odpowiednich pomiarów, w których dana osoba przekroczyła
100 km/h. W ostatnim kroku zagreguj poszczególne miasta i zlicz osoby, którym prawo jazdy
może zostać zabrane.

Wyniki powinny zawierać, następujące kolumny:
- `city` - nazwę miasta
- `how_many` - dokładną liczbę osób, którym prawo jazdy może zostać odebrane
- `ets` - czas rejestracji zdarzenia.

## Zadanie 5
Wybierz kierowców (właścicieli pojazdów), którzy poruszają się z większą prędkością,
niż średnia prędkość dla danego miasta w ciągu ostatnich np. 5 minut. Wykorzystaj tabelę
dla agregacji danych dla poszczególnych miast z ostatnich 5 minut. Następnie uzyskaną tabelę
złącz z prostym, ogólnym oknem bieżących danych i wybierz kierowców na podstawie odpowiedniego
warunku.

Wyniki powinny zawierać, następujące kolumny:
- `car_owner` - imię i nazwisko kierowcy (właściciela pojazdu)
- `car` - nazwę marki samochodu, którym porusza się kierowca
- `manufacturer` - nazwę producenta samochodu
- `city` - nazwę miasta, dla którego zmierzona została średnia prędkość z ostatnich 5 minut
- `velocity` - wartość prędkości dla danego kierowcy
- `avg_city_velocity` - średnią wartość prędkości dla danego miasta w ciągu ostatnich 5 minut.

## Zadanie 6
Uzyskaj informacje o seriach pomiarów w mieście Poznań mających długość co najmniej 3,
w których wartość prędkości nie spadała poniżej 100 km/h. Pobierz imię i nazwisko 
właściciela pojazdów, ich prędkości oraz czas rejestracji zdarzenia. Czas, w ramach
którego należy oczekiwać na dany wzorzec to 5 sekund. Kierowcy (utożsamiani z właścicielami)
to prawdopodobnie uczestnicy wyścigu ulicznego.

Wyniki powinny zawierać, następujące kolumny:
- `d1` - imię i nazwisko pierwszego właściciela pojazdu
- `v1` - zarejestrowaną prędkość dla pierwszego kierowcy
- `ets1` - czas pomiaru prędkości dla pierwszego kierowcy
- `d2` - imię i nazwisko drugiego właściciela pojazdu
- `v2` - zarejestrowaną prędkość dla drugiego kierowcy
- `ets2` - czas pomiaru prędkości dla drugiego kierowcy.
- `d3` - imię i nazwisko trzeciego właściciela pojazdu
- `v3` - zarejestrowaną prędkość dla trzeciego kierowcy
- `ets3` - czas pomiaru prędkości dla trzeciego kierowcy.

## Zadanie 7
Znajdź serię 3 pomiarów dla danej marki samochodu, z których wyróżnisz startowy pomiar
prędkości oraz pomiary, w których wykryto niższą oraz wyższą prędkość od startowej.
Kolejny pomiar zarejestrowany, na którego wzorzec należy oczekiwać 5 sekund, powinien
przekroczyć 100 km/h. Odnotujemy w ten sposób, czy dana marka samochodu sprzyja do 
szybkiej jazdy na podstawie poprzednich pomiarów, a zatem sprawdzimy, czy jej użytkownicy
są narażeni na otrzymywanie wysokich mandatów przez pokusę wykorzystania większej mocy
silnika.

Wyniki powinny zawierać, następujące kolumny:
- `car` - nazwę danej marki pojazdu
- `st_velocity` - startową wartość prędkości
- `low_velocity` - niższą wartość prędkości od prędkości startowej
- `high_velocity` - wyższą wartość prędkości od prędkości startowej.

## Zadanie 8
Wykrywaj serię pomiarów prędkości dla poszczególnych polskich miast układających
się w kształt litery V. Seria, inaczej niż w przypadku poprzedniego zadania, nie musi
składać się z trzech pomiarów. Wykryjemy w ten sposób liczne wahania prędkości w określonym
czasie dla danego miasta (prędkości te na wykresie czasu układałyby się w kształt litery V).

Wyniki powinny zawierać, następujące kolumny:
- `city` - nazwę miasta
- `start_velocity` - startową wartość prędkości ("start" litery V na wykresie czasu
rejestracji zdarzeń)
- `low_velocity` - niższą wartość prędkości od prędkości startowej
- `high_velocity` - wyższą wartość prędkości od prędkości startowej ("koniec" litery V na
wykresie czasu rejestracji zdarzeń).
- `start_ets` - czas rejestracji zdarzenia początkującego odchylenia prędkości od normy w 
danym mieście
- `end_ets` - czas rejestracji zdarzenia kończącego odchylenia prędkości od normy w
danym mieście (kompletującego formowanie litery V na wykresie czasu rejestracji zdarzeń).
