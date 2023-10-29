import matplotlib.pyplot as plt
import json

vars = dict()
mut_list = ['0', '005', '010', '020', '030', '040', '050']
colors = ['orange', 'pink', 'magenta', 'purple', 'violet', 'cyan', 'blue']
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
            vars['time'] = json.loads(vars['time'])
            vars['gen'] = json.loads(vars['gen'])
        for j in range(1, len(vars['nevals'])):
            vars['nevals'][j] += vars['nevals'][j - 1]
        
        plt.plot(vars['nevals'], vars['max'], color=colors[col_ind], label=f'mutation = {mut_val:.{2}f}' if i % 10 == 0 else '', linewidth=0.9)


plt.xlabel('Liczba ocenionych osobników')
plt.ylabel('Max Fitness')
plt.legend()
plt.grid()

plt.savefig('task_4_1.png')

print(sum(vars['nevals']))


