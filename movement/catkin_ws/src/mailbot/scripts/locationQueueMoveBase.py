#!/usr/bin/env python
import rospy
import ast
from std_msgs.msg import String
import actionlib
from move_base_msgs.msg import MoveBaseAction, MoveBaseGoal
import rooms as rm

goalList = [(22.662, 38.485, 0.9788, 0.2046), (17.18, 26.793, 0.9728, 0.2314), (15.436, -0.1924, 21.067, 0.9813)]
currentLoc = -1

def callback2(data2):
    # global anyLoc
    # anyLoc = True
    global globNodes
    global counter
    global currentLoc
    # if(int(data2.data) == globNodes[counter]):
    counter += 1
    # pub = rospy.Publisher('Location', String)
    if(counter == len(globNodes)):
        counter = 0
        # pub.publish("Route completed")
    else:
        # pub.publish(str(globNodes[counter]))
        client = actionlib.SimpleActionClient('move_base',MoveBaseAction)
        client.wait_for_server()
        #Sketch
        goalindex = globNodes[counter]
        goalX, goalY, goalZ, goalW = goalList[goalindex]
        goal = MoveBaseGoal()
        goal.target_pose.header.frame_id = "map"
        goal.target_pose.header.stamp = rospy.Time.now()
        goal.target_pose.pose.position.x = goalX
        goal.target_pose.pose.position.y = goalY
        goal.target_pose.pose.orientation.z = goalZ
        goal.target_pose.pose.orientation.w = goalW


        client.send_goal(goal)
        wait = client.wait_for_result()
        if not wait:
            rospy.logerr("Action server not available!")
            rospy.signal_shutdown("Action server not available!")
        else:
            currentLoc = globNodes[counter]
            print("at ", currentLoc)
            return client.get_result()
            # return globNodes[counter]
    # else:
    #     print("Not at current goal location")

def callback(data):
    # global anyLoc
    # anyLoc = True
    # nodes = data.data.split()
    nodes = ast.literal_eval(data.data)
    print(nodes)
    # nodes = [int(i) for i in nodes]
    global globNodes
    globNodes = nodes


    global counter
    counter = 0
    # pub = rospy.Publisher('Location', String)
    # pub.publish(str(globNodes[0]))
    client = actionlib.SimpleActionClient('move_base',MoveBaseAction)
    client.wait_for_server()
    goalindex = globNodes[counter]
    goalX, goalY, goalZ, goalW = goalList[goalindex]

    goal = MoveBaseGoal()
    goal.target_pose.header.frame_id = "map"
    goal.target_pose.header.stamp = rospy.Time.now()
    goal.target_pose.pose.position.x = goalX
    goal.target_pose.pose.position.y = goalY
    goal.target_pose.pose.orientation.z = goalZ
    goal.target_pose.pose.orientation.w = goalW


    client.send_goal(goal)
    wait = client.wait_for_result()
    if not wait:
        rospy.logerr("Action server not available!")
        rospy.signal_shutdown("Action server not available!")
    else:
        currentLoc = globNodes[counter]
        print("at ", currentLoc)
        return client.get_result()
        # return globNodes[counter]
    # print the path cost
    # print(path_cost(d, path))
    # print(path)


def listener():
    # anyLoc = False
    # In ROS, nodes are uniquely named. If two nodes with the same
    # node are launched, the previous one is kicked off. The
    # anonymous=True flag means that rospy will choose a unique
    # name for our 'listener' node so that multiple listeners can
    # run simultaneously.
    pub = rospy.Publisher('atLocation', String)
    result = rospy.Subscriber("solvedPath", String, callback)
    result2 = rospy.Subscriber("deliveryComplete", String, callback2)

    rospy.init_node('locationQueue', anonymous=True)



    if (True):
        # post at location
        print("yo")
        print(result)
        test_str = rm.indexToRoom(int(currentLoc))
        test_str = str(test_str)
        rospy.loginfo(test_str)
        pub.publish(test_str)
    if (True):
        # post at location
        print("yo2")

        print(result2)
        pub = rospy.Publisher('atLocation', String)
        test_str = rm.indexToRoom(int(currentLoc))
        test_str = str(test_str)
        rospy.loginfo(test_str)
        pub.publish(test_str)
        # print(result2)
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
