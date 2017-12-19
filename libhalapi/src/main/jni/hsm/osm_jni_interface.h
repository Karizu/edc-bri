/*
 * osm_jni_interface.h
 *
 *  Created on: 2013-3-29
 *      Author: s990902
 */

#ifndef OSM_JNI_INTERFACE_H_
#define OSM_JNI_INTERFACE_H_

const char* osm_get_class_name_cloudpos();
const char* osm_get_class_name_wizarpos();
const char* get_class_name_internal();
JNINativeMethod* osm_get_methods(int* pCount);

#endif /* OSM_JNI_INTERFACE_H_ */
