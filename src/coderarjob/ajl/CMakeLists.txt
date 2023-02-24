cmake_minimum_required(VERSION 3.10 FATAL_ERROR)
project(coderarjob.ajl LANGUAGES Java)

if (NOT Java_FOUND)
    message (FATAL_ERROR "Java not found. Cannot continue.")
endif()

set(AJL_NS coderarjob/ajl)
set(AJL_JAR_FILE ${KPDFSYNC_BIN_DIR}/${PROJECT_NAME}.jar)
set(KPDFSYNC_AJL_JAR_FILE ${AJL_JAR_FILE} PARENT_SCOPE)

add_jar(${PROJECT_NAME}
    OUTPUT_DIR ${KPDFSYNC_BIN_DIR}
    SOURCES
        file/ByteOrderMark.java
        file/ByteOrderMarkTypes.java
        AbstractConfig.java
        Config.java
)

set(RES_FILES
    res/app.settings
)

message(${AJL_JAR_FILE})
add_resource_to_jar(${PROJECT_NAME} ${AJL_JAR_FILE} ${AJL_NS} "${RES_FILES}")
