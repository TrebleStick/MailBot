#!/usr/bin/env python3
import rospy
from std_msgs.msg import String
from subprocess import run
from serial import Serial
import time

def callback(data):
    run(["rosrun", "mailbot", "openAllLatches.py", str(data.data)])


def listener():

    # In ROS, nodes are uniquely named. If two nodes with the same
    # node are launched, the previous one is kicked off. The
    # anonymous=True flag means that rospy will choose a unique
    # name for our 'listener' node so that multiple listeners can
    # run simultaneously.
    rospy.init_node('lockerOpener', anonymous=True)


    rospy.Subscriber("atLocation", String, callback)

    # spin() simply keeps python from exiting until this node is stopped
    rospy.spin()

if __name__ == '__main__':
    print('Waiting on atLocation Topic')
    listener()
