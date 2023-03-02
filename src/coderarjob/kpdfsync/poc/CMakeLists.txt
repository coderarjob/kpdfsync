cmake_minimum_required(VERSION 3.10 FATAL_ERROR)
project(coderarjob.kpdfsync LANGUAGES Java)

set(JAVA_CLASS_NS coderarjob/kpdfsync/poc)
set(GUI_JAR_FILE ${KPDFSYNC_BIN_DIR}/${PROJECT_NAME}.jar)
set(KPDFSYNC_GUI_JAR_FILE ${GUI_JAR_FILE} PARENT_SCOPE)

set(SOURCE_FILES
    pdffixes/pdftocairoFix.java
    pdffixes/AbstractFix.java
    Config.java
    HighlightNotePair.java
    HighlightNotePairListRenderer.java
    HighlightNotePairManager.java
    Log.java
    Main.java
    MainFrame.java
    NoteAssociationDialog.java
    PageNumberListRenderer.java
    PageResource.java
    PdfFixer.java
)

set(RES_FILES
    res/app.settings
    res/attention.png
    res/cancel.png
    res/check-mark.png
    res/choice.png
    res/document.png
    res/folder-2456686.png
    res/folder.png
    res/highlighter.png
    res/highlight.png
    res/info.png
    res/Logo_Banner.png
    res/Logo_Banner_right.png
    res/Logo_colorful2.png
    res/Logo_colorful4.png
    res/Logo_colorful.png
    res/Logo.png
    res/Logo_transparent.png
    res/Logo_transparent_white.png
    res/open-folder.png
    res/pdf.png
    res/problems.png
    res/service.png
    res/sticky-note.png
    res/stop.png
)

set(CP
    ${KPDFSYNC_LIBS_DIR}/pdfclown.jar
)

add_jar(${PROJECT_NAME}
    ${GUI_JAR_FILE}
    "${SOURCE_FILES}"
    "${RES_FILES}"
    "${CP}"
    Manifest.txt
)

add_dependencies(${PROJECT_NAME} "coderarjob.ajl" "coderarjob.kpdfsync.lib")
