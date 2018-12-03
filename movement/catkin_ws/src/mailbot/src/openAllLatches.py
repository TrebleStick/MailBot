#!/usr/bin/env python3
import sys
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
Linux_port = '/dev/ttyACM0'
Windows_port = 'COM3'

if len(sys.argv) > 1 :
    pins = (sys.argv[1:])  # ignore first argument (script name)
else :
    pins = ['1','2','3','4','5','6','7']
     # These are actually the latch numbers, the arduino code +1 to each of these to get the correct pins
print('Opening the following latches:')
print(pins)
channel = Serial(Linux_port, baudrate = 9600, timeout = 2)

# channel.open()
if channel.is_open :
    print('Serial channel open')
else :
    print('Serial Channel not opened check port setting')

time.sleep(2)


#-------CALIBRATE--------#
print('writing comms init to channel')
channel.write('9'.encode('utf-8'))
time.sleep(1)

# data = channel.readline()
# if (data[0]-48) == 9 :
#     print('woop')

#------------DEMO-ALL-LATCHES--------------#
for i in pins :
    channel.write(i.encode('utf-8'))
    time.sleep(2)
    print('Latch: ',i,', wrote: ', i.encode('utf-8'))
    # print(channel.readline()[0] - 48)

print('Latch demo complete')
#-----------SINGLE-LATCH-SCRIPT-----------#
# pin = unpack(data)
# channel.write(pin.encode('utf-8'))
# print(pin)
#---------------CLOSE---------------------#
time.sleep(1)
channel.close()
if channel.is_open :
    print('Channel close unsuccessful')
else :
    print('Channel closed')
