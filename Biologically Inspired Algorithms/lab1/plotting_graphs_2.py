import matplotlib.pyplot as plt
import json
import numpy as np
from collections import defaultdict

vars = dict()
mut_list = ['0', '005', '010', '020', '030', '040', '050']
colors = ['orange', 'pink', 'magenta', 'purple', 'violet', 'cyan', 'blue']
max_list = defaultdict(lambda: [])
plt.figure(figsize=(10, 6))
for col_ind, mut in enumerate(mut_list):
    mut_val = float('0.' + mut[1:])
    for i in range(1, 11):
        with open(f'HoF-f9-{mut}-{str(i)}.gen') as f:
            contents = f.read().strip()
            contents = contents.replace(':@Serialized', '')
            vars = dict(line.split(':') for line in contents.splitlines())
            vars['nevals'] = json.loads(vars['nevals'])
            vars['max'] = json.loads(vars['max'])
            max_list[mut].append(vars['max'])
            vars['time'] = json.loads(vars['time'])
            vars['gen'] = np.array(json.loads(vars['gen']))
        for j in range(1, len(vars['nevals'])):
            vars['nevals'][j] += vars['nevals'][j - 1]
    
    max_list[mut] = np.array(max_list[mut])
    mean = max_list[mut].mean(axis=0)
    std = max_list[mut].std(axis=0) / 3
    print(max_list[mut])
    print(vars['gen'].shape, mean.shape)
    plt.plot(vars['gen'], mean, color=colors[col_ind], label=f'mutation = {mut_val:.{2}f}' if i % 10 == 0 else '', linewidth=0.9)
    plt.fill_between(vars['gen'], mean - std, mean + std, color = colors[col_ind], alpha=0.1)


plt.xlabel('Generacja (pokolenie)')
plt.ylabel('Max Fitness')
plt.legend()
plt.grid()

print(max_list)

plt.savefig('task_4_2.png')

print(sum(vars['nevals']))