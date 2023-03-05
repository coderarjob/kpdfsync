cmake_minimum_required(VERSION 3.10 FATAL_ERROR)
project(coderarjob.ajl LANGUAGES Java)

set(NS coderarjob/ajl)
set(APP_SETTINGS_FILE res/app.settings)

generate_project_version(${PROJECT_NAME} ${APP_SETTINGS_FILE} ${NS})

add_jar(
    TARGET
        ${PROJECT_NAME}
    NAMESPACE
        ${NS}
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
        ${APP_SETTINGS_FILE}
)
