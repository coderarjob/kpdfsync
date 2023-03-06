
# ==================================================================================================
# Global settings (remains constant between different call to add_jar
# ==================================================================================================
if (UNIX)
    set(JAVA_CP_SEPARATOR_CHAR :)
elseif(WIN32)
    set(JAVA_CP_SEPARATOR_CHAR \;)
endif()

# ==================================================================================================
# add_jar(TARGET target
#         JAR_FILE <abs path to jar file>
#         SOURCES <source file> ...
#         [NAMESPACE <namespace>]
#         [MANIFEST <manifest file>]
#         [DEPENDS <target> ...]
#         [RESOURCES <resource file> ...]
#         [CLASSPATH <classpath> ..])
#
# Compiles *.java files, copies/creates resource files, then packs the class files and resource
# files into a jar file.
#
# TARGET
# A custom target is of this name is created which depends on the JAR_FILE.
#
# JAR_FILE
# Absolute file name of jar file. Directories will be createed if not exist.
#
# SOURCES
# Java source files are based at CMAKE_CURRENT_LIST_DIR are compiled into
# JAVA_CLASS_DIR/ADDJAR_NAMESPACE folder. Throws error if contains anything other than .java files.
#
# RESOURCES
# Resource files are based at CMAKE_CURRENT_LIST_DIR, and are copied/configured to the destination
# folder based at JAVA_CLASS_DIR/ADDJAR_NAMESPACE. Following rules are used to handle resource files:
#
# - Calls local `java_add_resource_file` function for each resource file. This function should
#   selectively create rules for the creation of resource files at the destination folder. Usually
#   this function does a `configure_file`.
#   Should return TRUE if rule was created for the file, FALSE otherwise.
#
# - If previous call returns FALSE or simply does not exist, then copies each resource file from
#   source to destination folder.
#
# CLASSPATH
# Class path or jar files which need to be passed to javac. JAVA_CLASS_DIR is always added and need
# to be passed separately.
#
# NAMESPACE
# Class files and resource files are generated at ${JAVA_CLASS_DIR}/<namespace>. This is the java
# package string, where '.' is replaced by '/'.
#
# DEPENDS
# Parent target(s) which the current target depends on. This ensures the following:
# - Determines build order; parent dependencies are build before child ones.
# - If any of the targets provided, is created by add_jar, this function adds its Jar files
#   into the current target so that the when the jar file updates the current target is also
#   rebuild. (If the target is not created by add_jar then only build order is ensured.)
#
# MANIFEST
# Manifest file is based at CMAKE_CURRENT_LIST_DIR are added to the jar file.
#
# Variables required to be set:
# JAVA_CLASS_DIR           : Path where class and resource files need to be kept.
# CMAKE_JAVA_COMPILE_FLAGS : Flags which need to be passed to javac.
# ==================================================================================================
function(add_jar)

    # Check if FindJava module is loaded and javac executable was found
    if (NOT Java_FOUND)
        message (FATAL_ERROR "Java not found. Cannot continue.")
    endif()

    # ---------------------------------------------------------------------------------------------
    # Parse arguments
    # ---------------------------------------------------------------------------------------------
    set (oneValueArgs TARGET JAR_FILE NAMESPACE MANIFEST)
    set (multiValueArgs SOURCES RESOURCES CLASSPATH DEPENDS)
    set(options)

    cmake_parse_arguments(PARSE_ARGV 0 ADDJAR "${options}" "${oneValueArgs}" "${multiValueArgs}")

    # Check validity
    if (NOT ADDJAR_JAR_FILE OR NOT ADDJAR_TARGET OR NOT ADDJAR_SOURCES)
        message(FATAL_ERROR "Jar file name, target and sources cannot be empty.")
    endif()

    # Extract jar file directory path. Used to later create the path.
    set(JAVA_JAR_FILE_ABS ${ADDJAR_JAR_FILE})
    get_filename_component(JAVA_JAR_DIR ${JAVA_JAR_FILE_ABS} DIRECTORY)

    # ---------------------------------------------------------------------------------------------
    # Class Path
    # ---------------------------------------------------------------------------------------------
    set(JAVA_CP ${JAVA_CLASS_DIR})

    foreach (classpath IN LISTS ADDJAR_CLASSPATH)
        set(JAVA_CP ${JAVA_CP}${JAVA_CP_SEPARATOR_CHAR}${classpath})
    endforeach()

    # ---------------------------------------------------------------------------------------------
    # Manifest file
    # ---------------------------------------------------------------------------------------------
    # Converts to absolute path (based at CMAKE_CURRENT_LIST_DIR), if manifest file path provided is
    # relative. Does nothing, if absolute path is provided.
    if (ADDJAR_MANIFEST)
        get_filename_component(JAVA_MANIFEST_FILE_ABS ${ADDJAR_MANIFEST}
            ABSOLUTE BASE_DIR ${CMAKE_CURRENT_LIST_DIR})
    endif()

    # ---------------------------------------------------------------------------------------------
    # Resource files
    # ---------------------------------------------------------------------------------------------
    foreach(file IN LISTS ADDJAR_RESOURCES)
        # Converts to absolute path (based at CMAKE_CURRENT_LIST_DIR), does nothing if absolute path
        # is provided.
        get_filename_component(resource_source_file_abs ${file}
            ABSOLUTE BASE_DIR ${CMAKE_CURRENT_LIST_DIR})

        # Joins absolute JAVA_CLASS_DIR and ADDJAR_NAMESPACE paths together. Works even if
        # ADDJAR_NAMESPACE was not provided.
        get_filename_component(resource_dest_file_abs
            ${JAVA_CLASS_DIR}/${ADDJAR_NAMESPACE}/${file} ABSOLUTE)

        if (COMMAND java_add_resource_file)
            java_add_resource_file(${resource_source_file_abs} ${resource_dest_file_abs} IS_HANDLED)
        else()
            set(IS_HANDLED FALSE)
            message(AUTHOR_WARNING "Falling back to default behaviour (copy) as \
                                    java_add_resource_file() is not found.")
        endif()

        # If not handled by `java_add_resource_file` function, the default action is to copy
        # resource flies from source to destination directories.
        if (NOT ${IS_HANDLED})
            get_filename_component(resource_dest_file_path_abs ${resource_dest_file_abs} DIRECTORY)
            add_custom_command(
                OUTPUT  ${resource_dest_file_abs}
                DEPENDS ${resource_source_file_abs}
                COMMAND ${CMAKE_COMMAND} -E make_directory ${resource_dest_file_path_abs}
                COMMAND ${CMAKE_COMMAND} -E copy ${resource_source_file_abs}
                                                 ${resource_dest_file_abs})
        endif()

        list(APPEND JAVA_RESOURCE_FILES_ABS ${resource_dest_file_abs})
    endforeach()

    # ---------------------------------------------------------------------------------------------
    # Java source files
    # ---------------------------------------------------------------------------------------------
    foreach(file IN LISTS ADDJAR_SOURCES)
        list(APPEND JAVA_SOURCE_FILES_ABS ${CMAKE_CURRENT_LIST_DIR}/${file})

        get_filename_component(file_ext ${file} EXT)
        if (NOT ${file_ext} STREQUAL ".java")
            message(FATAL_ERROR "Source files must only be *.java files")
        endif()

        get_filename_component(file_title ${file} NAME_WLE)
        get_filename_component(file_path ${file} DIRECTORY)
        set(file_without_ext ${file_path}/${file_title})
        get_filename_component(class_file_path_abs
            ${JAVA_CLASS_DIR}/${ADDJAR_NAMESPACE}/${file_without_ext}.class ABSOLUTE)

        list(APPEND JAVA_CLASS_FILES_ABS ${class_file_path_abs})
    endforeach()

    # ---------------------------------------------------------------------------------------------
    add_custom_target(${ADDJAR_TARGET} ALL
        DEPENDS ${JAVA_JAR_FILE_ABS})

    # Set `JAR_FILE` property - Absolute path to the jar file of the target.
    set_target_properties(${ADDJAR_TARGET} PROPERTIES JAR_FILE ${JAVA_JAR_FILE_ABS})

    # Adds dependencies only if there are non empty value in DEPENDS.
    if (ADDJAR_DEPENDS)
      # Ensures proper order of building.
      # Note: Requried even after adding jar file as dependencies.
        add_dependencies(${ADDJAR_TARGET} ${ADDJAR_DEPENDS})
    endif()

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
                ${Java_JAR_EXECUTABLE} ${JAVA_JAR_FLAGS} ${ADDJAR_NAMESPACE})

    # ---------------------------------------------------------------------------------------------

    # Collates Jar file of its parent targets and adds them as dependencies when compiling source
    # files. This ensures that the source files are recompiled if any one of its parent jar file
    # changes.
    # This step is required, simply add_dependencies does not ensure the above - even if parent jar
    # file is rebuilt, the child/dependent is not rebuild.
    foreach(target IN LISTS ADDJAR_DEPENDS)
        get_target_property(target_jar_file ${target} JAR_FILE)
        if (target_jar_file)
          list(APPEND DEPENDENT_JAR_FILES_ABS ${target_jar_file})
        endif()
    endforeach()

    # DRAWBACK:
    # Every class files are rebuild, even if single java file was changed.
    # This is because the way the custom command is setup, every class file depends on every source
    # files. Which means all of the class files are rebuilt even if one source file changes.
    # SOLUTION:
    # There is no easy solution to this problem. If I try to create one command for each source and
    # class file and compile a single source file, the javac complains of missing class definitions
    # (as the file which includes the definition was not included in the compilation). This
    # solution will only work if we compile source files in the perfect order, and add class path
    # each dependent class file previously generated. I assume this is what `Maven` or other build
    # systems do when compiling java projects.
    add_custom_command(
        OUTPUT ${JAVA_CLASS_FILES_ABS}
        DEPENDS ${JAVA_SOURCE_FILES_ABS} ${DEPENDENT_JAR_FILES_ABS}
        COMMAND ${Java_JAVAC_EXECUTABLE} ${CMAKE_JAVA_COMPILE_FLAGS}
                                         -cp "${JAVA_CP}"
                                         -d ${JAVA_CLASS_DIR} ${JAVA_SOURCE_FILES_ABS})
endfunction()
