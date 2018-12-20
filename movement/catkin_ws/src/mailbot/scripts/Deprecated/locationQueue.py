#!/usr/bin/env python
import rospy
import ast
from std_msgs.msg import String

def callback2(data2):
    global globNodes
    global counter

    if(int(data2.data) == globNodes[counter]):
        counter += 1
        pub = rospy.Publisher('Location', String)
        if(counter == len(globNodes)):
            counter = 0
            pub.publish("Route completed")
        else:
            pub.publish(str(globNodes[counter]))
    else:
        print("Not at current goal location")

def callback(data):

    # nodes = data.data.split()
    nodes = ast.literal_eval(data.data)
    print(nodes)
    # nodes = [int(i) for i in nodes]
    global globNodes
    globNodes = nodes


    global counter
    counter = 0
    pub = rospy.Publisher('Location', String)
    pub.publish(str(globNodes[0]))
    # print the path cost
    # print(path_cost(d, path))
    # print(path)


def listener():

    # In ROS, nodes are uniquely named. If two nodes with the same
    # node are launched, the previous one is kicked off. The
    # anonymous=True flag means that rospy will choose a unique
    # name for our 'listener' node so that multiple listeners can
    # run simultaneously.
    rospy.init_node('locationQueue', anonymous=True)


    rospy.Subscriber("solvedPath", String, callback)
    rospy.Subscriber("atLocation", String, callback2)

    # spin() simply keeps python from exiting until this node is stopped
    rospy.spin()

if __name__ == '__main__':
    counter = 0
    globNodes = []
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
