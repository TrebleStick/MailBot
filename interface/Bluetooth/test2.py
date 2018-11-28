import bluetooth
import time

name = "bt_server"
target_name = "test"

uuid = "00001105-0000-1000-8000-00805F9B34FB"
    # ``xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"

def runServer():
    serverSocket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    port = bluetooth.PORT_ANY
    serverSocket.bind(("", port))
    print("Listening for connections on port: ", port)

    serverSocket.listen(1)
    port= serverSocket.getsockname()[1]
    print("debug_0")

    bluetooth.advertise_service(serverSocket,   "SampleServer", service_id = uuid, service_classes = [ uuid, bluetooth.SERIAL_PORT_CLASS ], profiles = [ bluetooth.SERIAL_PORT_PROFILE ])

    try:
        print("debug_1")
        (clientSocket, address) = serverSocket.accept()
        print("Got connection with ", address)

    # data = clientSocket.recv("1024")
    # print("Received [%s] \n " % data)
    # clientSocket.close()
    # serverSocket.close()
    # The commented out gets data from the socket and then closes the sockets

    except bluetooth.btcommon.BluetoothError:
        return 1.3

    return port

def runClient(): #portNumber):
    tabAddress= "74:04:2b:e8:19:86"
    port = bluetooth.PORT_ANY

    sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    try:
        sock.connect((tabAddress, port))
    except bluetooth.btcommon.BluetoothError:
        return False
    return True

while (1):
    success = runClient()
    if success == False :
        print("Failed")
        time.sleep(0.01)
    else :
        print("Success")
        break
