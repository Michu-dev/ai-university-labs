import numpy as np
import matplotlib.pyplot as plt


def barycentric_coordinates(p, p1, p2, p3):
    # Compute vectors
    v0 = p2 - p1
    v1 = p3 - p1
    v2 = p - p1

    # Compute dot products
    dot00 = np.dot(v0, v0)
    dot01 = np.dot(v0, v1)
    dot02 = np.dot(v0, v2)
    dot11 = np.dot(v1, v1)
    dot12 = np.dot(v1, v2)

    # Compute barycentric coordinates
    invDenom = 1 / (dot00 * dot11 - dot01 * dot01)
    u = (dot11 * dot02 - dot01 * dot12) * invDenom
    v = (dot00 * dot12 - dot01 * dot02) * invDenom
    w = 1 - u - v

    return (u, v, w)


def color_interpolation(p, p1, p2, p3, c1, c2, c3):
    u, v, w = barycentric_coordinates(p, p1, p2, p3)
    color = u * np.array(c1) + v * np.array(c2) + w * np.array(c3)
    return tuple(color.astype(int))


# Test the function
p1 = np.array([0, 0])
p2 = np.array([1, 0])
p3 = np.array([0.5, np.sqrt(3) / 2])

c1 = (255, 0, 0)  # Red
c2 = (0, 255, 0)  # Green
c3 = (0, 0, 255)  # Blue
p = np.array([0.3, 0.2])

print(color_interpolation(p, p1, p2, p3, c1, c2, c3))

x = np.array([0, 1, 1 / 2, 0])
y = np.array([0, 0, np.sqrt(3) / 2, 0])
plt.plot(x, y)
x, y = 0.3, 0.2
plt.plot(x, y, "ro", color=color_interpolation(p, p1, p2, p3, c1, c2, c3) / 255)
plt.show()
