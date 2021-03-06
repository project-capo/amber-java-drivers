package pl.edu.agh.capo.hough.jni;

import org.slf4j.LoggerFactory;

public class JniKernelHough {

    static {
        try {
        	
        	//Win32
        	//System.load("C://Users//Szymon//git//amber-java-drivers//location//lib//win32_dll//kht-jni.dll");
        	
        	//Linux
        	System.load("//home//panda//amber//amber-java-drivers//location//lib//kht-jni");
        	
            LoggerFactory.getLogger(JniKernelHough.class).debug("Loaded kth-jni library");
        } catch (UnsatisfiedLinkError error) {
            LoggerFactory.getLogger(JniKernelHough.class).error("Could not load kth-jni library, did you run make.exe --file=Makefile.win inside lib directory?", error);
            System.exit(-1);
        }
    }

    public native KhtResult kht(byte[] binary_image, long image_width, long image_height, int max_lines, long cluster_min_size,
                                 double cluster_min_deviation, double delta, double kernel_min_height, double n_sigmas);

}
