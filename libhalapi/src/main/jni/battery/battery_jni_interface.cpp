#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <dlfcn.h>
#include <semaphore.h>
#include <unistd.h>
#include <errno.h>
#include <pthread.h>
#include <jni.h>

#include <time.h>

#include "hal_sys_log.h"
#include "battery_jni_interface.h"
#include "battery_interface.h"

const char* g_pJNIREG_CLASS = "com/cloudpos/jniinterface/BatteryInterface";
//	+add by pengli
const char* g_pJNIREG_CLASS_INTERNAL =
		"com/wizarpos/internal/jniinterface/BatteryInterface";
//	-add by pengli

typedef struct battery_interface {
	battery_open open;
	battery_close close;
	battery_query_info query_info;
	void* pHandle;
} BATTERY_INSTANCE;

static BATTERY_INSTANCE* g_pBatteryInstance = NULL;
static int ERR_NOT_OPENED = -255;
static int ERR_HAS_OPENED = -254;
static int ERR_NO_IMPLEMENT = -253;
static int ERR_INVALID_ARGUMENT = -252;
static int ERR_NORMAL = -251;

int native_battery_open(JNIEnv * env, jclass obj) {
	hal_sys_info("+ native_battery_open");
	int nResult = ERR_HAS_OPENED;
	if (g_pBatteryInstance == NULL) {
		void* pHandle = dlopen("libUnionpayCloudPos.so", RTLD_LAZY);
		if (!pHandle) {
			hal_sys_error("%s\n", dlerror());
			return ERR_NORMAL;
		}
		g_pBatteryInstance = new BATTERY_INSTANCE();
		g_pBatteryInstance->pHandle = pHandle;
		char * methodName;
		if (NULL == (g_pBatteryInstance->open = (battery_open) dlsym(pHandle, methodName = "printer_open"))
				|| NULL == (g_pBatteryInstance->close = (battery_close) dlsym(pHandle, methodName = "printer_close"))
				|| NULL == (g_pBatteryInstance->query_info = (battery_query_info) dlsym(pHandle, methodName = "printer_query_voltage"))) {
			hal_sys_error("can't find %s", methodName);
			nResult = ERR_NO_IMPLEMENT;
		} else {
			nResult = g_pBatteryInstance->open();
		}

	}
	hal_sys_info("- native_battery_open, result = %d", nResult);
	return nResult;
}

int native_battery_close(JNIEnv * env, jclass obj) {
	hal_sys_info("+ native_battery_close");
	int nResult = ERR_NORMAL;
	if (g_pBatteryInstance == NULL)
		return ERR_NOT_OPENED;
	nResult = g_pBatteryInstance->close();
	dlclose(g_pBatteryInstance->pHandle);
	delete g_pBatteryInstance;
	g_pBatteryInstance = NULL;
	hal_sys_info("- native_battery_close, result = %d", nResult);
	return nResult;
}

int native_battery_query_info(JNIEnv * env, jclass obj, jintArray capacity, jintArray voltage) {
	hal_sys_info("+ native_battery_query_info");
	int nResult = ERR_NORMAL;
	if (g_pBatteryInstance == NULL)
		return ERR_NOT_OPENED;
	if(g_pBatteryInstance->query_info == NULL)
		return ERR_NO_IMPLEMENT;

	jint* nCapacity = env->GetIntArrayElements(capacity, NULL);
	jint* nVoltage = env->GetIntArrayElements(voltage, NULL);
	nResult = g_pBatteryInstance->query_info(nCapacity, nVoltage);
	env->ReleaseIntArrayElements(capacity, nCapacity, 0);
	env->ReleaseIntArrayElements(voltage, nVoltage, 0);
	hal_sys_info("- native_battery_query_info, result = %d", nResult);
	return nResult;
}

static JNINativeMethod g_Methods[] = {
		{ "open", 				"()I",					(void*) native_battery_open },
		{ "close", 				"()I",					(void*) native_battery_close },
		{ "queryInfo", 			"([I[I)I",				(void*) native_battery_query_info },};

const char* battery_get_class_name() {
	return g_pJNIREG_CLASS;
}
const char* get_class_name_internal() {
	return g_pJNIREG_CLASS_INTERNAL;
}

JNINativeMethod* battery_get_methods(int* pCount) {
	*pCount = sizeof(g_Methods) / sizeof(g_Methods[0]);
	return g_Methods;
}
