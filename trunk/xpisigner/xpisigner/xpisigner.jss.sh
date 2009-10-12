#! /bin/sh

for arg in "$@" ; do
    xpi_exec_args="$xpi_exec_args $arg"
done
 

PRG="$0"
progname=`basename "$0"`

# need this for relative symlinks
while [ -h "$PRG" ] ; do
	ls=`ls -ld "$PRG"`
	link=`expr "$ls" : '.*-> \(.*\)$'`
	if expr "$link" : '/.*' > /dev/null; then
		PRG="$link"
	else
		PRG=`dirname "$PRG"`"/$link"
	fi
done

XPI_HOME=`dirname "$PRG"`

# make it fully qualified
XPI_HOME=`cd "$XPI_HOME" && pwd`

LD_LIBRARY_PATH=$XPI_HOME:$LD_LIBRARY_PATH

exec java -Dxpi.mode=jss -jar $XPI_HOME/xpi.jar $xpi_exec_args