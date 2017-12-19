#ifndef EMV_KERNAL_JNI_INTERFACE_H_
#define EMV_KERNAL_JNI_INTERFACE_H_

const char* get_internal_class_name();
const char* get_class_name();
JNINativeMethod* emv_kernal_get_methods(int* pCount);

#endif /* EMV_KERNAL_JNI_INTERFACE_H_ */
