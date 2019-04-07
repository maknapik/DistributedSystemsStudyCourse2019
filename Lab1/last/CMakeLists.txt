cmake_minimum_required(VERSION 3.10)
project(last C)

set(CMAKE_C_STANDARD 99)

add_executable(last main.c)

target_link_libraries(last pthread)