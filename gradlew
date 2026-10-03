#!/bin/sh
#
# Gradle start up script for UN*X
#
# Important for running:
# (1) You need a POSIX-compliant shell to run this script. If your /bin/sh is
#     noncompliant, but you have some other compliant shell such as ksh or
#     bash, then to run this script, type that shell name before the whole
#     command line, like:
#
#           ksh gradlew
#
#     Busybox and similar reduced shells will NOT work, because this script
#     requires all of these POSIX shell features:
#         * functions;
#         * expansions «$var», «${var}», «${var:-default}», «${var+default}»,
#           «${var#prefix}», «${var%suffix}», and «$( cmd )»;
#         * compound commands having a testable exit status, especially «case»;
#         * various built-in commands: «command», «set», «shift», «export», «readonly»;
#         * various built-in commands: «echo», «printf», «test», «[», «]», «true», «false»;
#         * various built-in commands: «read», «unset», «trap», «exec», «exit», «return»;
#         * various built-in commands: «cd», «pwd», «dirname», «basename», «expr», «sleep»;
#         * various built-in commands: «kill», «wait», «ulimit», «umask», «uname», «id»;
#         * various built-in commands: «sed», «awk», «tr», «cut», «sort», «uniq», «wc»;
#         * various built-in commands: «head», «tail», «cat», «tee», «grep», «egrep», «fgrep»;
#         * various built-in commands: «ls», «rm», «mv», «cp», «ln», «mkdir», «rmdir»;
#         * various built-in commands: «chmod», «chown», «chgrp», «ln», «stat», «file»;
#         * various built-in commands: «date», «whoami», «hostname», «uname», «id»;
#         * various built-in commands: «ps», «df», «du», «free», «top», «vmstat», «iostat»;
#         * various built-in commands: «netstat», «ss», «ip», «route», «ifconfig», «ping»;
#         * various built-in commands: «tar», «gzip», «gunzip», «bzip2», «bunzip2», «xz», «unxz»;
#         * various built-in commands: «ssh», «scp», «sftp», «rsync», «curl», «wget»;
#         * various built-in commands: «git», «hg», «svn», «cvs», «bzr», «darcs»;
#         * various built-in commands: «make», «cmake», «ninja», «meson», «autoconf», «automake»;
#         * various built-in commands: «python», «python3», «perl», «ruby», «node», «npm», «yarn»;
#         * various built-in commands: «java», «javac», «jar», «javadoc», «javap», «jdb», «jconsole»;
#         * various built-in commands: «groovy», «groovyc», «groovyConsole», «groovysh», «groovydoc»;
#         * various built-in commands: «kotlin», «kotlinc», «kotlin-script», «kotlin-compiler»;
#         * various built-in commands: «scala», «scalac», «sbt», «mill», «bloop», «coursier»;
#         * various built-in commands: «go», «gofmt», «govet», «golint», «goimports», «gopls»;
#         * various built-in commands: «rustc», «cargo», «rustfmt», «clippy», «rust-analyzer»;
#         * various built-in commands: «swift», «swiftc», «swift-package», «swift-build», «swift-test»;
#         * various built-in commands: «dart», «pub», «flutter», «flutter-test», «flutter-build»;
#         * various built-in commands: «flutter», «flutter-test», «flutter-build», «flutter-run»;
#         * various built-in commands: «flutter», «flutter-test», «flutter-build», «flutter-run»;
#
# (2) You need to have a Java Development Kit (JDK) installed. This script
#     requires at least Java 8. If you have multiple JDKs installed, you can
#     specify which one to use by setting the JAVA_HOME environment variable.
#
# (3) You need to have the Gradle distribution downloaded. This script will
#     download it automatically if it's not already present.
#
# (4) You need to have write permission to the Gradle user home directory.
#     By default, this is ~/.gradle. If you want to use a different directory,
#     set the GRADLE_USER_HOME environment variable.
#
# (5) You need to have network access to download the Gradle distribution.
#     If you're behind a proxy, you may need to set the HTTP_PROXY and
#     HTTPS_PROXY environment variables.
#
# (6) You need to have the correct file permissions. This script must be
#     executable. If it's not, run: chmod +x gradlew
#
# (7) You need to have the correct line endings. This script must use Unix
#     line endings (LF). If it has Windows line endings (CRLF), it won't work.
#     You can convert it using: dos2unix gradlew
#
# (8) You need to have the correct shebang. This script must start with
#     #!/bin/sh. If it doesn't, it won't work.
#
# (9) You need to have the correct encoding. This script must be UTF-8.
#     If it's not, it won't work.
#
# (10) You need to have the correct version. This script is for Gradle 8.5.
#      If you're using a different version, you may need to update the script.
#
# For more information, see the Gradle documentation:
# https://docs.gradle.org/current/userguide/gradle_wrapper.html

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`/"$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if [ "$cygwin" = "false" -a "$darwin" = "false" -a "$nonstop" = "false" ] ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD >/dev/null 2>&1
        # If ulimit returns an error, we don't want to fail, so we ignore it.
    fi
fi

# For Darwin, add options to specify how the application appears in the dock
if $darwin; then
    GRADLE_OPTS="$GRADLE_OPTS \"-Xdock:name=$APP_NAME\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""
fi

# For Cygwin or MSYS, switch paths to Windows format before running java
if [ "$cygwin" = "true" -o "$msys" = "true" ] ; then
    APP_HOME=`cygpath --path --mixed "$APP_HOME"`
    CLASSPATH=`cygpath --path --mixed "$CLASSPATH"`
    JAVACMD=`cygpath --unix "$JAVACMD"`

    # We build the pattern for arguments to be converted via cygpath
    ROOTDIRSRAW=`find -L / -maxdepth 1 -mindepth 1 -type d 2>/dev/null | sed -e 's/ /\\ /g'`
    ROOTDIRS=`echo "$ROOTDIRSRAW" | sed -e 's/ /\\ /g'`
    # Now convert the arguments - kludge to limit to 100 args
    for arg in "$@" ; do
        CHECK=`echo "$arg" | egrep -c '^[a-zA-Z]:\\'`
        if [ $CHECK -ne 0 ] ; then
            arg=`cygpath --path --mixed "$arg"`
        fi
        # Remove the first space
        arg=`echo "$arg" | sed -e 's/^ //'`
        # Add the argument to the list
        set -- "$@" "$arg"
        shift
    done
fi

# Split up the JVM_OPTS And GRADLE_OPTS values into an array, following the shell quoting and substitution rules
function splitJvmOpts() {
    JVM_OPTS=()
    for opt in "$@" ; do
        JVM_OPTS+=("$opt")
    done
}

eval splitJvmOpts $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS
JVM_OPTS[${#JVM_OPTS[*]}]="-Dorg.gradle.appname=$APP_BASE_NAME"

exec "$JAVACMD" "${JVM_OPTS[@]}" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"