from pulp import *
import numpy as np

# Utworzenie instancji problemu
model = LpProblem(name="jakis-problem", sense=LpMaximize)

# Utworzenie czterech zmiennych decyzyjnych
epsilon = LpVariable(name="epsilon", lowBound=0, cat='Continuous')
w1 = LpVariable(name="w1", lowBound=0, cat='Continuous')
w2 = LpVariable(name="w2", lowBound=0, cat='Continuous')
w3 = LpVariable(name="w3", lowBound=0, cat='Continuous')
w4 = LpVariable(name="w4", lowBound=0, cat='Continuous')

# Ograniczenia problemu
model += (0.45*w1 + 0.86*w2 + 0*w3 + 0.73*w4 >= 0.83*w1 + 0.25*w2 + 0.8*w3 + 0.65*w4 + epsilon, "#1 constraint")
model += (1*w1 + 0.45*w2 + 0.57*w3 + 0.5*w4 >= 0.71*w1 + 0.25*w2 + 0.88*w3 + 0.67*w4 + epsilon, "#2 constraint")
# model += (0.74*w1 + 0.25*w2 + 0.8*w3 + 0.49*w4 >= 0.83*w1 + 0.25*w2 + 0.8*w3 + 0.65*w4 + epsilon, "#3 constraint")
# model += (1*w1 + 0.45*w2 + 0.57*w3 + 0.5*w4 + epsilon >= 0.76*w1 + 0.06*w2 + 1*w3 + 0.6*w4, "#4 constraint")
# model += (0.32*w1 + 0.83*w2 + 0*w3 + 0.94*w4 + epsilon >= 0.59*w1 + 0.24*w2 + 0.7*w3 + 0.63*w4, "#5 constraint")
# model += (w1 + w2 + w3 + w4 == 1, "#6 constraint")
# model += (w1 >= 0, "#7 constraint")
# model += (w2 >= 0, "#8 constraint")
# model += (w3 >= 0, "#9 constraint")
# model += (w4 >= 1, "#10 constraint")


# Funkcja celu 
obj_func = epsilon
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
print("w1: ", w1.value())
print("w2: ", w2.value())
print("w3: ", w3.value())
print("w4: ", w4.value())
# WYNIK
#    1.6666667
#    2.6666667