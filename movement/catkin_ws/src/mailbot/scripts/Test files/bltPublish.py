#!/usr/bin/env python
import sys
import bluetooth
import time
from ast import literal_eval as make_tuple
import rospy
from std_msgs.msg import String

def talker(pub_str, topic):
    try:
        pub = rospy.Publisher(topic, String)
        rospy.init_node('bltPublish', anonymous=True)
        rospy.loginfo(pub_str)
        pub.publish(pub_str)
    except rospy.ROSInterruptException:
        pass


uuid = "00001105-0000-1000-8000-00805F9B34FB"
# tabAddress = "74:04:2b:e8:19:86"
tabAddress = "74:04:2B:D5:19:86"

service_matches = []

while not service_matches:
    service_matches = bluetooth.find_service( name = 'MYAPP', address = tabAddress )

print service_matches

if len(service_matches) == 0:
    print "couldn't find the FooBar service"
    sys.exit(0)

first_match = service_matches[0]
port = first_match["port"]
name = first_match["name"]
host = first_match["host"]

print "connecting to \"%s\" on %s" % (name, host)

sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
sock.connect((host, port))

def recieve_data():
    try:
        data = sock.recv(1024)
        if len(data) == 0: break
        print("received [%s]" % data)
        if data == '0507':
            sock.send('1409')
        else:
            str_list = str(data).split()
            if str_list[0] == 'RLO':
                pass
            elif str_list[0] == 'SLL':
                talker(str_list[1:], 'deliveryLocations')
                return False
            elif str_list[0] == 'GTN':
                pass
    except IOError:
        pass

    time.sleep(0.01)

    return True

def send_data():
    sock.send('1')
    return False


# make_tuple("(1,2,3,4,5)") # NOTE: There must be no spaces in between the numbers
# sock.send("hello!!") # sends hello to tablet
recieve_things = False
while 1:
    if recieve_things:
        recieve_things = recieve_data()
    else:
        recieve_things = send_data()


sock.close()
