# PhotoEvents (na podstawie providera Photography)

## Opis charakteru danych
W ramach pewnego portalu internetowego zrzeszającego pasjonatów fotografii
publikowane są zdjęcia.

Każda fotografia zawiera kilka podstawowych informacji, takich jak użyty do jej wykonania
sprzęt, parametr ISO oraz rozmiar.

Dodatkowo, aby ułatwić wyszukiwanie zdjęć o tematyce, której poszukuje użytkownik,
każdy obraz przypisany ma temat przewodni.

Dane są uzupełnione o etykietę czasową, która definiuje moment publikacji.
Etykieta ta może się losowo spóźniać w stosunku do czasu systemowego, maksymalnie do 45 sekund.

## Opis atrybutów
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

## Opis trzech przykładowych analiz

### Agregacja
Dla każdego gatunku (tematu przewodniego) fotografii znajdź medianę wartości ISO z ostatnich 5 minut.

### Wykrywanie anomalii
Wykrywaj zdjęcia o "niestandardowych" wymiarach. Za takowy uznajemy sytuację, gdy:
* wysokość fotografii stanowi co najwyżej 10% jej szerokości, lub
* szerokość fotografii stanowi co najwyżej 10% jej wysokości

### Wykrywanie anomalii oparte na agregacji
Wykrywaj zdjęcia, dla których wartość ISO jest co najmniej 2 razy większa od mediany
dla danego gatunku (tematu) z ostatnich 5 minut.
