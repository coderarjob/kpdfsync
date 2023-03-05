# execute_process is only execute at the time CMake is processing the project prior to build system
# gneration or running build. This means the commit id and build id is not updated at the time of
# building.

set(KPDFSYNC_VER 0.10.0 CACHE STRING "Application version.")
set(KPDFSYNC_TAG alpha CACHE STRING "Application version tag.")

execute_process(
    COMMAND git log --format=%h -n 1 ${source_dir}
    OUTPUT_STRIP_TRAILING_WHITESPACE
    OUTPUT_VARIABLE KPDFSYNC_COMMIT_ID)

execute_process(
    COMMAND date +%H%M%S
    OUTPUT_STRIP_TRAILING_WHITESPACE
    OUTPUT_VARIABLE KPDFSYNC_BUILD_ID)

configure_file(${infile} ${outfile})
