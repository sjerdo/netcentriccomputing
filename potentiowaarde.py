"""
Author: Sjoerd Wenker
        Shahrukh Zaidi

Course: Netcentric Computing
Date: Jan 9, 2016

This file contains code to generate a plot with estimated potentio value
for our measurements.
"""

import numpy as np
import matplotlib.pyplot as plt

# metingen
x = np.arange(0, 10.5, 0.5)
y = [0.0000, 0.001, 0.0035, 0.0075, 0.015, 0.0295, 0.0455, 0.0705, 0.093,
     0.120, 0.1435, 0.168, 0.1935, 0.2235, 0.260, 0.3105, 0.4335, 0.566,
     0.7025, 0.8135, 0.964]

yerr = [0, 0, 0.0005, 0.0005, 0.001, 0.0015, 0.0015, 0.0015, 0.002, 0.001,
        0.0025, 0.001, 0.0015, 0.0015, 0.001, 0.0015, 0.0015, 0.001, 0.0015,
        0.0015, 0.001]
ls = 'dotted'

fig = plt.figure()
ax = fig.add_subplot(1, 1, 1)

# error bars
plt.errorbar(x, y, yerr=yerr, ls=ls, color='red')

ax.set_xlim((-0.5, 10.5))
ax.set_title('Metingen potentiowaarde + schatting')


def potentio(x):
    if x < 5.5:
        return 0.00599767 * x * x - 0.000904196 * x - 0.00289697
    return 0.0158139 * np.exp(0.41411 * x)

x = np.arange(0, 10.001, 0.001)
y = [potentio(b) for b in x]

plt.plot(x, y)
plt.xlabel('Potentiowaarde')
plt.ylabel('Spanningsmeting')

plt.savefig('plot-with-estimate.png')
plt.show()
