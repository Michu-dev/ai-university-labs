from pulp import *
import pandas as pd
from collections import defaultdict
from copy import deepcopy
import numpy as np


def get_efficiency(input, output, city):
    cities = input.index.values.tolist()
    model = LpProblem(name="Airports-DEA", sense=LpMinimize)
    theta = LpVariable("theta", 0)
    lambdas = LpVariable.dicts("lambda", cities, 0)
    model += theta
    for inp in input.columns:
        model += lpSum([input.loc[city][inp] * lambdas[city] for city in cities]) <= input.loc[city][inp] * theta
    for out in output.columns:
        model += lpSum([output.loc[city][out] * lambdas[city] for city in cities]) >= output.loc[city][out]
    model.solve()
    return (value(theta), lambdas)

def calculate_efficiency(input, output):
    cities = input.index.values.tolist()
    results_dict = defaultdict(dict)
    for city in cities:
        result = get_efficiency(input, output, city)
        lambdas = dict()
        for key, val in result[1].items():
            lambdas[key] = value(val)
        results_dict[city]["efficiency"] = result[0]
        results_dict[city]["lambdas"] = lambdas
    return results_dict

def calculate_hcu(input, output, non_effective_units, results):
    hcu = dict()
    for city in non_effective_units:
        hcu[city] = dict()
        for inp in input.columns:
            hcu[city][inp] = sum([val * input.loc[key][inp] for key, val in results[city]['lambdas'].items()])
        for out in output.columns:
            hcu[city][out] = sum([val * output.loc[key][out] for key, val in results[city]['lambdas'].items()])
    return hcu

def calculate_corrections(input, output, non_effective_units, hcu):
    corrections = dict()
    for city in non_effective_units:
        corrections[city] = dict()
        for inp in input.columns:
            corrections[city][inp] = input.loc[city][inp] - hcu[city][inp]
        for out in output.columns:
            corrections[city][out] = hcu[city][out] - output.loc[city][out]

    return corrections

def get_superefficiency(input, output, city):
    cities = input.index.values.tolist()
    cities_without_city = deepcopy(cities)
    # print(cities_without_city)
    # print(city)
    cities_without_city.remove(city)
    model = LpProblem(name="Airports-DEA", sense=LpMinimize)
    theta = LpVariable("theta", 0)
    lambdas = LpVariable.dicts("lambda", cities, 0)
    model += theta
    for inp in input.columns:
        model += lpSum([input.loc[city][inp] * lambdas[city] for city in cities_without_city]) <= input.loc[city][inp] * theta
    for out in output.columns:
        model += lpSum([output.loc[city][out] * lambdas[city] for city in cities_without_city]) >= output.loc[city][out]
    model.solve()
    return value(theta)

def calculate_superefficiency(input, output, cities):
    results_dict = dict()
    for city in cities:
        results_dict[city] = get_superefficiency(input, output, city)
    return results_dict

def get_optimal_effciency(input, output, city):
    cities = input.index.values.tolist()
    model = LpProblem(name="Airports-DEA", sense=LpMaximize)
    v = LpVariable.dicts("", input.columns, 0)
    u = LpVariable.dicts("", output.columns, 0)
    model += lpSum([output.loc[city][out] * u[out] for out in output.columns])
    model += lpSum([input.loc[city][inp] * v[inp] for inp in input.columns]) == 1
    for c in cities:
        model += lpSum([output.loc[c][out] * u[out] for out in output.columns]) <= lpSum([input.loc[c][inp] * v[inp] for inp in input.columns])
    model.solve()
    return (v, u)

def calculate_efficiency_vectors(input, output, cities):
    results = dict()
    for city in cities:
        result = get_optimal_effciency(input, output, city)
        temp = dict()
        for key, val in result[0].items():
            temp[key] = value(val)
        for key, val in result[1].items():
            temp[key] = value(val)
        results[city] = temp
    
    return results

def calculate_cross_efficiency(input, output, cities):
    results = calculate_efficiency_vectors(input, output, cities)
    cross_efficiency = np.zeros((len(cities), len(cities)))
    for i in range(len(cities)):
        for j in range(len(cities)):
            nominator = np.sum([results[cities[j]][out] * output.loc[cities[i]][out] for out in output.columns])
            denominator = np.sum([results[cities[j]][inp] * input.loc[cities[i]][inp] for inp in input.columns])
            cross_efficiency[i, j] = round(nominator / denominator, 3)

    cross_efficiency_results = dict()
    for i in range(len(cities)):
        cross_efficiency_results[cities[i]] = np.average(cross_efficiency[i, :])

    return (cross_efficiency, cross_efficiency_results)

     


input_path = './inputs.csv'
output_path = './outputs.csv'

inputs = pd.read_csv(input_path, delimiter=';', header=0, index_col=0)
outputs = pd.read_csv(output_path, delimiter=';', header=0, index_col=0)
print(inputs)
print(outputs)

results = calculate_efficiency(inputs, outputs)

effective_units = []
non_effective_units = []

for k, v in results.items():
    if v['efficiency'] == 1:
        effective_units.append(k)
    else:
        non_effective_units.append(k)

# calculate hcu
hcu = calculate_hcu(inputs, outputs, non_effective_units, results)


corrections = calculate_corrections(inputs, outputs, non_effective_units, hcu)

cities = inputs.index.values.tolist()
superefficiency_results = calculate_superefficiency(inputs, outputs, cities)

crossefficiency, crossefficiency_results = calculate_cross_efficiency(inputs, outputs, cities)


print('-------------------------------')
print('Efektywność')
for k, v in results.items():
    print(f"{k}: {v['efficiency']}")

print('------------------------')
print('Jednostki efektywne')
print(' '.join(effective_units))

print('------------------------')
print('Jednostki nieefektywne')
print(' '.join(non_effective_units))

print('------------------------')
print('Hipotetyczne jednostki efektywne')
for k, v in hcu.items():
    print(k)
    for k1, v1 in v.items():
        print(f'{k1}: {v1}')


print('------------------------')
print('Poprawki')
for k, v in corrections.items():
    print(k)
    for k1 in v:
        print(f'{k1}: {v[k1]}')

print('------------------------')
print('Superefektywność')
for city in superefficiency_results:
    print(f'{city}: {superefficiency_results[city]}')

print('------------------------')
print('Efektywność krzyżowa')
print(cities)
for i in range(len(cities)):
    print(cities[i] + "  " + str(crossefficiency[i, :]))






