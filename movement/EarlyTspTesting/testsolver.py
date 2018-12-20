from tsp_solver.greedy import solve_tsp
import pandas as pd
import numpy as np
from tsp_solver.util import path_cost

# Read node distances from file
D = pd.read_csv("weights.csv", header=None)
D  = D.values

# Get nodes to traverse
nodes = [0,1,3]


# Create a smaller distance matrix to solve from nodes to traverse
row_idx = np.array(nodes)
col_idx = np.array(nodes)
Dsub = D[row_idx[:, None], col_idx]


# Solve the tsp for the nodes desired
relPath = solve_tsp(Dsub)

# Stating a start and finish point solves our tour issue
# relPath = solve_tsp(Dsub, endpoints = (0,1)))

# Convert the solution path back to index of full matrix
path = [0]*len(nodes)
for j in range(0, len(relPath)):
    path[j] = nodes[relPath[j]]

# Print the path cost
print(path_cost(D, path))
print(path)


# To update the values in the decision matrix you must remember it's a lower left triangle
# Therefore the smaller value node comes first
# Dtest = D[2, 3]
# NOT THIS
# Dtest = D[3, 2]
# print(Dtest)

#Save the Distance matrix to file
# df = pd.DataFrame(D)
# df.to_csv("weights.csv", header=None, index=None)
