#!/usr/bin/env python
import rospy
import sys
from std_msgs.msg import String

def talker():
    pub = rospy.Publisher('atLocation', String)
    rospy.init_node('atLocationPub', anonymous=True)
    rate = rospy.Rate(10) # 10hz
    while not rospy.is_shutdown():
        test_str = str(location)
        rospy.loginfo(test_str)
        pub.publish(test_str)
        rate.sleep()
        rospy.signal_shutdown('Quit')

if __name__ == '__main__':
    myargv = rospy.myargv(argv=sys.argv)
    location = myargv[1]
    try:
        talker()
    except rospy.ROSInterruptException:
        pass
