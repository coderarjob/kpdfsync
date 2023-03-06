cmake_minimum_required(VERSION 3.10 FATAL_ERROR)
project(coderarjob.ajl LANGUAGES Java)

add_jar(
    TARGET
        ${PROJECT_NAME}
    NAMESPACE
        coderarjob/ajl
    JAR_FILE
        ${KPDFSYNC_BIN_DIR}/${PROJECT_NAME}.jar
    DEPENDS
        ${PROJECT_NAME}-get-ver
    SOURCES
        file/ByteOrderMark.java
        file/ByteOrderMarkTypes.java
        AbstractConfig.java
        Config.java
    RESOURCES
        res/app.settings
)
