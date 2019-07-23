#!/bin/bash
cd "$(dirname "$0")"                          # must run TOS from TOS installation directory
export GTK2_RC_FILES="/opt/TOS_DI-20181026_1147-V7.1.1/configuration/.gtkrc"      # use configured gtk2 theme/font
./TOS_DI-linux-gtk-x86.sh
