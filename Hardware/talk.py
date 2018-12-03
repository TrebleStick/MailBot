import serial
import time

Linux_port = '/dev/ttyUSB0'
Windows_port = 'COM3'

pins = ['1','2','3','4','5','6', '7']
# pins = ['1','2','3']

channel = serial.Serial(Windows_port, baudrate = 9600, timeout = 2)

if channel.is_open :
    print('Serial channel open')
else :
    print('Serial Channel not opened check port setting')

time.sleep(2)

#------------DEMO-ALL-LATCHES--------------#
for i in pins :
    print(i)
    channel.write(i.encode('utf-8'))
    time.sleep(1.5)

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
