#!/usr/bin/env python

# To add to launch file
# <node pkg="mailbot" type="tspSolver.py" name="tspSolver" />
from tsp_solver.greedy import solve_tsp
import pandas as pd
import numpy as np
# from tsp_solver.util import path_cost
import rospy
from std_msgs.msg import String

def callback(data):
    # Read node distances from file
    # File location is in Home
    pub = rospy.Publisher('solvedPath', String, queue_size=10)
    d = pd.read_csv("weights.csv", header=None)
    d  = d.values

    # get nodes to traverse
    nodes = data.data.split()
    nodes = [int(i) for i in nodes]
    #print(nodes)
    # create a smaller distance matrix to solve from nodes to traverse
    row_idx = np.array(nodes)
    col_idx = np.array(nodes)
    dsub = d[row_idx[:, None], col_idx]

    # solve the tsp for the nodes desired
    relpath = solve_tsp(dsub)

    # stating a start and finish point solves our tour issue
    # relpath = solve_tsp(dsub, endpoints = (0,1)))

    # convert the solution path back to index of full matrix
    path = [0]*len(nodes)
    for j in range(0, len(relpath)):
        path[j] = nodes[relpath[j]]

    # print the path cost
    # print(path_cost(d, path))
    # print(path)
    pub.publish(str(path))
#    rospy.loginfo(rospy.get_caller_id() + "i heard %s", data.data, ", solution is ", ''.join(str(e) for e in path))
#    rospy.loginfo(rospy.get_caller_id() + "i heard %s", data.data)

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
