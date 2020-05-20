#!/bin/bash
cd "$(dirname "$0")"                          # must run TOS from TOS installation directory
export GTK2_RC_FILES="/opt/TOS_DI-20200219_1130-V7.3.1/configuration/.gtkrc"      # use configured gtk2 theme/font
./TOS_DI-linux-gtk-x86.sh
