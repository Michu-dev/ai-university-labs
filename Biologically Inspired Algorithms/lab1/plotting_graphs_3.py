import matplotlib.pyplot as plt
import json
import numpy as np
from collections import defaultdict

vars = dict()
mut_list = ['0', '005', '010', '020', '030', '040', '050']
colors = ['orange', 'pink', 'magenta', 'purple', 'violet', 'cyan', 'blue']
time_data, data = [], []
labels = ['0.0', '0.05', '0.1', '0.2', '0.3', '0.4', '0.5']
fig, (ax1, ax2) = plt.subplots(nrows=1, ncols=2, figsize=(15, 12))
for col_ind, mut in enumerate(mut_list):
    mut_val = float('0.' + mut[1:])
    time_data.append([])
    data.append([])
    for i in range(1, 11):
        with open(f'HoF-f9-{mut}-{str(i)}.gen') as f:
            contents = f.read().strip()
            contents = contents.replace(':@Serialized', '')
            vars = dict(line.split(':') for line in contents.splitlines())
            vars['vertpos'] = json.loads(vars['vertpos'])
            vars['nevals'] = json.loads(vars['nevals'])
            vars['max'] = json.loads(vars['max'])
            data[col_ind].append(vars['vertpos'])
            vars['time'] = json.loads(vars['time'])
            time_data[col_ind].append(vars['time'])
            vars['gen'] = np.array(json.loads(vars['gen']))
        for j in range(1, len(vars['nevals'])):
            vars['nevals'][j] += vars['nevals'][j - 1]
    data[col_ind] = np.array(data[col_ind])
    time_data[col_ind] = np.array(time_data[col_ind])
    
    
# time_data, data = np.array(time_data), np.array(data)
print(data)
bplot1 = ax1.boxplot(data, vert=True, patch_artist=True, labels=labels)
bplot2 = ax2.boxplot(time_data, vert=True, patch_artist=True, labels=labels)
for bplot in (bplot1, bplot2):
    for patch, color in zip(bplot['boxes'], colors):
        patch.set_facecolor(color)

for ax in [ax1, ax2]:
    ax.yaxis.grid(True)
    ax.set_xlabel('mutation')
ax1.set_ylabel('fitness')
ax2.set_ylabel('time (seconds)')


plt.savefig('task_4_3.png')

print(sum(vars['nevals']))