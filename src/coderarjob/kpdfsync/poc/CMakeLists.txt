cmake_minimum_required(VERSION 3.10 FATAL_ERROR)
project(coderarjob.kpdfsync LANGUAGES Java)

set(NS coderarjob/kpdfsync/poc)
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
        coderarjob.kpdfsync.lib
    MANIFEST
        Manifest.txt
    SOURCES
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
    RESOURCES
        ${APP_SETTINGS_FILE}
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
