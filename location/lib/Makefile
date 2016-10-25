SOURCE   = kht_source
CPP      = g++
OBJ      = ${SOURCE}/buffer_2d.o ${SOURCE}/jni_kernel_hough.o ${SOURCE}/kht.o ${SOURCE}/linking.o ${SOURCE}/peak_detection.o ${SOURCE}/subdivision.o ${SOURCE}/voting.o ${SOURCE}/eigen.o

INCLUDES  = -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/linux"
BIN      = ./kht-jni
CXXFLAGS = ${INCLUDES} -DBUILDING_DLL=1 -fPIC
RM       = rm -f
DEF      = libkht-jni.def
STATIC   = libkht-jni.a

.PHONY: all all-before all-after clean clean-custom

all: all-before ${BIN} all-after

clean: clean-custom
	${RM} ${OBJ} ${BIN} ${DEF} ${STATIC}

${BIN}: ${OBJ}
	${CPP} -m32 -shared ${OBJ} -o ${BIN} 

../kht_sandbox/kht_source/buffer_2d.o: ../kht_sandbox/kht_source/buffer_2d.cpp
	${CPP} -c ../kht_sandbox/kht_source/buffer_2d.cpp -o ../kht_sandbox/kht_source/buffer_2d.o ${CXXFLAGS}

../kht_sandbox/kht_source/jni_kernel_hough.o: ../kht_sandbox/kht_source/jni_kernel_hough.cpp
	${CPP} -c ../kht_sandbox/kht_source/jni_kernel_hough.cpp -o ../kht_sandbox/kht_source/jni_kernel_hough.o ${CXXFLAGS}

../kht_sandbox/kht_source/kht.o: ../kht_sandbox/kht_source/kht.cpp
	${CPP} -c ../kht_sandbox/kht_source/kht.cpp -o ../kht_sandbox/kht_source/kht.o ${CXXFLAGS}

../kht_sandbox/kht_source/linking.o: ../kht_sandbox/kht_source/linking.cpp
	${CPP} -c ../kht_sandbox/kht_source/linking.cpp -o ../kht_sandbox/kht_source/linking.o ${CXXFLAGS}

../kht_sandbox/kht_source/peak_detection.o: ../kht_sandbox/kht_source/peak_detection.cpp
	${CPP} -c ../kht_sandbox/kht_source/peak_detection.cpp -o ../kht_sandbox/kht_source/peak_detection.o ${CXXFLAGS}

../kht_sandbox/kht_source/subdivision.o: ../kht_sandbox/kht_source/subdivision.cpp
	${CPP} -c ../kht_sandbox/kht_source/subdivision.cpp -o ../kht_sandbox/kht_source/subdivision.o ${CXXFLAGS}

../kht_sandbox/kht_source/voting.o: ../kht_sandbox/kht_source/voting.cpp
	${CPP} -c ../kht_sandbox/kht_source/voting.cpp -o ../kht_sandbox/kht_source/voting.o ${CXXFLAGS}

../kht_sandbox/kht_source/eigen.o: ../kht_sandbox/kht_source/eigen.cpp
	${CPP} -c ../kht_sandbox/kht_source/eigen.cpp -o ../kht_sandbox/kht_source/eigen.o ${CXXFLAGS}
