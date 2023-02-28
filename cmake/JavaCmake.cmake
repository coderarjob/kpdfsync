
# ==================================================================================================
# Global settings (remains constant between different call to add_jar
# ==================================================================================================
if (UNIX)
    set(JAVA_CP_SEPARATOR_CHAR :)
elseif(WIN32)
    set(JAVA_CP_SEPARATOR_CHAR \;)
endif()

# ==================================================================================================
# function(add_jar target source_files resource_files manifest_file)
#
# Compiles *.java files, copies/creates resource files, then packs the class files and resource
# files into a jar file.
#
# Parameters:
#
# jar_file_path:  Absolute file name of jar file. Directories will be createed if not exist.
#
# source_files:   Java source files are based at CMAKE_CURRENT_LIST_DIR are compiled into
#                 JAVA_CLASS_DIR/JAVA_CLASS_NS folder.
#                 Throws error if contains anything other than .java files.
#
# resource_files: Resource files are based at CMAKE_CURRENT_LIST_DIR, and are copied/configured to
#                 the destination folder based at JAVA_CLASS_DIR/JAVA_CLASS_NS. Following rules are
#                 used to handle resource files:
#
#                 - Calls local `java_add_resource_file` function for each resource file. This
#                   function should selectively create rules for the creation of resource files at
#                   the destination folder. Usually this function does a `configure_file`.
#                   Should return TRUE if rule was created for the file, FALSE otherwhise.
#
#                 - If previous call returns FALSE or simply does not exist, then copies each
#                   resource file from source to destination folder.
#
# cp:             Class path or jar files which need to be passed to javac. JAVA_CLASS_DIR is
#                 always added and need to be passed separately.
#
# manifest_file:  Manifest file is based at CMAKE_CURRENT_LIST_DIR are added to the jar file.
#
# Variables required to be set:
#
# JAVA_CLASS_NS (Optional) : Package/Namespace of the source and resource files.
# JAVA_CLASS_DIR           : Path where class and resource files need to be kept.
# CMAKE_JAVA_COMPILE_FLAGS : Flags which need to be passed to javac.
# ==================================================================================================
function(add_jar target jar_file_path source_files resource_files cp manifest_file)

    # Check if FindJava module is loaded and javac executable was found
    if (NOT Java_FOUND)
        message (FATAL_ERROR "Java not found. Cannot continue.")
    endif()

    # Extract jar file directory path. Used to later create the path.
    get_filename_component(JAVA_JAR_DIR ${jar_file_path} DIRECTORY)
    set(JAVA_JAR_FILE_ABS ${jar_file_path})

    # ---------------------------------------------------------------------------------------------
    # Class Path
    # ---------------------------------------------------------------------------------------------
    set(JAVA_CP ${JAVA_CLASS_DIR})

    foreach (classpath IN LISTS cp)
        set(JAVA_CP ${JAVA_CP}${JAVA_CP_SEPARATOR_CHAR}${classpath})
    endforeach()

    # ---------------------------------------------------------------------------------------------
    # Manifest file
    # ---------------------------------------------------------------------------------------------
    # Converts to absolute path (based at CMAKE_CURRENT_LIST_DIR), if manifest file path provided is
    # relative. Does nothing, if absolute path is provided.
    if (manifest_file)
        get_filename_component(JAVA_MANIFEST_FILE_ABS ${manifest_file} ABSOLUTE BASE_DIR ${CMAKE_CURRENT_LIST_DIR})
    endif()

    # ---------------------------------------------------------------------------------------------
    # Resource files
    # ---------------------------------------------------------------------------------------------
    foreach(file IN LISTS resource_files)
        # Converts to absolute path (based at CMAKE_CURRENT_LIST_DIR), does nothing if absolute path
        # is provided.
        get_filename_component(resource_source_file_abs ${file} ABSOLUTE BASE_DIR ${CMAKE_CURRENT_LIST_DIR})

        # Joins absolute JAVA_CLASS_DIR and JAVA_CLASS_NS paths together. Works even if
        # JAVA_CLASS_NS was not provided.
        get_filename_component(resource_dest_file_abs ${JAVA_CLASS_DIR}/${JAVA_CLASS_NS}/${file} ABSOLUTE)

        if (COMMAND java_add_resource_file)
            java_add_resource_file(${resource_source_file_abs} ${resource_dest_file_abs} IS_HANDLED)
        else()
            set(IS_HANDLED FALSE)
            message(AUTHOR_WARNING "Falling back to default behaviour (copy) as java_add_resource_file() is not found.")
        endif()

        # If not handled by `java_add_resource_file` function, the default action is to copy
        # resource flies from source to destination directories.
        if (NOT ${IS_HANDLED})
            get_filename_component(resource_dest_file_path_abs ${resource_dest_file_abs} DIRECTORY)
            add_custom_command(
                OUTPUT  ${resource_dest_file_abs}
                DEPENDS ${file}
                COMMAND ${CMAKE_COMMAND} -E make_directory ${resource_dest_file_path_abs}
                COMMAND ${CMAKE_COMMAND} -E copy ${resource_source_file_abs}
                                                 ${resource_dest_file_abs})
        endif()

        # Convert to relative path (relative to JAVA_CLASS_DIR).
        string(REPLACE "${JAVA_CLASS_DIR}/" "" resource_dest_file_rel ${resource_dest_file_abs})

        list(APPEND JAVA_RESOURCE_FILES_ABS ${resource_dest_file_abs})
        list(APPEND JAVA_RESOURCE_FILES_REL ${resource_dest_file_rel})

    endforeach()

    # ---------------------------------------------------------------------------------------------
    # Java source files
    # ---------------------------------------------------------------------------------------------
    foreach(file IN LISTS source_files)
        list(APPEND JAVA_SOURCE_FILES_ABS ${CMAKE_CURRENT_LIST_DIR}/${file})

        get_filename_component(file_ext ${file} EXT)
        if (NOT ${file_ext} STREQUAL ".java")
            message(FATAL_ERROR "Source files must only be *.java files")
        endif()

        get_filename_component(file_title ${file} NAME_WLE)
        get_filename_component(file_path ${file} DIRECTORY)
        set(file_without_ext ${file_path}/${file_title})
        get_filename_component(class_file_path_abs ${JAVA_CLASS_DIR}/${JAVA_CLASS_NS}/${file_without_ext}.class ABSOLUTE)
        string(REPLACE "${JAVA_CLASS_DIR}/" "" class_file_path_rel ${class_file_path_abs})

        list(APPEND JAVA_CLASS_FILES_ABS ${class_file_path_abs})
        list(APPEND JAVA_CLASS_FILES_REL ${class_file_path_rel})
    endforeach()

    # ---------------------------------------------------------------------------------------------
    add_custom_target(
        ${target} ALL
        DEPENDS ${JAVA_JAR_FILE_ABS})

    # ---------------------------------------------------------------------------------------------
    if (JAVA_MANIFEST_FILE_ABS)
        set(JAVA_JAR_FLAGS cfm ${JAVA_JAR_FILE_ABS} ${JAVA_MANIFEST_FILE_ABS})
    else()
        set(JAVA_JAR_FLAGS cf ${JAVA_JAR_FILE_ABS})
    endif()

    add_custom_command(
        OUTPUT ${JAVA_JAR_FILE_ABS}
        DEPENDS ${JAVA_CLASS_FILES_ABS} ${JAVA_RESOURCE_FILES_ABS}
        COMMAND ${CMAKE_COMMAND} -E make_directory ${JAVA_JAR_DIR}
        COMMAND ${CMAKE_COMMAND} -E chdir ${JAVA_CLASS_DIR}
                ${Java_JAR_EXECUTABLE} ${JAVA_JAR_FLAGS} ${JAVA_CLASS_NS})

    # ---------------------------------------------------------------------------------------------
# Drawback: Every class files are rebuild, even if single java file was changed.
# This is because the way the custom command is setup, every class file depends on every the source
# files. Which means all of the class files are rebuilt even if one source file changes.
# There is no easy solution to this problem. If I try to create one command for each source and
# class file and compile a single source file, the javac complains of missing class definations (as
# the file which includes the defination was not included in the compilation).
    add_custom_command(
        OUTPUT ${JAVA_CLASS_FILES_ABS}
        DEPENDS ${JAVA_SOURCE_FILES_ABS}
        COMMAND ${Java_JAVAC_EXECUTABLE} ${CMAKE_JAVA_COMPILE_FLAGS} -cp "${JAVA_CP}"
                                         -d ${JAVA_CLASS_DIR} ${JAVA_SOURCE_FILES_ABS})
endfunction()
