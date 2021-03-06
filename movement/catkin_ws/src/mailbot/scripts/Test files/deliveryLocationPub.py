#!/usr/bin/env python
import rospy
from std_msgs.msg import String

def talker():
    pub = rospy.Publisher('deliveryLocations', String)
    rospy.init_node('deliverLocationPub', anonymous=True)
    rate = rospy.Rate(10) # 10hz
    while not rospy.is_shutdown():
        test_str = "508 507 510"
        rospy.loginfo(test_str)
        pub.publish(test_str)
        rate.sleep()
        rospy.signal_shutdown('Quit')

if __name__ == '__main__':
    try:
        talker()
    except rospy.ROSInterruptException:
        pass
