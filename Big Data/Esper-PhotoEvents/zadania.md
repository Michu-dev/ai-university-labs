# Opis charakteru danych
W ramach pewnego portalu internetowego zrzeszającego pasjonatów fotografii
publikowane są zdjęcia.

W strumieniu pojawiają się zdarzenia zgodne ze schematem `PhotoEvent`.

```
create json schema PhotoEvent(camera string, genre string, iso int, width int, height int, ets string, its string);
```

Każde zdarzenie związane z jest z faktem opublikowania przez użytkownika pojedynczej fotografii. 

Każda fotografia zawiera kilka podstawowych informacji, takich jak użyty do jej wykonania
sprzęt, parametr ISO oraz rozmiar.

Dodatkowo, aby ułatwić wyszukiwanie zdjęć o tematyce, której poszukuje użytkownik,
każdy obraz przypisany ma temat przewodni.

Dane są uzupełnione są o dwie etykiety czasowe. 
* Pierwsza (`ets`) związana jest z momentem publikacji zdjęcia. 
  Etykieta ta może się losowo spóźniać w stosunku do czasu systemowego, maksymalnie do 45 sekund.
* Druga (`its`) związana jest z momentem rejestracji zdarzenia w systemie.

# Opis atrybutów
- `camera` - marka oraz model użytego aparatu do zrobienia zdjęcia
- `genre` - rodzaj fotografii, temat przewodni
- `iso` - wrażliwość na światło (im mniejsza wartość, tym mniejsza wrażliwość na światło i tym samym ciemniejsze zdjęcie)
- `width` - szerokość zdjęcia, w pikselach
- `height` - wysokość zdjęcia, w pikselach
- `ets` - data i czas wykonanania zdjęcia
- `its` - data i czas publikacji zdarzenia

# Zadania
Opracuj rozwiązania poniższych zadań. 
* Opieraj się strumieniu zdarzeń zgodnych ze schematem `PhotoEvent`
* W każdym rozwiązaniu możesz skorzystać z jednego lub kilku poleceń EPL.
* Ostatnie polecenie będące ostatecznym rozwiązaniem zadania musi 
  * być poleceniem `select` 
  * posiadającym etykietę `answer`, przykładowo:
  ```aidl
    @name('answer') SELECT genre, count(*) howMany, ets, its
    from PhotoEvent#ext_timed(java.sql.Timestamp.valueOf(its).getTime(), 3 sec)
    group by genre;
  ```

## Zadanie 1
Dla każdego gatunku (tematu przewodniego) fotografii znajdź medianę wartości ISO z ostatniej minuty.

Wyniki powinny zawierać następujące kolumny:
- `genre` - gatunek zdjęcia oraz 
- `median_iso` - mediana wartości ISO.

## Zadanie 2
Wykrywaj zdjęcia o "niestandardowych" wymiarach. Za niestandardowy wymiar uznajemy sytuację, gdy:
- wysokość fotografii stanowi co najwyżej 10% jej szerokości, lub
- szerokość fotografii stanowi co najwyżej 10% jej wysokości

Wyniki powinny zawierać wszystkie kolumny zdarzenia publikacji zdjęcia.

## Zadanie 3
Wykrywaj zdjęcia, dla których wartość ISO mocno różni się od mediany dla danego gatunku (tematu) zdjęcia z ostatnich 5 minut,
tzn. jest co najmniej 2 razy większa lub 2 razy mniejsza od mediany.

Wyniki powinny zawierać wszystkie kolumny zdarzenia publikacji zdjęcia.

## Zadanie 4
Porównaj ze sobą średnią wartość ISO między 5 ostatnio zarejestrowanymi zdjęciami dla tematyki weselnej oraz beauty.
Porównanie wyraź za pomocą stosunku średnich dla tematyki weselnej do tematyki beauty.
Pomijaj przypadki, dla których ilorazu nie da się obliczyć (z powodu np. braku zdjęć o którejś tematyce).

Wyniki powinny zawierać następujące kolumny:
- `iloraz` - stosunek średniej wartości ISO dla tematyki weselnej do średniej wartości ISO dla tematyki beauty

## Zadanie 5
Ograniczając się do analizy zdjęć o tematyce Beauty wyszukuj informacje na temat serii zdjęć o długości co najmniej 2, w czasie której szerokość zdjęcia nie przekroczyła 5000. Dla każdej takiej serii wypisz datę 
publikacji pierwszej fotografii, a także wartości ISO dwóch pierwszych 
elementów.

Wyniki powinny zawierać, następujące kolumny:
- `its_first` - data publikacji pierwszego zdjęcia z serii
- `iso_first` - wartość ISO dla pierwszego zdjęcia z serii
- `iso_second` - wartość ISO dla drugiego zdjęcia z serii


## Zadanie 6
Poszukujemy średniej wartości ISO dla trzech kolejno (nie koniecznie bezpośrednio) opublikowanych zdjęć, dla których:
- zdjęcie poprzedzające pozostałe ma ISO jest większe od 1000
- zdjęcie kolejne ma wysokość większą od 200 pikseli
- zdjęcie trzecie ma ISO większe od 1000, a w międzyczasie (od drugiego do trzeciego zdjęcia) nie pojawia się żadna bardzo ciemna fotografia (z ISO mniejszym od 100).

Wyniki powinny zawierać następujące kolumny:
- `mean` - średnia wartość ISO wszystkich trzech zdjęć
- `its_first` - data publikacji pierwszego zdjęcia z serii
- `its_second` - data publikacji drugiego zdjęcia z serii
- `its_third` - data publikacji trzeciego zdjęcia z serii

## Zadanie 7
Wykrywaj serie publikacji zdjęć, gdzie każda kolejna publikacja w serii ma coraz mniejszą wartość ISO. 
Dla każdej serii wypisz czas pierwszej i ostatniej publikacji.

Wyniki powinny zawierać następujące kolumny:
- `its_start` - data publikacji pierwszego zdjęcia z serii
- `its_end` - data publikacji ostatniego zdjęcia z serii
