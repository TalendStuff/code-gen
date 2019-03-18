# tos-7.1.1-docker

Docker image for running Talend Open Studio 7.1.1 and building Talend Open Studio 7.1.1 projects

To build a tos:7.1.1 image:

    docker build --tag tos:7.1.1 .

To run Talend Open Studio:

    docker run --net=host --env="DISPLAY" -v "$HOME/.Xauthority:/root/.Xauthority:rw" -it --rm tos:7.1.1 TOS_DI-linux-gtk-x86.sh

To debug Talend Open Studio:

    docker run --net=host --env="DISPLAY" -v "$HOME/.Xauthority:/root/.Xauthority:rw" -it --rm tos:7.1.1 debug-tos.sh

To run the talend code generator building the generic timestep harvester:

    docker run --net=host --env="DISPLAY" -v "$HOME/.Xauthority:/root/.Xauthority:rw" -it --rm tos:7.1.1 build.sh GENERIC_TIMESTEP GENERIC_TIMESTEP_harvester

To debug the talend code generator building the generic timestep harvester:

    docker run --net=host --env="DISPLAY" -v "$HOME/.Xauthority:/root/.Xauthority:rw" -it --rm tos:7.1.1 debug.sh GENERIC_TIMESTEP GENERIC_TIMESTEP_harvester

To use a host workspace and user component directory mount them to /workspace and /opt/talend-components using

    -v "$HOME/git-repos/aodn/harvesters/workspace:/workspace:rw" -v "$HOME/git-repos/aodn/talend-components/directory-build/target/talend-components:/opt/talend-components"

For example to build the generic timestep harvester in $HOME/git-repos/aodn/harvesters/workspace using components in $HOME/git-repos/aodn/talend-components/directory-build/target/talend-components:/opt/talend-components

     docker run --net=host --env="DISPLAY" -v "$HOME/.Xauthority:/root/.Xauthority:rw" -v "$HOME/git-repos/aodn/harvesters/workspace:/workspace:rw" -v "$HOME/git-repos/aodn/talend-components/directory-build/target/talend-components:/opt/talend-components" -it --rm tos:7.1.1 build.sh GENERIC_TIMESTEP GENERIC_TIMESTEP_harvester

Errors will be reported in $HOME/git-repos/aodn/harvesters/workspace/.build-workspace/.metadata/.log

Output will go to $HOME/git-repos/aodn/harvesters/workspace/.talend-build (if there are no errors)
