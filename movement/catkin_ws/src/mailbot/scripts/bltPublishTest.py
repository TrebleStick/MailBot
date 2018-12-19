#!/usr/bin/env python
import sys
import time
from ast import literal_eval as make_tuple
import rospy
from std_msgs.msg import String

def at_location(data, talker):
    talker.tmp = str(data.data)

open_locker_topic = 'openLocker'
delivery_locations_topic = 'deliveryLocations'
delivery_complete_topic = 'deliveryComplete'

at_location_topic = ('atLocation', at_location)

class Talker(object):
    def __init__(self, publishers, subscribers):
        self.publishers = {}
        for topic in publishers:
            self.publishers[topic] = rospy.Publisher(topic, String)

        self.subscribers = {}
        for topic, function in subscribers:
            self.subscribers[topic] = rospy.Subscriber(topic, String, function, callback_args=self)
        rospy.init_node('bltTalker', anonymous=True)

    def publish(self, pub_str, topic):
        try:
            print('publishing', pub_str, 'to topic', topic,
                  'using publisher', self.publishers[topic])
            self.publishers[topic].publish(pub_str)
        except rospy.ROSInterruptException:
            pass
    def listen(self):
        self.tmp = None
        while not self.tmp:
            time.sleep(0.1)
        else:
            return self.tmp

publishers = [open_locker_topic, delivery_locations_topic, delivery_complete_topic]
subscribers = [at_location_topic]

talker = Talker(publishers, subscribers)


def getLockerToOpen(btSocket):
    print("getLockerToOpen()")
    # btSocket.send("1409")
    appMsg = 'RLO'
    while 1:
        # appMsg = btSocket.recv(1024)
        if not appMsg:
            continue
        elif str(appMsg) == "RLO":
            print(str(appMsg))
            # should write RLO to terminal
            # btSocket.send("num")
            appMsg = '1'
            continue
        else:
            # should print a number (the locker to be opened)
            print(appMsg)
            # should publish the locker number to open_locker_topic
            talker.publish(appMsg, open_locker_topic)
            break
        time.sleep(0.01)

def getLocationList(btSocket):
    print("getLocationList()")
    # btSocket.send("3020")
    appMsg = 'SLL'
    while 1:
        # appMsg = btSocket.recv(1024)
        if not appMsg:
            continue
        elif str(appMsg) == "SLL":
            print(str(appMsg))
            # should write SLL to terminal
            # btSocket.send("list")
            appMsg = '1 2 3'
            continue
        else:
            # should print a space delimited list of locations
            print(appMsg)
            # should publish a space delimited list of locations to delivery_locations_topic
            talker.publish(appMsg, delivery_locations_topic)
            return appMsg
        time.sleep(0.01)

def notifyArrival(btSocket):
    print("notifyArrival()")
    # btSocket.send("8062")
    appMsg = 'ref'
    while 1:
        # appMsg = btSocket.recv(1024)
        if not appMsg:
            continue
        elif str(appMsg) == "6208":
            print(str(appMsg))
            # should print 1409
            # btSocket.send("adm")
            continue
        elif str(appMsg) == "ref":
            print(str(appMsg))
            # should print ref
            location = talker.listen()
            # btSocket.send("l,"+location)
            break
        time.sleep(0.01)

def getDeliveryComplete(btSocket):
    print("getDeliveryComplete()")
    # btSocket.send("5060")
    appMsg = 'yes'
    while 1:
        # appMsg = btSocket.recv(1024)
        if not appMsg:
            continue
        elif str(appMsg) == "GTN":
            print(str(appMsg))
            # should write SLL to terminal
            # btSocket.send("done")
            continue
        else:
            print(str(appMsg))
            # should print a space delimited list of locations
            talker.publish(appMsg, delivery_complete_topic)
            return appMsg
        time.sleep(0.01)

start = False
waitingToArrive = False
progression = 0
sock=0
while 1:

    try:
        # should always come here once the app and computer have connected
        print("starting")
        if not waitingToArrive:
            if progression == 0:
                getLockerToOpen(sock)
                progression += 1
                continue
            elif progression == 1:
                locationList = getLocationList(sock)
                print(str(locationList))
                # waitingToArrive means the MailBot is now moving
                # it is waiting to arrive at a mail item destination
                waitingToArrive = True
                progression += 1
                continue
            else:
                complete = getDeliveryComplete(sock)
                print(str(complete))
                progression = 0
        else:
            # gets here when it is waiting to arrive at a location
            arrivalCount = 0
            # should wait for some message from ROS?
            notifyArrival(sock)
            waitingToArrive = False

    except IOError:
        pass
    time.sleep(0.01)
