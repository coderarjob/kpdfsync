
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

function(GetCommitID var)
    find_package(Git)

    if (Git_FOUND)
        execute_process(
            COMMAND git log --format=%h -n 1 ${source_dir}
            OUTPUT_STRIP_TRAILING_WHITESPACE
            OUTPUT_VARIABLE commit_id)

        set(${var} ${commit_id} PARENT_SCOPE)
    endif()

endfunction()

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
            OUTPUT_VARIABLE build_id)

        set(${var} ${build_id} PARENT_SCOPE)
    endif()

endfunction()

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
