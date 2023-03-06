
# ==================================================================================================
# Calls GetVersion.cmake script, which builds the output `post_file` file, by replacing strings
# ${BUILD_ID} and ${COMMIT_ID} with values retrieved from programs on the system.
#
# BUILD_ID is set to date in the format YYMMDD. Requires, `date` in Linux or `powershell` in
# Windows. If they are not found BUILD_ID is set to `Unknown`.
#
# COMMIT_ID is set to the last git commit ID of the last commit in the current folder.
# Requires, `git`.If Git is not found COMMIT_ID is set to `Unknown`.
# ==================================================================================================
function(ConfigureVersionInFile target pre_file post_file)

    add_custom_target(${target} ALL
        DEPENDS ${pre_file}
        BYPRODUCTS ${post_file}
        COMMAND ${CMAKE_COMMAND}
                -DCreateVersion=1
                -Dinfile=${pre_file}
                -Doutfile=${post_file}
                -Dsourcedir=${CMAKE_CURRENT_SOURCE_DIR}
                -P ${CMAKE_SOURCE_DIR}/cmake/GetVersion.cmake)
endfunction()

# ==================================================================================================
# Not to be called directly.
# Retrieves last commit ID of the last commit in the current folder.
# ==================================================================================================
function(GetCommitID var)
    find_package(Git)

    if (Git_FOUND)
        execute_process(
            COMMAND git log --format=%h -n 1 ${source_dir}
            OUTPUT_STRIP_TRAILING_WHITESPACE
            OUTPUT_VARIABLE commit_id
            RESULT_VARIABLE exit_code)

        if (NOT exit_code)
            set(${var} ${commit_id} PARENT_SCOPE)
        endif()
    endif()

endfunction()

# ==================================================================================================
# Not to be called directly.
# Build ID is date in YYMMDD format.
# ==================================================================================================
function(GetBuildID var)
    if(UNIX)
        find_program(DATE date)
        if (DATE)
            set(date_command date +%y%m%d)
        endif()
    elseif(WIN32)
        find_program(POWERSHELL powershell)
        if (POWERSHELL)
            set(date_command powershell -Command Get-Date -Format "yymmdd")
        endif()
    endif()

    if (DATE OR POWERSHELL)
        execute_process(
            COMMAND ${date_command}
            OUTPUT_STRIP_TRAILING_WHITESPACE
            OUTPUT_VARIABLE build_id
            RESULT_VARIABLE exit_code)

        if (NOT exit_code)
            set(${var} ${build_id} PARENT_SCOPE)
        endif()
    endif()

endfunction()

# ==================================================================================================
# Not to be called directly.
# Main function which takes the build id and commit id and calls configure_file to make the actual
# replacement.
# Note that `configure_file` in CMake Version >= 3.10, will only replace the output file (here
# post_file) if the contents change.
# ==================================================================================================
function(CreateVersion)
    set(COMMIT_ID Unknown)
    set(BUILD_ID Unknown)

    GetCommitID(COMMIT_ID)
    GetBuildID(BUILD_ID)

    configure_file(${infile} ${outfile})
endfunction()

if (CreateVersion)
    CreateVersion()
endif()
