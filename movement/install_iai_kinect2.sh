#!/usr/bin/env bash
# You can try running it as a bash script, or just part by part

# https://github.com/code-iai/iai_kinect2#install

# Install PCL (Point Cloud Library)
  sudo add-apt-repository ppa:v-launchpad-jochen-sprickerhof-de/pcl
  sudo apt-get update
  sudo apt-get install libpcl-all


# Install Eigen3
  sudo apt-get install libeigen3-dev

# Install OpenCl  https://askubuntu.com/a/850594
  sudo apt install ocl-icd-libopencl1
  sudo apt install opencl-headers
  sudo apt install clinfo
  sudo apt install ocl-icd-opencl-dev

# Install OpenCV
  # install dependencies
  sudo apt-get update
  sudo apt-get install build-essential
  sudo apt-get install cmake
  sudo apt-get install libgtk2.0-dev
  sudo apt-get install pkg-config
  sudo apt-get install python-numpy python-dev
  sudo apt-get install libavcodec-dev libavformat-dev libswscale-dev
  sudo apt-get install libjpeg-dev libpng12-dev libtiff5-dev libjasper-dev

  sudo apt-get -qq install libopencv-dev build-essential checkinstall cmake pkg-config yasm libjpeg-dev libjasper-dev libavcodec-dev libavformat-dev libswscale-dev libdc1394-22-dev libxine2 libgstreamer0.10-dev libgstreamer-plugins-base0.10-dev libv4l-dev python-dev python-numpy libtbb-dev libqt4-dev libgtk2.0-dev libmp3lame-dev libopencore-amrnb-dev libopencore-amrwb-dev libtheora-dev libvorbis-dev libxvidcore-dev x264 v4l-utils

  # download opencv-2.4.13
  wget http://downloads.sourceforge.net/project/opencvlibrary/opencv-unix/2.4.13/opencv-2.4.13.zip
  unzip opencv-2.4.13.zip
  cd opencv-2.4.13
  mkdir release
  cd release

  # compile and install
  cmake -G "Unix Makefiles" -DCMAKE_CXX_COMPILER=/usr/bin/g++ CMAKE_C_COMPILER=/usr/bin/gcc -DCMAKE_BUILD_TYPE=RELEASE -DCMAKE_INSTALL_PREFIX=/usr/local -DWITH_TBB=ON -DBUILD_NEW_PYTHON_SUPPORT=ON -DWITH_V4L=ON -DINSTALL_C_EXAMPLES=ON -DINSTALL_PYTHON_EXAMPLES=ON -DBUILD_EXAMPLES=ON -DWITH_QT=ON -DWITH_OPENGL=ON -DBUILD_FAT_JAVA_LIB=ON -DINSTALL_TO_MANGLED_PATHS=ON -DINSTALL_CREATE_DISTRIB=ON -DINSTALL_TESTS=ON -DENABLE_FAST_MATH=ON -DWITH_IMAGEIO=ON -DBUILD_SHARED_LIBS=OFF -DWITH_GSTREAMER=ON ..
  make all -j2 # 2 cores
  sudo make install

  # ignore libdc1394 error http://stackoverflow.com/questions/12689304/ctypes-error-libdc1394-error-failed-to-initialize-libdc1394

  #python
  #> import cv2
  #> cv2.SIFT
  #<built-in function SIFT>

# Install libfreenect2 - https://github.com/OpenKinect/libfreenect2
  git clone https://github.com/OpenKinect/libfreenect2.git
  cd libfreenect2
  sudo apt-get update
  sudo apt-get install build-essential cmake pkg-config # Build tools
  sudo apt-get install libusb-1.0-0-dev
  sudo apt-get install libturbojpeg libjpeg-turbo8-dev
  sudo apt-get install libglfw3-dev # opengl
  sudo apt-get install beignet-dev  #opencl intelGPU
  sudo apt-get install libva-dev libjpeg-dev # vaapi intelGPU
  sudo apt-get install libopenni2-dev # Install OpenNi2

  mkdir build && cd build
  cmake .. -DENABLE_CXX11=ON -DCMAKE_INSTALL_PREFIX=$HOME/freenect2
  cmake .. -DENABLE_CXX11=ON -Dfreenect2_DIR=$HOME/freenect2/lib/cmake/freenect2
  make
  make install
  sudo cp ../platform/linux/udev/90-kinect2.rules /etc/udev/rules.d/ # udev for device access
  # Test Program - will show 4 images NOTE must be run in libfreenect2/build/
  # ./bin/Protonect

  # Return to root
  cd ../../


# Finally install iai_kinect2
  cd catkin_ws/src/ # cd to catkin_ws
  git clone https://github.com/code-iai/iai_kinect2.git
  cd iai_kinect2
  rosdep install -r --from-paths .# rosdep will output errors  on not being able to locate [kinect2_bridge] and [depth_registration]. That is fine because they are all part of the iai_kinect2 package and rosdep does not know these packages.
  cd ../../ # cd back to catkin_ws folder to run catkin_make
  catkin_make -DCMAKE_BUILD_TYPE="Release" # \
              # -Dfreenect2_DIR=path_to_freenect2/lib/cmake/freenect2 # if we changed the location of install of freenect2 - see 'Install libfreenect2' section -Dfreenect2_DIR=$HOME/freenect2/lib/cmake/freenect2

# NOTE: Our cmake got a an error CMake Warning at iai_kinect2/kinect2_registration/CMakeLists.txt:60 (message):
#  Your libOpenCL.so is incompatible with CL/cl.h.  Install ocl-icd-opencl-dev  to update libOpenCL.so?
# But our Ubuntu 16.04 didn't have a newer version available
