cmake_minimum_required(VERSION 3.22)
project(xgdtool_android)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED True)

if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Release CACHE STRING "" FORCE)
endif()

# Prefix where zstd / lz4 / OpenSSL / curl were cross-compiled for arm64-v8a
if(NOT XGD_DEPS_PREFIX)
    set(XGD_DEPS_PREFIX "$ENV{HOME}/android/install-arm64")
endif()
if(NOT XGD_NLOHMANN_INCLUDE)
    set(XGD_NLOHMANN_INCLUDE "$ENV{HOME}/android/external")
endif()

set(REPO_ROOT ${CMAKE_CURRENT_SOURCE_DIR}/..)
set(SRC_DIR ${REPO_ROOT}/src)
set(CMAKE_SOURCE_DIR ${REPO_ROOT}) # embed_resources.cmake expects the repo root here
include(${REPO_ROOT}/cmake/embed_resources.cmake)

add_subdirectory(${REPO_ROOT}/external/ZArchive ${CMAKE_CURRENT_BINARY_DIR}/zarchive)

set(CORE_SRC
    ${SRC_DIR}/XGDException.cpp
    # Upstream's XGDLog.cpp (desktop CLI/wxWidgets sink) was removed along
    # with the rest of the desktop build; android-src/XGDLog_JNI.cpp is the
    # only XGDLog sink implementation in this Android-only fork.

    ${SRC_DIR}/InputHelper/InputHelper.cpp
    ${SRC_DIR}/InputHelper/InputHelper_FS.cpp

    ${SRC_DIR}/ImageReader/ImageReader.cpp
    ${SRC_DIR}/ImageReader/XisoReader/XisoReader.cpp
    ${SRC_DIR}/ImageReader/GoDReader/GoDReader.cpp
    ${SRC_DIR}/ImageReader/CCIReader/CCIReader.cpp
    ${SRC_DIR}/ImageReader/CSOReader/CSOReader.cpp

    ${SRC_DIR}/ImageWriter/ImageWriter.cpp
    ${SRC_DIR}/ImageWriter/XisoWriter/XisoWriter.cpp
    ${SRC_DIR}/ImageWriter/GoDWriter/GoDWriter.cpp
    ${SRC_DIR}/ImageWriter/CCIWriter/CCIWriter.cpp
    ${SRC_DIR}/ImageWriter/ZARWriter/ZARWriter.cpp
    ${SRC_DIR}/ImageWriter/CSOWriter/CSOWriter.cpp

    ${SRC_DIR}/ImageExtractor/ImageExtractor.cpp

    ${SRC_DIR}/ZARExtractor/ZARExtractor.cpp

    ${SRC_DIR}/Executable/ExeTool.cpp
    ${SRC_DIR}/Executable/AttachXbeTool.cpp

    ${SRC_DIR}/TitleHelper/TitleHelper.cpp

    ${SRC_DIR}/AvlTree/AvlTree.cpp
    ${SRC_DIR}/AvlTree/AvlTree_Calculate.cpp
    ${SRC_DIR}/AvlTree/AvlTree_Traverse.cpp
    ${SRC_DIR}/AvlTree/AvlTree_Create.cpp
    ${SRC_DIR}/AvlTree/AvlIterator.cpp

    ${SRC_DIR}/SplitFStream/SplitIFStream.cpp
    ${SRC_DIR}/SplitFStream/SplitOFStream.cpp

    ${SRC_DIR}/Utils/EndianUtils.cpp
    ${SRC_DIR}/Utils/StringUtils.cpp

    ${SRC_DIR}/Formats/Xiso.cpp
)

set(ANDROID_GLUE_SRC
    ${CMAKE_CURRENT_SOURCE_DIR}/../android-src/XGDLog_JNI.cpp
    ${CMAKE_CURRENT_SOURCE_DIR}/../android-src/xgd_jni.cpp
)

add_library(xgdtool SHARED
    ${CORE_SRC}
    ${ANDROID_GLUE_SRC}
)

target_include_directories(xgdtool PRIVATE
    ${SRC_DIR}
    ${XGD_NLOHMANN_INCLUDE}
    ${XGD_DEPS_PREFIX}/include
)

find_library(LZ4_LIB NAMES lz4 PATHS ${XGD_DEPS_PREFIX}/lib NO_DEFAULT_PATH)
find_library(ZSTD_LIB NAMES zstd PATHS ${XGD_DEPS_PREFIX}/lib NO_DEFAULT_PATH)
find_library(CURL_LIB NAMES curl PATHS ${XGD_DEPS_PREFIX}/lib NO_DEFAULT_PATH)
find_library(SSL_LIB NAMES ssl PATHS ${XGD_DEPS_PREFIX}/lib NO_DEFAULT_PATH)
find_library(CRYPTO_LIB NAMES crypto PATHS ${XGD_DEPS_PREFIX}/lib NO_DEFAULT_PATH)

target_link_libraries(xgdtool PRIVATE
    zarchive
    ${CURL_LIB}
    ${SSL_LIB}
    ${CRYPTO_LIB}
    ${ZSTD_LIB}
    ${LZ4_LIB}
    log
)

target_compile_definitions(xgdtool PRIVATE ANDROID_BUILD=1)
