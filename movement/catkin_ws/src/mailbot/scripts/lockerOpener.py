#!/usr/bin/env python
import rospy
from std_msgs.msg import String

from serial import Serial
import time
#----------------ROS-NOT TESTED-------------#
# import roslib as ros
# ros.load_manifest('node_example')
# #check the sourc file provided for this
# #http://wiki.ros.org/ROSNodeTutorialPython
# import rospy
#
# def callback(data):
#     rospy.loginfo(rospy.get_name())
#
# def listener():
#     #create a uniqe name for the listener so multiple listeners can be used
#     rospy.init_node('listener', anonymus=True)
#     # subscribe to info "chatter" may need re-naming
#     rospy.Subscriber("chatter", String, callback)
#     # stop the python script from terminating after callback
#     rospy.spin()
#---------------FUNC-----------------#
# def unpack(data):
    #find format of data taken from listener channel
    #convert this to int TODO
#change ports for system
def callback(data):
    Linux_port = '/dev/ttyACM0'
    Windows_port = 'COM3'

    latchToOpen = int(data.data)
    # latchToOpen = data.data

    pins = ['1','2','3','4','5','6','7']

    channel = Serial(Linux_port, baudrate = 9600, timeout = 2)

    # channel.open()
    if channel.is_open :
        print('Serial channel open')
    else :
        print('Serial Channel not opened check port setting')

    time.sleep(1)

    #-----------SINGLE-LATCH-SCRIPT-----------#
    channel.write(pins[latchToOpen].encode('utf-8'))
    # channel.write('6'.encode('utf-8'))

    print(pins[latchToOpen].encode('utf-8'))
    print(latchToOpen)
    time.sleep(2)
    #---------------CLOSE---------------------#

    channel.close()
    if channel.is_open :
        print('Channel close unsuccessful')
    else :
        print('Channel closed')


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
    listener()
