#include <ros/ros.h>
#include <geometry_msgs/Twist.h>
#include <sensor_msgs/Joy.h>

vel_pub_ = nh.advertise<geometry_msgs::Twist>("cmd_vel",10);
joy_sub_ = nh.subscribe<sensor_msg::Joy>("joy", 10, &joyCallback, this);

void joyCallback(const sensor_msgs::Joy::ConstPtr& joy_msg)

joy_msg->axes[3];
joy_msg->axes[2];


