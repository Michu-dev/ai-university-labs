import matplotlib.pyplot as plt
import numpy as np
import math
import random

n_points = 10000
side_length = math.sqrt(3) / 2
x = np.array([0, 1, 1 / 2, 0])
y = np.array([0, 0, math.sqrt(3) / 2, 0])
plt.plot(x, y)
dens = 0.01
r, g, b = np.array([1, 0, 0]), np.array([0, 1, 0]), np.array([0, 0, 1])
for u in np.arange(0, 1 + dens, dens):

    for v in np.arange(0, 1 + dens - u, dens):
        w = max(1 - u - v, 0)

        div = u + v + w
        x, y = (
            u / div * np.array([0, 0])
            + v / div * np.array([1, 0])
            + w / div * np.array([1 / 2, math.sqrt(3) / 2])
        )
        col = u * r + v * g + w * b
        plt.plot(x, y, "ro", color=col)
plt.show()
