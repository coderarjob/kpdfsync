cmake_minimum_required(VERSION 3.10 FATAL_ERROR)
project(coderarjob.kpdfsync.lib LANGUAGES Java)

set(NS coderarjob/kpdfsync/lib)
set(APP_SETTINGS_FILE res/app.settings)

generate_project_version(${PROJECT_NAME} ${APP_SETTINGS_FILE} ${NS})

add_jar(
    TARGET
        ${PROJECT_NAME}
    JAR_FILE
        ${KPDFSYNC_BIN_DIR}/${PROJECT_NAME}.jar
    NAMESPACE
        ${NS}
    CLASSPATH
        ${KPDFSYNC_LIBS_DIR}/pdfclown.jar
    DEPENDS
        ${PROJECT_NAME}-get-ver
        coderarjob.ajl
    RESOURCES
        ${APP_SETTINGS_FILE}
    SOURCES
        annotator/AbstractAnnotator.java
        annotator/PdfAnnotatorV1.java
        clipparser/AbstractParser.java
        clipparser/KindleParserV1.java
        clipparser/KindleParserV2.java
        clipparser/ParserEvents.java
        clipparser/ParserException.java
        clipparser/ParserFactory.java
        clipparser/ParserResult.java
        pm/AbstractMatcher.java
        pm/BasicMatcher.java
        pm/Line.java
        pm/Match.java
        pm/PatternMatcherEvents.java
        Config.java
        KindleClippingsEntryEnumeration.java
        KindleClippingsFile.java
)
