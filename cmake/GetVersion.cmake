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

function(CreateVersion)
    execute_process(
        COMMAND git log --format=%h -n 1 ${source_dir}
        OUTPUT_STRIP_TRAILING_WHITESPACE
        OUTPUT_VARIABLE COMMIT_ID)

    execute_process(
        COMMAND date +%y%m%d
        OUTPUT_STRIP_TRAILING_WHITESPACE
        OUTPUT_VARIABLE BUILD_ID)

    configure_file(${infile} ${outfile})
endfunction()

if (CreateVersion)
    CreateVersion()
endif()
