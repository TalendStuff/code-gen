# tos-7.1.1-docker

Docker image for running Talend Open Studio 7.1.1 and building Talend Open Studio 7.1.1 projects

## Pre-requisites

Requires the code generator to be built before building the image:

    # build locally
    mvn clean package
    
    # OR build inside Docker build environment
    docker-compose run dev mvn clean package

To build a tos:7.1.1 image:

    docker build --file Dockerfile.app --tag tos:7.1.1 .

To run Talend Open Studio or to build talend projects you need to give docker access to your xserver

    xhost +local:docker

(For Windows or Mac refer to https://cuneyt.aliustaoglu.biz/en/running-gui-applications-in-docker-on-windows-linux-mac-hosts/)

## Running Talend Open Studio

Then, to run Talend Open Studio:

    docker run -e DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix -it --rm tos:7.1.1 talend.sh

    # OR run with Docker Compose
    docker-compose run app
 
To debug Talend Open Studio (port 8990):

    docker run --net=host -e DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix -it --rm tos:7.1.1 debug-tos.sh

## Editing Harvesters

First you need to clone the harvesters and talend-components repositories to your local machine:

    git clone git@github.com:aodn/talend-components.git
    git clone git@github.com:aodn/harvesters.git

The talend components repo needs to be built to create talend components

    cd talend-components
    mvn clean package

To use the harvesters workspace and talend components built above, mount them to /workspace and /opt/talend-components
and use them in talend.

For example to run talend with harvesters and talend-components cloned to $HOME/git-repos/aodn and as built above:

    docker run --net=host -e DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix \\
     -v "$HOME/git-repos/aodn/harvesters/workspace:/workspace:rw" \\
     -v "$HOME/git-repos/aodn/talend-components/directory-build/target/talend-components:/opt/talend-components" \\
     -it --rm tos:7.1.1 talend.sh

And then to use the harvesters workspace:

* select "Manage Connections" in the project selection dialog
* select /workspace for workspace and restart when asked

And to use IMOS components mounted to /opt/talend-components

* open an existing project
* BEFORE opening any jobs,
  * from the menu select Window / Preferences
  * under talend / components set "user components folder" to /opt/talend-components

## Building harvesters from the command line

To build the generic timestep harvester in $HOME/git-repos/aodn/harvesters/workspace using components in $HOME/git-repos/aodn/talend-components/directory-build/target/talend-components:/opt/talend-components

     docker run -v "$HOME/git-repos/aodn/harvesters/workspace:/workspace:rw" -v "$HOME/git-repos/aodn/talend-components/directory-build/target/talend-components:/opt/talend-components" -it --rm tos:7.1.1 build.sh GENERIC_TIMESTEP GENERIC_TIMESTEP_harvester

And to debug building the generic timestep harvester (port 8990):

     docker run --net=host -v "$HOME/git-repos/aodn/harvesters/workspace:/workspace:rw" -v "$HOME/git-repos/aodn/talend-components/directory-build/target/talend-components:/opt/talend-components" -it --rm tos:7.1.1 debug-build.sh GENERIC_TIMESTEP GENERIC_TIMESTEP_harvester

Errors will be reported in $HOME/git-repos/aodn/harvesters/workspace/.build-workspace/.metadata/.log

Output will go to $HOME/git-repos/aodn/harvesters/workspace/.talend-build (if there are no errors)
