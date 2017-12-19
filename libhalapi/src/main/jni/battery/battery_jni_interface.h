#ifndef BATTERY_JNI_INTERFACE_H_
#define BATTERY_JNI_INTERFACE_H_

const char* battery_get_class_name();
//	+add by pengli
const char* get_class_name_internal();
//	-add by pengli
JNINativeMethod* battery_get_methods(int* pCount);

#endif
