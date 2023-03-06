cmake_minimum_required(VERSION 3.10 FATAL_ERROR)

find_package(Java 1.8 REQUIRED) # Loads FindJava.cmake

project(kpdfsync LANGUAGES NONE)

set(KPDFSYNC_BIN_DIR ${CMAKE_BINARY_DIR}/bin)
set(KPDFSYNC_LIBS_DIR ${CMAKE_CURRENT_SOURCE_DIR}/lib)

set(KPDFSYNC_VER 0.10.0 CACHE STRING "Application version.")
set(KPDFSYNC_TAG alpha CACHE STRING "Application version tag.")
set(KPDFSYNC_JDK_VER_TARGET 8)

# Used by cmake/JavaCmake.cmake
set(CMAKE_JAVA_COMPILE_FLAGS -target ${KPDFSYNC_JDK_VER_TARGET}
                             -source ${KPDFSYNC_JDK_VER_TARGET})
set(JAVA_CLASS_DIR ${CMAKE_BINARY_DIR}/class)

include(cmake/GetVersion.cmake)
include(cmake/AddResource.cmake)
include(cmake/JavaCmake.cmake)

add_subdirectory(src/coderarjob/ajl)
add_subdirectory(src/coderarjob/kpdfsync/lib)
add_subdirectory(src/coderarjob/kpdfsync/poc)
