import matplotlib.pyplot as plt
import numpy as np
import math
import random

n_points = 1000
side_length = math.sqrt(3) / 2
x = np.array([0, 1, 1 / 2, 0])
y = np.array([0, 0, math.sqrt(3) / 2, 0])
plt.plot(x, y)
for i in range(n_points):

    u, v = (
        random.uniform(0, 1 / 2),
        random.uniform(0, 1 / 2),
    )
    w = 1 - u - v

    # u * [0, 0] = [0, 0]
    # v * [1, 0] = [v, 0]
    # w * [1/2, sqrt(3) / 2] = [w / 2, sqrt(3)w / 2]
    # [v + w/2, sqrt(3)w / 2]
    x, y = (
        side_length * np.array([0, 0])
        + side_length * np.array([1, 0])
        + side_length * np.array([1 / 2, math.sqrt(3) / 2])
    )
    plt.plot(x, y, "ro")
plt.show()
