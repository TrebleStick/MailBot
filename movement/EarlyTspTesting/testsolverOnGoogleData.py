from tsp_solver.greedy import solve_tsp
import pandas as pd
import numpy as np
from tsp_solver.util import path_cost

# Read node distances from file
# D = pd.read_csv("weights.csv", header=None)
# D  = D.values
city_names = ["New York", "Los Angeles", "Chicago", "Minneapolis", "Denver", "Dallas", "Seattle",
                "Boston", "San Francisco", "St. Louis", "Houston", "Phoenix", "Salt Lake City"]

D = np.array([
    [   0, 2451,  713, 1018, 1631, 1374, 2408,  213, 2571,  875, 1420, 2145, 1972], # New York
    [2451,    0, 1745, 1524,  831, 1240,  959, 2596,  403, 1589, 1374,  357,  579], # Los Angeles
    [ 713, 1745,    0,  355,  920,  803, 1737,  851, 1858,  262,  940, 1453, 1260], # Chicago
    [1018, 1524,  355,    0,  700,  862, 1395, 1123, 1584,  466, 1056, 1280,  987], # Minneapolis
    [1631,  831,  920,  700,    0,  663, 1021, 1769,  949,  796,  879,  586,  371], # Denver
    [1374, 1240,  803,  862,  663,    0, 1681, 1551, 1765,  547,  225,  887,  999], # Dallas
    [2408,  959, 1737, 1395, 1021, 1681,    0, 2493,  678, 1724, 1891, 1114,  701], # Seattle
    [ 213, 2596,  851, 1123, 1769, 1551, 2493,    0, 2699, 1038, 1605, 2300, 2099], # Boston
    [2571,  403, 1858, 1584,  949, 1765,  678, 2699,    0, 1744, 1645,  653,  600], # San Francisco
    [ 875, 1589,  262,  466,  796,  547, 1724, 1038, 1744,    0,  679, 1272, 1162], # St. Louis
    [1420, 1374,  940, 1056,  879,  225, 1891, 1605, 1645,  679,    0, 1017, 1200], # Houston
    [2145,  357, 1453, 1280,  586,  887, 1114, 2300,  653, 1272, 1017,    0,  504], # Phoenix
    [1972,  579, 1260,  987,  371,  999,  701, 2099,  600, 1162,  1200,  504,   0]]) # Salt Lake City

# Get nodes to traverse
nodes = list(range(0,len(city_names)))
print(nodes)

# Create a smaller distance matrix to solve from nodes to traverse
row_idx = np.array(nodes)
col_idx = np.array(nodes)
Dsub = D[row_idx[:, None], col_idx]


# Solve the tsp for the nodes desired
relPath = solve_tsp(Dsub, endpoints =(0,9))

# Convert the solution path back to index of full matrix
path = [0]*len(nodes)
for j in range(0, len(relPath)):
    path[j] = nodes[relPath[j]]

# Print the path cost
print(path_cost(D, path))
print(path)

pathNames = [0]*len(path)
for j in range(0, len(path)):
    pathNames[j] = city_names[path[j]]

print(pathNames)


# To update the values in the decision matrix you must remember it's a lower left triangle
# Therefore the smaller value node comes first
# Dtest = D[2, 3]
# NOT THIS
# Dtest = D[3, 2]
# print(Dtest)

#Save the Distance matrix to file
# df = pd.DataFrame(D)
# df.to_csv("weights.csv", header=None, index=None)
