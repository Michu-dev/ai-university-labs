from pulp import *
import numpy as np

# Utworzenie instancji problemu
model = LpProblem(name="jakis-problem", sense=LpMaximize)

# Utworzenie dwoch zmiennych decyzyjnych
x1 = LpVariable(name="x1", lowBound=0, cat='Continuous')
x2 = LpVariable(name="x2", lowBound=0, cat='Continuous')

# Ograniczenia problemu
model += (1 * x1 + 1 * x2 >= 1, "#1 constraint")
model += (2 * x1 + 1 * x2 <= 6, "#2 constraint")
model += (-1 * x1 + 1 * x2 == 1, "#3 constraint")

# Funkcja celu 
obj_func = 4 * x1 + 2 * x2
model += obj_func

# Uruchomienie solvera
status = model.solve()

# Wypisanie statusu
print(f"status: {model.status}, {LpStatus[model.status]}")

# WYNIK: status: 1, Optimal
# Wypisanie realizacji funkcji celu
print(f"objective: {model.objective.value()}")
# WYNIK: objective: 12.000000199999999

# Wypisanie wartosci zmiennych decyzyjnych
print(x1.value())
print(x2.value())
# WYNIK
#    1.6666667
#    2.6666667