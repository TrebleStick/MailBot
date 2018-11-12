import serial
import time

#change ports for system
Linux_port = '/dev/ttyUSB0'
Windows_port = 'COM3'

channel = serial.Serial(Windows_port, baudrate = 9600, timeout = 2)

# channel.open()
if channel.is_open :
    print('Serial channel open')
else :
    print('Serial Channel not opened check port setting')

# time.sleep(2)

print('writing comms init to channel')
channel.write('9'.encode('utf-8'))

data = channel.readline()
if (data[0]-48) == 9 :
    print('woop')

channel.write('3'.encode('utf-8'))

data = channel.readline()
print(data[0]-48)



time.sleep(1)

channel.close()
if channel.is_open :
    print('Channel close unsuccessful')
else :
    print('Channel closed')
