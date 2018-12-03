from tsp_solver.greedy import solve_tsp
import pandas as pd
import numpy as np
from tsp_solver.util import path_cost
import rospy
from std_msgs.msg import String

def callback(data):
    # Read node distances from file
    D = pd.read_csv("weights.csv", header=None)
    D  = D.values

    # Get nodes to traverse
    nodes = data.data.split()

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

    rospy.loginfo(rospy.get_caller_id() + "I heard %s", data.data, ", solution is ", path)


def listener():

    # In ROS, nodes are uniquely named. If two nodes with the same
    # node are launched, the previous one is kicked off. The
    # anonymous=True flag means that rospy will choose a unique
    # name for our 'listener' node so that multiple listeners can
    # run simultaneously.
    rospy.init_node('tspSolver', anonymous=True)

    rospy.Subscriber("deliveryLocations", String, callback)

    # spin() simply keeps python from exiting until this node is stopped
    rospy.spin()

if __name__ == '__main__':
    listener()











# To update the values in the decision matrix you must remember it's a lower left triangle
# Therefore the smaller value node comes first
# Dtest = D[2, 3]
# NOT THIS
# Dtest = D[3, 2]
# print(Dtest)

#Save the Distance matrix to file
# df = pd.DataFrame(D)
# df.to_csv("weights.csv", header=None, index=None)
