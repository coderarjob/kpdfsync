
# ==================================================================================================
# Adds rules to create a resource file, which overrides the default rule (which is to just copy
# resource files from source to destination)
#
# Receives source and destination path of resource file. If handled return TRUE, otherwise return
# FALSE.
# ==================================================================================================
function(java_add_resource_file resource_source_file resource_dest_file return_var)
    get_filename_component(res_file_ext ${resource_dest_file} EXT)

    if (${res_file_ext} STREQUAL ".settings")
        configure_file(${resource_source_file} ${resource_dest_file})
        set(${return_var} TRUE PARENT_SCOPE)
        return()
    endif()

    set(${return_var} FALSE PARENT_SCOPE)
endfunction()
