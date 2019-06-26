# tos-7.1.1-docker

Docker image for running Talend Open Studio 7.1.1 and building Talend Open Studio 7.1.1 projects

Requires the code generator to be built before building the image:

    mvn clean package

To build a tos:7.1.1 image:

    docker build --tag tos:7.1.1 .

To run Talend Open Studio or to build talend projects you need to give docker access to your xserver

    xhost +local:docker

(For Windows or Mac refer to https://cuneyt.aliustaoglu.biz/en/running-gui-applications-in-docker-on-windows-linux-mac-hosts/)

Then, to run Talend Open Studio:

    docker run -e DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix -it --rm tos:7.1.1 talend.sh

To debug Talend Open Studio (port 8990):

    docker run --net=host -e DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix -it --rm tos:7.1.1 debug-tos.sh

To use a host workspace and user component directory mount them to /workspace and /opt/talend-components.  For example:

    -v "$HOME/git-repos/aodn/harvesters/workspace:/workspace:rw" -v "$HOME/git-repos/aodn/talend-components/directory-build/target/talend-components:/opt/talend-components"

To build the generic timestep harvester in $HOME/git-repos/aodn/harvesters/workspace using components in $HOME/git-repos/aodn/talend-components/directory-build/target/talend-components:/opt/talend-components

     docker run -e DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix -v "$HOME/git-repos/aodn/harvesters/workspace:/workspace:rw" -v "$HOME/git-repos/aodn/talend-components/directory-build/target/talend-components:/opt/talend-components" -it --rm tos:7.1.1 build.sh GENERIC_TIMESTEP GENERIC_TIMESTEP_harvester

And to debug building the generic timestep harvester (port 8990):

     docker run --net=host -e DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix -v "$HOME/git-repos/aodn/harvesters/workspace:/workspace:rw" -v "$HOME/git-repos/aodn/talend-components/directory-build/target/talend-components:/opt/talend-components" -it --rm tos:7.1.1 debug-build.sh GENERIC_TIMESTEP GENERIC_TIMESTEP_harvester

Errors will be reported in $HOME/git-repos/aodn/harvesters/workspace/.build-workspace/.metadata/.log

Output will go to $HOME/git-repos/aodn/harvesters/workspace/.talend-build (if there are no errors)
