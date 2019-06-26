#!/bin/sh
unset UBUNTU_MENUPROXY
export GDK_NATIVE_WINDOWS=1
export SWT_GTK3=0
./TOS_DI-linux-gtk-x86_64 --launcher.appendVmargs -vmargs -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8990
