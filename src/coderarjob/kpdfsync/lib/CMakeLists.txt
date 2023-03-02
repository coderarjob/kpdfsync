cmake_minimum_required(VERSION 3.10 FATAL_ERROR)
project(coderarjob.kpdfsync.lib LANGUAGES Java)

set(JAVA_CLASS_NS coderarjob/kpdfsync/lib)
set(LIB_JAR_FILE ${KPDFSYNC_BIN_DIR}/${PROJECT_NAME}.jar)
set(KPDFSYNC_LIB_JAR_FILE ${LIB_JAR_FILE} PARENT_SCOPE)

set(SOURCE_FILES
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

set(CP
    ${KPDFSYNC_LIBS_DIR}/pdfclown.jar
)

add_jar(${PROJECT_NAME}
    ${LIB_JAR_FILE}
    "${SOURCE_FILES}"
    "${RES_FILES}"
    "${CP}"
    ""
)

add_dependencies(${PROJECT_NAME} "coderarjob.ajl")
