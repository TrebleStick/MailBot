
import sys
import bluetooth
import
from ast import literal_eval as make_tuple

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

# make_tuple("(1,2,3,4,5)") # NOTE: There must be no spaces in between the numbers
while 1:
    sock.send("hello!!")
    try:
        data = sock.recv(1024)
        if len(data) == 0: break
        print("received [%s]" % data)
            # break
    except IOError:
        pass
    time.sleep(0.01)


sock.close()
