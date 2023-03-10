cmake_minimum_required(VERSION 3.10 FATAL_ERROR)
project(kpdfsync LANGUAGES NONE)

find_package(Java 1.8 REQUIRED)
include(cmake/GetVersion.cmake)
include(cmake/AddResource.cmake)
include(cmake/JavaCmake.cmake)

set(KPDFSYNC_BIN_DIR ${CMAKE_BINARY_DIR}/bin)
set(KPDFSYNC_LIBS_DIR ${CMAKE_CURRENT_SOURCE_DIR}/lib)
set(KPDFSYNC_DOCS_DIR ${CMAKE_CURRENT_SOURCE_DIR}/docs)
set(KPDFSYNC_RELEASE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/release)
set(KPDFSYNC_PACK_DIR ${KPDFSYNC_RELEASE_DIR}/current)
set(KPDFSYNC_RELEASE_RESOURCE_DIR ${KPDFSYNC_RELEASE_DIR}/resource)

set(KPDFSYNC_JDK_VER_TARGET 8)
set(KPDFSYNC_VER 0.0.0   CACHE STRING "Application version.")
set(KPDFSYNC_TAG <undef> CACHE STRING "Application version tag.")

# Used by cmake/JavaCmake.cmake
set(CMAKE_JAVA_COMPILE_FLAGS -target ${KPDFSYNC_JDK_VER_TARGET}
                             -source ${KPDFSYNC_JDK_VER_TARGET})
set(JAVA_CLASS_DIR ${CMAKE_BINARY_DIR}/class)

set (KPDFSYNC_PACK_DIR_FILES
    ${KPDFSYNC_RELEASE_RESOURCE_DIR}/kpdfsync.bat
    ${KPDFSYNC_RELEASE_RESOURCE_DIR}/kpdfsync
    ${KPDFSYNC_DOCS_DIR}/QuickReference.pdf
    ${CMAKE_CURRENT_SOURCE_DIR}/LICENSE
    ${CMAKE_CURRENT_SOURCE_DIR}/README.md
)

add_custom_command(
    OUTPUT
        ${KPDFSYNC_BIN_DIR}/tools
        ${KPDFSYNC_BIN_DIR}/pdfclown.jar
    COMMAND ${CMAKE_COMMAND} -E copy_directory ${KPDFSYNC_RELEASE_RESOURCE_DIR}/tools
                                               ${KPDFSYNC_BIN_DIR}/tools
    COMMAND ${CMAKE_COMMAND} -E copy ${KPDFSYNC_LIBS_DIR}/pdfclown.jar ${KPDFSYNC_BIN_DIR}
)

add_custom_target(build-all ALL)

add_custom_target(mv-deps
    DEPENDS ${KPDFSYNC_BIN_DIR}/tools
            ${KPDFSYNC_BIN_DIR}/pdfclown.jar)

add_custom_target(run
    COMMAND ${CMAKE_COMMAND} -E env java -jar coderarjob.kpdfsync.jar
    WORKING_DIRECTORY ${KPDFSYNC_BIN_DIR}
)

add_custom_target(pack
   COMMAND ${CMAKE_COMMAND} -E remove_directory ${KPDFSYNC_PACK_DIR}
   COMMAND ${CMAKE_COMMAND} -E make_directory ${KPDFSYNC_PACK_DIR}
   COMMAND ${CMAKE_COMMAND} -E copy           ${KPDFSYNC_PACK_DIR_FILES} ${KPDFSYNC_PACK_DIR}
   COMMAND ${CMAKE_COMMAND} -E copy_directory ${KPDFSYNC_BIN_DIR} ${KPDFSYNC_PACK_DIR}/bin
   COMMAND ${CMAKE_COMMAND} -E echo "Package created in directory: ${KPDFSYNC_PACK_DIR}"
)

add_dependencies (pack mv-deps build-all)
add_dependencies(build-all coderarjob.ajl coderarjob.kpdfsync.lib coderarjob.kpdfsync)
add_dependencies(run build-all mv-deps)

add_subdirectory(src/coderarjob/ajl)
add_subdirectory(src/coderarjob/kpdfsync/lib)
add_subdirectory(src/coderarjob/kpdfsync/poc)
