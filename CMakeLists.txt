cmake_minimum_required(VERSION 3.10 FATAL_ERROR)

find_package(Java 1.8 REQUIRED) # Loads FindJava.cmake

project(kpdfsync LANGUAGES NONE)

set(KPDFSYNC_BIN_DIR ${CMAKE_CURRENT_BINARY_DIR}/bin)
set(KPDFSYNC_LIBS_DIR ${CMAKE_CURRENT_LIST_DIR}/lib)
set(JAVA_CLASS_DIR ${CMAKE_BINARY_DIR}/class)

set(KPDFSYNC_VER 0.10.0)
set(KPDFSYNC_TAG alpha)
set(KPDFSYNC_COMMIT_ID deadf00)
set(KPDFSYNC_BUILD_ID f001)

set(KPDFSYNC_JDK_VER_TARGET 8)
set(CMAKE_JAVA_COMPILE_FLAGS -target ${KPDFSYNC_JDK_VER_TARGET}
                             -source ${KPDFSYNC_JDK_VER_TARGET})

include(cmake/add_resource.cmake)
include(cmake/JavaCmake.cmake)

add_subdirectory(src/coderarjob/ajl)
add_subdirectory(src/coderarjob/kpdfsync/lib)
add_subdirectory(src/coderarjob/kpdfsync/poc)
