cmake_minimum_required(VERSION 3.10 FATAL_ERROR)
project(coderarjob.kpdfsync.lib LANGUAGES Java)

if (NOT Java_FOUND)
    message (FATAL_ERROR "Java not found. Cannot continue.")
endif()

set(LIB_NS coderarjob/kpdfsync/lib)
set(LIB_JAR_FILE ${KPDFSYNC_BIN_DIR}/${PROJECT_NAME}.jar)
set(KPDFSYNC_LIB_JAR_FILE ${LIB_JAR_FILE} PARENT_SCOPE)

add_jar(${PROJECT_NAME}
    OUTPUT_DIR
        ${KPDFSYNC_BIN_DIR}
    INCLUDE_JARS
        ${KPDFSYNC_AJL_JAR_FILE}
        ${CMAKE_SOURCE_DIR}/lib/pdfclown.jar
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

set(RES_FILES
    res/app.settings
)

add_resource_to_jar(${PROJECT_NAME} ${LIB_JAR_FILE} ${LIB_NS} "${RES_FILES}")
