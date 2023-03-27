from pulp import *
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from collections import defaultdict

CRITERIA_NUMBER = 4
# pairs: (11, 22), (4, 27), (18, 27), (8, 23), (10, 19)

variants = {
    11: [0.61, 0.54, 0.38, 0.49], # 1
    22: [0.32, 0.83, 0, 0.94], # 0
    4: [0.48, 0.87, 0, 0.75], # 0
    27: [0.8, 0.06, 1, 0.67], # 0.5
    18: [0.76, 0.06, 1, 0.06], # 1
    27: [0.8, 0.06, 1, 0.67], # 0.5
    8: [0.64, 0.44, 0.54, 0.54], # 1
    23: [0.59, 0.24, 0.7, 0.63], # 0.5
    10: [0.45, 0.86, 0, 0.73], # 0.5
    19: [0.35, 0.91, 0, 0.98] # 0
}

# 11, 18, 8
# 27, 23, 10
# 22, 4, 19

# 11, 10, 8, 23, 4, 22, 19, 18, 27
# 11 == 10 > 8 (3 grupa) > 23 (3 grupa) > 4 (2 grupa) > 22 (4 grupa) == 19 > 18 (3 grupa) > 27 (dominacja) 
# 11 == 10 > 4 (4 grupa) > 22 (4 grupa) > 19 (4 grupa) > 8 (3, 4 grupa) == 18 > 23 (3, 4 grupa) > 27 (3, 4 grupa) 

# Utworzenie instancji problemu
model = LpProblem(name="Nuclear-waste-management-UTA", sense=LpMaximize)

# Utworzenie czterech zmiennych decyzyjnych
epsilon = LpVariable(name="epsilon", lowBound=0, cat='Continuous')
u_variables = defaultdict(lambda: {})

for v in variants:
    for i in range(CRITERIA_NUMBER):
        val = variants[v][i]
        u_variables[i][val] = LpVariable(name=f"u_{i}_{val}", lowBound=0, cat='Continuous')

ideal_utilities = [LpVariable(name=f"weight_{i}_0", lowBound=0, cat='Continuous') for i in range(CRITERIA_NUMBER)]
worst_utilities = [LpVariable(name=f"weight_{i}_1", lowBound=0, cat='Continuous') for i in range(CRITERIA_NUMBER)]



# Ograniczenia problemu
# normalizacja
model += (sum(ideal_utilities[i] for i in range(CRITERIA_NUMBER)) == 1, "normalization")
for i in range(CRITERIA_NUMBER):
    model += (worst_utilities[i] == 0, f"normalization_{i}")

for i in range(CRITERIA_NUMBER):
    model += (sum(u_variables[i][variants[j][i]] for j in variants) <= 0.5, f"u_{i} <= 0.5")

# ranking referencyjny
# 11 == 10 > 4 (4 grupa) > 22 (4 grupa) > 19 (4 grupa) > 8 (3, 4 grupa) == 18 > 23 (3, 4 grupa) > 27 (3, 4 grupa)
model += (
    sum(u_variables[i][variants[11][i]] for i in range(CRITERIA_NUMBER)) == sum(
    u_variables[i][variants[10][i]] for i in range(CRITERIA_NUMBER)
    ) + epsilon, '11 == 10'
)


model += (
    sum(u_variables[i][variants[10][i]] for i in range(CRITERIA_NUMBER)) >= sum(
    u_variables[i][variants[4][i]] for i in range(CRITERIA_NUMBER)
    ) + epsilon, '10 >= 4 (4 group preference information)'
)

model += (
    sum(u_variables[i][variants[4][i]] for i in range(CRITERIA_NUMBER)) >= sum(
    u_variables[i][variants[22][i]] for i in range(CRITERIA_NUMBER)
    ) + epsilon, '4 >= 22 (4 group preference information)'
)

model += (
    sum(u_variables[i][variants[22][i]] for i in range(CRITERIA_NUMBER)) >= sum(
    u_variables[i][variants[19][i]] for i in range(CRITERIA_NUMBER)
    ) + epsilon, '22 >= 19 (4 group preference information)'
)

model += (
    sum(u_variables[i][variants[19][i]] for i in range(CRITERIA_NUMBER)) >= sum(
    u_variables[i][variants[8][i]] for i in range(CRITERIA_NUMBER)
    ) + epsilon, '19 >= 8 (1 group preference information)'
)

model += (
    sum(u_variables[i][variants[8][i]] for i in range(CRITERIA_NUMBER)) == sum(
    u_variables[i][variants[18][i]] for i in range(CRITERIA_NUMBER)
    ) + epsilon, '8 == 18'
)

model += (
    sum(u_variables[i][variants[18][i]] for i in range(CRITERIA_NUMBER)) >= sum(
    u_variables[i][variants[23][i]] for i in range(CRITERIA_NUMBER)
    ) + epsilon, '18 >= 23 (1 group preference information)'
)

model += (
    sum(u_variables[i][variants[23][i]] for i in range(CRITERIA_NUMBER)) >= sum(
    u_variables[i][variants[27][i]] for i in range(CRITERIA_NUMBER)
    ) + epsilon, '23 >= 27 (1 group preference information)'
)


for i in range(CRITERIA_NUMBER):
    sorted_keys = sorted(u_variables[i].keys())
    for j in range(len(sorted_keys) - 1):
        model += (u_variables[i][sorted_keys[j]] >= u_variables[i][sorted_keys[j + 1]], f"weight_{i}_{sorted_keys[j]} >= weight_{i}_{sorted_keys[j + 1]}")
    model += (u_variables[i][sorted_keys[0]] <= ideal_utilities[i], f"weight_{i}_0 >= weight_{i}_{sorted_keys[0]}")
    model += (u_variables[i][sorted_keys[len(sorted_keys ) - 1]] >= worst_utilities[i], f"weight_{i}_{sorted_keys[len(sorted_keys ) - 1]} >= weight_{i}_1")

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

criteria_plots = {}
# Wypisanie wartosci zmiennych decyzyjnych
for i in range(CRITERIA_NUMBER):
    sorted_keys = sorted(u_variables[i].keys())
    for j in sorted_keys:
        if i not in criteria_plots:
            criteria_plots[i] = [(j, u_variables[i][j].value())]
        else:
            criteria_plots[i].append((j, u_variables[i][j].value()))
        print(f"weight_{i}_{j}: ", u_variables[i][j].value())

variants_uta_values = defaultdict(lambda: 0)
for v in variants:
    for i in range(CRITERIA_NUMBER):
        # if v not in variants_uta_values:
        variants_uta_values[v] += u_variables[i][variants[v][i]].value()

print('UTA values: ')
for k in variants_uta_values.keys():
    print(str(k) + ': ' + str(variants_uta_values[k]))

# Rysowanie wykresów funkcji użyteczności
for i in range(CRITERIA_NUMBER):
    x = [tup[0] for tup in criteria_plots[i]]
    y = [tup[1] for tup in criteria_plots[i]]
    print(x)
    print(y)
    plt.subplot(2, 2, i + 1)
    plt.title(f"{i + 1} criteria")
    plt.plot(x, y)

plt.tight_layout()
plt.show()
# WYNIK
#    1.6666667
#    2.6666667