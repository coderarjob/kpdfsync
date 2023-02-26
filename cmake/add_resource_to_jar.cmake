function(add_resource_to_jar target jarfile namespace resource_files)
    foreach(file IN LISTS resource_files)
        get_filename_component(FILE_EXT ${file} EXT)

        if (${FILE_EXT} STREQUAL ".settings")
            message("Configuring file ${file}")
            configure_file(${file} ${KPDFSYNC_BIN_DIR}/${namespace}/${file})
            set(SOURCE_DIR ${KPDFSYNC_BIN_DIR})
        else()
            message("Not Configuring file ${file}")
            set(SOURCE_DIR ${CMAKE_SOURCE_DIR}/src)
        endif()

        add_custom_command(
            TARGET
                ${target}
            POST_BUILD
            COMMAND
                jar -uf ${jarfile} -C ${SOURCE_DIR} ${namespace}/${file}
        )
    endforeach()
endfunction()
