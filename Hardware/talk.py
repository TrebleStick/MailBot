import serial
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

#------------------------------------#
#change ports for system
Linux_port = '/dev/ttyUSB0'
Windows_port = 'COM3'

pins = ['1','2','3','4','5','6','7']

channel = serial.Serial(Windows_port, baudrate = 9600, timeout = 2)

# channel.open()
if channel.is_open :
    print('Serial channel open')
else :
    print('Serial Channel not opened check port setting')

time.sleep(2)

print('writing comms init to channel')
channel.write('9'.encode('utf-8'))

data = channel.readline()
if (data[0]-48) == 9 :
    print('woop')



for i in pins :
    channel.write(i.encode('utf-8'))
    time.sleep(0.5)
    print(channel.readline()[0] - 48)
# channel.write('3'.encode('utf-8'))

# data = channel.readline()
# print(data[0]-48)


print('Latch demo complete')


time.sleep(1)

channel.close()

if channel.is_open :
    print('Channel close unsuccessful')
else :
    print('Channel closed')
