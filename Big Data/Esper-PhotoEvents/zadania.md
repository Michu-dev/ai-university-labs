# Opis charakteru danych
W ramach pewnego portalu internetowego zrzeszającego pasjonatów fotografii
publikowane są zdjęcia.

Każda fotografia zawiera kilka podstawowych informacji, takich jak użyty do jej wykonania
sprzęt, parametr ISO oraz rozmiar.

Dodatkowo, aby ułatwić wyszukiwanie zdjęć o tematyce, której poszukuje użytkownik,
każdy obraz przypisany ma temat przewodni.

W strumieniu pojawiają się zdarzenia zgodne ze schematem `PhotoEvent`.

```
create json schema PhotoEvent(camera string, genre string, iso int, width int, height int, ets string, its string);
```

Każde zdarzenie związane z jest z faktem opublikowania przez użytkownika fotografii. 

Dane są uzupełnione są o dwie etykiety czasowe. 
* Pierwsza (`ets`) związana jest z momentem publikacji zdjęcia. 
  Etykieta ta może się losowo spóźniać w stosunku do czasu systemowego, maksymalnie do 45 sekund.
* Druga (`its`) związana jest z momentem rejestracji zdarzenia w systemie.

# Opis atrybutów
- **camera**
  - typ: string
  - znaczenie: marka oraz model użytego aparatu do zrobienia zdjęcia
  - kategoria: atrybut lub atrybuty dodatkowe, opisujące zdarzenie
- **genre**
  - typ: string
  - znaczenie: rodzaj fotografii, temat przewodni
  - kategoria: atrybut lub atrybuty, po których można grupować (zbiór ograniczono 10 możliwych wartości)
- **iso**
  - typ: int
  - znaczenie: wrażliwość na światło (im mniejsza wartość, tym mniejsza wrażliwość na światło i tym samym ciemniejsze zdjęcie)
  - kategoria: atrybut lub atrybuty, których wartości można agregować (wartości numeryczne)
- **width**
  - typ: int
  - znaczenie: szerokość zdjęcia, w pikselach
  - kategoria: atrybut lub atrybuty, których wartości można agregować (wartości numeryczne, liczba całkowita z przedziału [50, 10 000])
- **height**
  - typ: int
  - znaczenie: wysokość zdjęcia, w pikselach
  - kategoria: atrybut lub atrybuty, których wartości można agregować (wartości numeryczne, liczba całkowita z przedziału [50, 10 000])
- **ets**
  - typ: string
  - znaczenie: data i czas publikacji zdjęcia
  - format: ``yyyy-MM-dd HH:mm:ss``
  - kategoria: znacznik czasowy zdarzeń (inny niż czas systemowy), zaokrąglony do 1 sekundy
- **its**
  - typ: string
  - znaczenie: data i czas otrzymania zdarzenia
  - format: ``yyyy-MM-dd HH:mm:ss``
  - kategoria: czas systemowy, zaokrąglony do 1 sekundy

# Zadania
Opracuj rozwiązania poniższych zadań. 
* Opieraj się strumieniu zdarzeń zgodnych ze schematem `PhotoEvent`
* W każdym rozwiązaniu możesz skorzystać z jednego lub kilku poleceń EPL.
* Ostatnie polecenie będące ostatecznym rozwiązaniem zadania musi 
  * być poleceniem `select` 
  * posiadającym etykietę `result`, przykładowo:
  ```aidl
    @name('result') SELECT genre, count(*) howMany, ets, its
    from PhotoEvent#ext_timed(java.sql.Timestamp.valueOf(its).getTime(), 3 sec)
    group by genre;
  ```

## Zadanie 1
Dla każdego gatunku (tematu przewodniego) fotografii znajdź medianę wartości ISO z ostatnich 5 minut.

Wyniki powinny zawierać, następujące kolumny:
- `genre` - gatunek zdjęcia oraz 
- `median_iso` - mediana wartości ISO z ostatnich 5 minut.

## Zadanie 2
Wykrywaj zdjęcia o "niestandardowych" wymiarach. Za takowy uznajemy sytuację, gdy:
- wysokość fotografii stanowi co najwyżej 10% jej szerokości, lub
- szerokość fotografii stanowi co najwyżej 10% jej wysokości

Wyniki powinny zawierać wszystkie kolumny.

## Zadanie 3
Wykrywaj zdjęcia, dla których wartość ISO mocno różni się od mediany dla danego gatunku (tematu) zdjęcia z ostatnich 5 minut,
tzn. jest co najmniej 2 razy większa lub mniejsza od mediany.

Wyniki powinny zawierać wszystkie kolumny.

## Zadanie 4
Porównaj ze sobą średnią wartość ISO między 5 ostatnio zarejestrowanymi zdjęciami dla tematyki weselnej oraz beauty.
Porównanie wyraź za pomocą stosunku średnich dla tematyki weselnej do tematyki beauty.
Pamiętaj, żeby pominąć przypadki, dla których ilorazu nie da się obliczyć (z powodu np. braku zdjęć o którejś tematyce).

Wyniki powinny zawierać, następujące kolumny:
- `iloraz` - stosunek średniej wartości ISO dla tematyki weselnej do średniej wartości ISO dla tematyki beauty

## Zadanie 5
W ramach zadania stworzone zostało okno:
```agsl
create window BeautyPhotos#length(10) as PhotoEvent;
insert into BeautyPhotos select * from PhotoEvent where genre = 'Beauty';
```
Uzyskaj informację o seriach opublikowanych zdjęć o tematyce Beauty o długości co najmniej 2, 
dopóki szerokość zdjęć nie jest mniejsza od 5000. Dla każdej takiej sekwencji wypisz datę 
publikacji pierwszej fotografii z sekwencji, a także wartości ISO dwóch pierwszych 
elementów, o ile ISO pierwszego zdjęcia jest większe od 1000.

Wyniki powinny zawierać, następujące kolumny:
- `ets_first` - data publikacji pierwszego zdjęcia z serii
- `iso_first` - wartość ISO dla pierwszego zdjęcia z serii
- `iso_second` - wartość ISO dla drugiego zdjęcia z serii


## Zadanie 6
W ramach zadania stworzone zostało okno:
```agsl
create window BeautyPhotos#length(10) as PhotoEvent;
insert into BeautyPhotos select * from PhotoEvent where genre = 'Beauty';
```
Poszukujemy średniej wartości ISO dla trzech opublikowanych zdjęć, dla których:
- zdjęcie poprzedzające pozostałe ma ISO jest większe od 1000
- zdjęcie kolejne ma wysokość większą od 200 pikseli
- zdjęcie trzecie ma ISO większe od 1000, a w międzyczasie nie pojawia się żadna bardzo ciemna fotografia (z ISO mniejszym od 100)

Dodatkowo, wypisz czas publikacji każdego zdjęcia.

Wyniki powinny zawierać, następujące kolumny:
- `mean` - średnia wartość ISO trzech zdjęć
- `ets_first` - data publikacji pierwszego zdjęcia z serii
- `ets_second` - data publikacji drugiego zdjęcia z serii
- `ets_third` - data publikacji trzeciego zdjęcia z serii

## Zadanie 7
W ramach zadania stworzone zostało okno:
```agsl
create window BeautyPhotos#length(10) as PhotoEvent;
insert into BeautyPhotos select * from PhotoEvent where genre = 'Beauty';
```
Wykrywaj wzorce, gdzie każde kolejne zdarzenie w serii ma coraz mniejszą wartość ISO. 
Dla każdej serii wypisz początek oraz koniec spadku.

Wyniki powinny zawierać wszystkie kolumny.
