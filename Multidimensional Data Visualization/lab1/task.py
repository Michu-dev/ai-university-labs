import matplotlib.pyplot as plt
import numpy as np
import math

u, v, w = 1 / 3, 1 / 3, 1 / 3

x = np.array([0, 1, 1 / 2, 0])
y = np.array([0, 0, math.sqrt(3) / 2, 0])
plt.plot(x, y)
# u * [0, 0] = [0, 0]
# v * [1, 0] = [v, 0]
# w * [1/2, sqrt(3) / 2] = [w / 2, sqrt(3)w / 2]
# [v + w/2, sqrt(3)w / 2]
x, y = (
    u * np.array([0, 0])
    + v * np.array([1, 0])
    + w * np.array([1 / 2, math.sqrt(3) / 2])
)
plt.plot(x, y, "ro")
plt.show()
