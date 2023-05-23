from pulp import *
import pandas as pd
from collections import defaultdict


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


input_path = './inputs.csv'
output_path = './outputs.csv'

inputs = pd.read_csv(input_path, delimiter=';', header=0, index_col=0)
outputs = pd.read_csv(output_path, delimiter=';', header=0, index_col=0)
print(inputs)
print(outputs)

results = calculate_efficiency(inputs, outputs)
print(results)

effective_units = []
non_effective_units = []

for k, v in results.items():
    if v['efficiency'] == 1:
        effective_units.append(k)
    else:
        non_effective_units.append(k)

# calculate hcu
hcu = calculate_hcu(inputs, outputs, non_effective_units, results)




