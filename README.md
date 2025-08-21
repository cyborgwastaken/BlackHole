# **black**_**hole**

Black hole simulation project

Here is the black hole raw code, everything will be inside a src bin incase you want to copy the files

I'm writing this as I'm beginning this project (hopefully I complete it ;D) here is what I plan to do:

1. Ray-tracing : add ray tracing to the gravity simulation to simulate gravitational lensing

2. Accretion disk : simulate accreciate disk using the ray tracing + the halos

3. Spacetime curvature : demonstrate visually the "trapdoor in spacetime" that is black holes using spacetime grid

4. [optional] try to make it run realtime ;D

I hope it works :/

Edit: After completion of project -

Thank you everyone for checking out the video, if you haven't it explains code in detail: https://www.youtube.com/watch?v=8-B6ryuBkCM

## **Building Requirements:**

1. C++ Compiler supporting C++ 17 or newer

2. [Cmake](https://cmake.org/)

3. [Vcpkg](https://vcpkg.io/en/)

4. [Git](https://git-scm.com/)

## **Build Instructions:**

This project supports two build systems: **CMake** (recommended for C++ development) and **Maven** (for compatibility).

### **Option 1: CMake Build (Recommended)**

1. Clone the repository:
	-  `git clone https://github.com/cyborgwastaken/BlackHole.git`
2. CD into the newly cloned directory
	- `cd ./BlackHole` 
3. Install dependencies with Vcpkg
	- `vcpkg install`
4. Get the vcpkg cmake toolchain file path
	- `vcpkg integrate install`
	- This will output something like : `CMake projects should use: "-DCMAKE_TOOLCHAIN_FILE=/path/to/vcpkg/scripts/buildsystems/vcpkg.cmake"`
5. Create a build directory
	- `mkdir build`
6. Configure project with CMake
	-  `cmake -B build -S . -DCMAKE_TOOLCHAIN_FILE=/path/to/vcpkg/scripts/buildsystems/vcpkg.cmake`
	- Use the vcpkg cmake toolchain path from above
7. Build the project
	- `cmake --build build`
8. Run the program
	- The executables will be located in the build folder

### **Option 2: Maven Build (Alternative)**

If you prefer using Maven, this project includes a pom.xml that wraps the CMake build process:

1. Clone the repository:
	- `git clone https://github.com/cyborgwastaken/BlackHole.git`
2. CD into the newly cloned directory
	- `cd ./BlackHole`
3. Install dependencies with Vcpkg (same as above)
	- `vcpkg install`
4. Build with Maven:
	- `mvn clean compile`
	- Or with vcpkg integration: `mvn clean compile -Dvcpkg.toolchain.file=/path/to/vcpkg/scripts/buildsystems/vcpkg.cmake`
5. Clean build artifacts:
	- `mvn clean`

**Note:** Both build methods require the same dependencies (GLEW, GLFW3, GLM) to be installed via vcpkg first.

## **How the code works:**
for 2D: simple, just run 2D_lensing.cpp with the nessesary dependencies installed.

for 3D: black_hole.cpp and geodesic.comp work together to run the simuation faster using GPU, essentially it sends over a UBO and geodesic.comp runs heavy calculations using that data.

should work with nessesary dependencies installed, however I have only run it on windows with my GPU so am not sure!

LMK if you would like an in-depth explanation of how the code works aswell :)
