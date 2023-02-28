cmake_minimum_required(VERSION 3.10 FATAL_ERROR)
project(coderarjob.ajl LANGUAGES Java)

set(JAVA_CLASS_NS coderarjob/ajl)
set(AJL_JAR_FILE ${KPDFSYNC_BIN_DIR}/${PROJECT_NAME}.jar)
set(KPDFSYNC_AJL_JAR_FILE ${AJL_JAR_FILE} PARENT_SCOPE)

set(SOURCE_FILES
    file/ByteOrderMark.java
    file/ByteOrderMarkTypes.java
    AbstractConfig.java
    Config.java
)

set(RES_FILES
    res/app.settings
)

add_jar(${PROJECT_NAME}
    ${AJL_JAR_FILE}
    "${SOURCE_FILES}"
    "${RES_FILES}"
    ""
    ""
)
