#!/usr/bin/env bash
# You can try running it as a bash script, or just part by part

# http://wiki.ros.org/Robots/evarobot/Tutorials/indigo/Kinect

# Some dependencies
# sudo apt-get install g++ python libusb-1.0-0-dev freeglut3-dev
# sudo apt-get install doxygen graphviz mono-complete

# 16.04 Needs to add jdk-7 [DON't]
# sudo add-apt-repository ppa:openjdk-r/ppa
# sudo apt-get update
# sudo apt-get install openjdk-7-jdk

# OpenNI
# git clone https://github.com/OpenNI/OpenNI.git
# cd OpenNI
# git checkout Unstable-1.5.8.5 # Check the releases on the github page and download the latest
# cd Platform/Linux/CreateRedist
# sudo chmod +x RedistMaker
# ./RedistMaker
# cd ../Redist/OpenNI-Bin-Dev-Linux-[xxx]
# sudo ./install.sh

# Install Kinect Driver
# git clone  https://github.com/ph4m/SensorKinect.git
# cd SensorKinect/Platform/Linux/CreateRedist
# sudo chmod +x RedistMaker
# ./RedistMaker
# cd ../Redist/Sensor-Bin-Linux-x64-v*
# sudo ./install.sh


# Install ROS packages
# sudo apt-get install ros-kinetic-openni-camera ros-kinetic-openni-launch ros-kinetic-rgbd-launch

cd catkin_ws/src/
git clone https://github.com/ros-drivers/openni_camera.git
git clone https://github.com/ros-drivers/rgbd_launch.git
cd ..
catkin_make

# To run Kinect on ROS:
# roslaunch openni_launch openni.launch

# To visualise Kinect data
# rosrun rviz rviz
