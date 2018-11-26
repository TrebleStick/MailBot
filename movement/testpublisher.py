import rospy
from std_msgs.msg import String

def talker():
    pub = rospy.Publisher('deliveryLocations', String)
    rospy.init_node('testpublisher', anonymous=True)
    rate = rospy.Rate(10) # 10hz
    while not rospy.is_shutdown():
        test_str = "0 1 3"
        rospy.loginfo(test_str)
        pub.publish(test_str)
        rate.sleep()

if __name__ == '__main__':
    try:
        talker()
    except rospy.ROSInterruptException:
        pass
