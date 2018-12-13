#!/usr/bin/env python
import sys
import bluetooth
import time
from ast import literal_eval as make_tuple
import rospy
from std_msgs.msg import String
from serial import Serial

def openAllLatches(pins=None):
    Linux_port = '/dev/ttyACM0'
    Windows_port = 'COM3'

    if not pins:
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
    print "getLockerToOpen()"
    btSocket.send("1409")
    while 1:
        appMsg = btSocket.recv(1024)
        if not appMsg:
            continue
        elif str(appMsg) == "RLO":
            print str(appMsg)
            # should write RLO to terminal
            btSocket.send("num")
            continue
        else:
            print appMsg
            # should print a number (the locker to be opened)
            talker.publish(appMsg, open_locker_topic)
            openAllLatches([str(appMsg)])
            break
        time.sleep(0.01)

def getLocationList(btSocket):
    print "getLocationList()"
    btSocket.send("3020")
    while 1:
        appMsg = btSocket.recv(1024)
        if not appMsg:
            continue
        elif str(appMsg) == "SLL":
            print str(appMsg)
            # should write SLL to terminal
            btSocket.send("list")
            continue
        else:
            print appMsg
            # should print a space delimited list of locations
            talker.publish(appMsg, delivery_locations_topic)
            return appMsg
        time.sleep(0.01)

def notifyArrival(btSocket, location):
    print"notifyArrival()"
    btSocket.send("8062")
    while 1:
        appMsg = btSocket.recv(1024)
        if not appMsg:
            continue
        elif str(appMsg) == "6208":
            print str(appMsg)
            # should print 1409
            btSocket.send("adm")
            continue
        elif str(appMsg) == "ref":
            print str(appMsg)
            # should print ref
            btSocket.send("l,"+location)
            # should really be sending l,destinationNumber
            break
        time.sleep(0.01)

def getDeliveryComplete(btSocket):
    print "getDeliveryComplete()"
    btSocket.send("5060")
    while 1:
        appMsg = btSocket.recv(1024)
        if not appMsg:
            continue
        elif str(appMsg) == "GTN":
            print str(appMsg)
            # should write SLL to terminal
            btSocket.send("done")
            continue
        else:
            print str(appMsg)
            # should print a space delimited list of locations
            talker.publish(appMsg, delivery_complete_topic)
            # return appMsg
            break
        time.sleep(0.01)

uuid = "00001105-0000-1000-8000-00805F9B34FB"
# tabAddress = "74:04:2b:e8:19:86"
tabAddress = "74:04:2B:D5:19:86"

service_matches = []

while not service_matches:
    service_matches = bluetooth.find_service( name = 'MYAPP', address = tabAddress )

print service_matches

# Is there a timeout that would let the script get to this if statement though
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

# make_tuple("(1,2,3,4,5)") # NOTE: There must be no spaces in between the numbers

start = False
waitingToArrive = False
test = 0
# sock.send("hello!!")
while 1:

    if waitingToArrive:
        # gets here when it is waiting to arrive at a location
        print "Waiting to arrive"
        location = talker.listen()
        print('Arrived  ')
        notifyArrival(sock, location)
        waitingToArrive = False

    try:
        print "getting data"
        data = sock.recv(1024)
        # sock.recv blocks lmao
        print("received [%s]" % data)
            # try:
            #     talker(str(data))
            # except rospy.ROSInterruptException:
            #     pass
            #     # break

        if not start:
            # Should only come here if the app and computer have not connected
            print "not start"
            print str(data)
            if str(data) == "hey":
                start = True
                print "started"
                continue

        # should always come here once the app and computer have connected
        if not waitingToArrive:
            if str(data) == "0507":
                getLockerToOpen(sock)
            elif str(data) == "0203":
                locationList = getLocationList(sock)
                print str(locationList)
                # waitingToArrive means the MailBot is now moving
                # it is waiting to arrive at a mail item destination
                waitingToArrive = True
                print "waitingToArrive = true"
            elif str(data) == "0605":
                getDeliveryComplete(sock)
                waitingToArrive = True
            continue

    except IOError:
        print "Error"
        pass
    time.sleep(0.01)
sock.close()
