import serial
import time

#change ports for system
Linux_port = '/dev/ttyUSB0'
WIndows_port = 'COM3'

channel = serial.Serial(WIndows_port, baudrate = 9600)

# channel.open()
if channel.is_open :
    print('Serial channel open')
else :
    print('Serial Channel not opened check port setting')

time.sleep(2)
print('writing 3 to channel')
channel.write('3'.encode('utf-8'))

channel.close()
if channel.is_open :
    print('Channel close unsuccessful')
else :
    print('Channel closed')
