import bluetooth
import os
import sys
import subprocess
from subprocess import Popen, PIPE

# Replaces the ':' with '_' to allow the MacAddress to be in the form
# of a "Path" when "selecting an attribute"
def changeMacAddr(word):
    return ''.join(c if c != ':' else '_' for c in word)

# Connects to a given MacAddress and then selects the attribute to write to
def connBT(BTsubProcess, stringMacAddr):
    BTsubProcess.stdin.write(bytes("".join("connect "+stringMacAddr +"\n"), "utf-8"))
    BTsubProcess.stdin.flush()
    time.sleep(2)
    stringFormat = changeMacAddr(stringMacAddr)
    BTsubProcess.stdin.write(bytes("".join("select-attribute /org/bluez/hci0/dev_"
                              + stringFormat +
                              "/service0010/char0011" + "\n"), "utf-8"))
    BTsubProcess.stdin.flush()

# Can only be run once connBT has run - writes the data in a list [must have numbers 0 - 255 ]
def writeBT(BTsubProcess, listOfData):
    stringList = [str('{0} ').format(elem) for elem in listOfData]
    BTsubProcess.stdin.write(bytes("".join("write " + "".join(stringList) + "\n"), "utf-8"))
    BTsubProcess.stdin.flush()

# Disconnects
def clostBT(BTsubProcess):
    BTsubProcess.communicate(bytes("disconnect\n", "utf-8"))

# To use the functions a subprocess "instance" of bluetoothctl must be made
blt = subprocess.Popen(["bluetoothctl"], stdin=subprocess.PIPE, shell=True)
# blt with then be passed into the function for BTsubProcess

# Note: the MacAddresses of the Bluetooth modules were pre-connected and trusted manually via bluetoothctl
macbot = "74:04:2B:D5:19:86"
