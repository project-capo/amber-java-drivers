#include <jni.h>
#include <iterator>
#include "kht.h"
#include "jni_kernel_hough.h"

unsigned char* as_unsigned_char_array(JNIEnv * env, jbyteArray array) {
    int len = env->GetArrayLength (array);
    unsigned char* buf = new unsigned char[len];
    env->GetByteArrayRegion (array, 0, len, reinterpret_cast<jbyte*>(buf));
    return buf;
}

jobject create_section(JNIEnv * env, section_t closest_section) {
	jclass cls = env->FindClass("java/util/ArrayList");
	jmethodID constructor = env->GetMethodID(cls, "<init>", "()V");
	jmethodID add = env->GetMethodID(cls, "add", "(Ljava/lang/Object;)Z");
	jclass section_cls = env->FindClass("pl/edu/agh/capo/common/Section");
    jmethodID section_constructor = env->GetMethodID(section_cls, "<init>", "(DD[Lpl/edu/agh/capo/maze/Coordinates;)V");
    jclass coordinates_cls = env->FindClass("pl/edu/agh/capo/maze/Coordinates");
    jmethodID coordinates_constructor =  env->GetMethodID(coordinates_cls, "<init>", "()V");
    jmethodID coordinates_set_x =  env->GetMethodID(coordinates_cls, "setX", "(D)V");
    jmethodID coordinates_set_y =  env->GetMethodID(coordinates_cls, "setY", "(D)V");
	jobjectArray coordinates_array = env->NewObjectArray(closest_section.size, coordinates_cls, NULL);
	for (int k = 0; k < closest_section.size; k++){
		pixel_t pixel = closest_section.pixels[k];
		jobject coordinates = env->NewObject(coordinates_cls, coordinates_constructor);
		env->CallVoidMethod(coordinates, coordinates_set_x, pixel.x);
		env->CallVoidMethod(coordinates, coordinates_set_y, pixel.y);
		env->SetObjectArrayElement(coordinates_array,k, coordinates);
	}
	jobject jsection = env->NewObject(section_cls, section_constructor, closest_section.rho, closest_section.theta, coordinates_array);
	return jsection;
}

jobject get_sections(JNIEnv * env, line_t line, section_list_t &sections) {
	jclass cls = env->FindClass("java/util/ArrayList");
	jmethodID constructor = env->GetMethodID(cls, "<init>", "()V");
	jmethodID add = env->GetMethodID(cls, "add", "(Ljava/lang/Object;)Z");
	jclass section_cls = env->FindClass("pl/edu/agh/capo/common/Section");
    jmethodID section_constructor = env->GetMethodID(section_cls, "<init>", "(DD[Lpl/edu/agh/capo/maze/Coordinates;)V");
    jclass coordinates_cls = env->FindClass("pl/edu/agh/capo/maze/Coordinates");
    jmethodID coordinates_constructor =  env->GetMethodID(coordinates_cls, "<init>", "()V");
    jmethodID coordinates_set_x =  env->GetMethodID(coordinates_cls, "setX", "(D)V");
    jmethodID coordinates_set_y =  env->GetMethodID(coordinates_cls, "setY", "(D)V");

	section_t closest_section;
	double min_theta_difference = 181;
	//todo: zrobić to lepiej z oboma parametrami
	for (int i = 0; i < sections.size(); i++) {
		section_t &section = sections[i];
		double theta_difference = abs(section.theta - line.theta);
		if (theta_difference < min_theta_difference) {
		     min_theta_difference = theta_difference;
		     closest_section = section;
		}
	}

	jobjectArray coordinates_array = env->NewObjectArray(closest_section.size, coordinates_cls, NULL);
    for (int k = 0; k < closest_section.size; k++){
        pixel_t pixel = closest_section.pixels[k];
        jobject coordinates = env->NewObject(coordinates_cls, coordinates_constructor);
        env->CallVoidMethod(coordinates, coordinates_set_x, pixel.x);
        env->CallVoidMethod(coordinates, coordinates_set_y, pixel.y);
        env->SetObjectArrayElement(coordinates_array,k, coordinates);
    }
    jobject jsection = env->NewObject(section_cls, section_constructor, closest_section.rho, closest_section.theta, coordinates_array);
	return jsection;
}

jobject as_line_list(JNIEnv * env,lines_list_t lines, section_list_t &sections, int max_lines){
	jclass cls = env->FindClass("java/util/ArrayList");
	jmethodID constructor = env->GetMethodID(cls, "<init>", "()V");
    jobject list = env->NewObject(cls, constructor);
    jmethodID add = env->GetMethodID(cls, "add", "(Ljava/lang/Object;)Z");
    
    jclass line_cls = env->FindClass("pl/edu/agh/capo/common/Line");
    jmethodID line_constructor = env->GetMethodID(line_cls, "<init>", "(DD)V");

     int end = lines.size();
     if (end > max_lines) {
         end = max_lines;
     }
   	 for (size_t k=0; k!=end; ++k){
		const line_t &line = lines[k];
		jobject jline = env->NewObject(line_cls, line_constructor, line.theta, line.rho);
		env->CallVoidMethod(list, add, jline);
	}

	jobject section_list = env->NewObject(cls, constructor);
	for (size_t k=0; k!= sections.size(); ++k) {
	    const section_t &section = sections[k];
	    jobject jsection = create_section(env, section);
	    env->CallVoidMethod(section_list, add, jsection);
	}

	jclass result_cls = env->FindClass("pl/edu/agh/capo/hough/jni/KhtResult");
	jmethodID result_constructor = env->GetMethodID(result_cls, "<init>", "(Ljava/util/List;Ljava/util/List;)V");
    jobject result = env->NewObject(result_cls, result_constructor, list, section_list);

	return result;
}

JNIEXPORT jobject JNICALL Java_pl_edu_agh_capo_hough_jni_JniKernelHough_kht
  (JNIEnv * env, jobject method, jbyteArray binary_image, jlong image_width, jlong image_height, jint max_size, jlong cluster_min_size, jdouble cluster_min_deviation,
  jdouble delta, jdouble kernel_min_height, jdouble n_sigmas) {
  	
  	lines_list_t* lines = new lines_list_t();
  	section_list_t* sections = new section_list_t();
  	kht((*lines), (*sections), as_unsigned_char_array(env, binary_image), image_width, image_height, cluster_min_size, cluster_min_deviation, delta,
          kernel_min_height, n_sigmas);

    return as_line_list(env, *lines, (*sections), max_size);
}

