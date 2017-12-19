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
#include "clone_screen_jni_interface.h"
#include "clone_screen_interface.h"

const char* g_pJNIREG_CLASS = "com/cloudpos/jniinterface/CloneScreenInterface";
//	+add by pengli
const char* g_pJNIREG_CLASS_INTERNAL =
		"com/wizarpos/internal/jniinterface/CloneScreenInterface";
//	-add by pengli

typedef struct clone_screen_interface {
	clone_screen_enable enable;
	clone_screen_show show;
	void* pHandle;
} CLONE_SCREEN_INSTANCE;

static CLONE_SCREEN_INSTANCE* g_pCloneScreenInstance = NULL;
static int ERR_NOT_OPENED = -255;
static int ERR_HAS_OPENED = -254;
static int ERR_NO_IMPLEMENT = -253;
static int ERR_INVALID_ARGUMENT = -252;
static int ERR_NORMAL = -251;

int native_clone_screen_open(JNIEnv * env, jclass obj) {
	hal_sys_info("+ native_clone_screen_open");
	int nResult = ERR_HAS_OPENED;
	if (g_pCloneScreenInstance == NULL) {
		//										  libwizarposDriver.so
		void* pHandle = dlopen("libUnionpayCloudPos.so", RTLD_LAZY);
		if (!pHandle) {
			hal_sys_error("%s\n", dlerror());
			return ERR_NORMAL;
		}
		g_pCloneScreenInstance = new CLONE_SCREEN_INSTANCE();
		g_pCloneScreenInstance->pHandle = pHandle;
		char * methodName;
		if (NULL == (g_pCloneScreenInstance->enable = (clone_screen_enable) dlsym(pHandle, methodName = "clone_screen_enable"))
				|| NULL == (g_pCloneScreenInstance->show = (clone_screen_show) dlsym(pHandle, methodName = "clone_screen_show"))) {
			hal_sys_error("can't find %s", methodName);
			nResult = ERR_NO_IMPLEMENT;
			goto fingerprint_init_clean;
		}
		nResult = g_pCloneScreenInstance->enable(false);

		if (nResult < 0) {
			goto fingerprint_init_clean;
		}
	}
	hal_sys_info("- native_clone_screen_open, result = %d", nResult);
	return nResult;

	fingerprint_init_clean:
		hal_sys_info("clone_screen_init_clean");
		dlclose(g_pCloneScreenInstance->pHandle);
		delete g_pCloneScreenInstance;
		g_pCloneScreenInstance = NULL;
		hal_sys_info("- native_clone_screen_open, result = %d", nResult);
	return nResult;
}

int native_clone_screen_close(JNIEnv * env, jclass obj) {
	hal_sys_info("+ native_clone_screen_close");
	int nResult = ERR_NORMAL;
	if (g_pCloneScreenInstance == NULL)
		return ERR_NOT_OPENED;
	nResult = g_pCloneScreenInstance->enable(true);
	dlclose(g_pCloneScreenInstance->pHandle);
	delete g_pCloneScreenInstance;
	g_pCloneScreenInstance = NULL;
	hal_sys_info("- native_clone_screen_close, result = %d", nResult);
	return nResult;
}

int native_clone_screen_show(JNIEnv * env, jclass obj, jintArray arryBitmap,
		jint nBitmapLength, jint nBitmapWidth, jint nBitmapHeight) {
	hal_sys_info("+ native_clone_screen_show");
	int nResult = ERR_NORMAL;
	if (g_pCloneScreenInstance == NULL)
		return ERR_NOT_OPENED;

	jint* pBitmapBuffer = env->GetIntArrayElements(arryBitmap, NULL);
	nResult = g_pCloneScreenInstance->show(pBitmapBuffer, nBitmapLength, nBitmapWidth, nBitmapHeight);
	env->ReleaseIntArrayElements(arryBitmap, pBitmapBuffer, 0);
	hal_sys_info("- native_clone_screen_show, result = %d", nResult);
	return nResult;
}

static JNINativeMethod g_Methods[] = {
		{ "open", 				"()I",					(void*) native_clone_screen_open },
		{ "close", 				"()I",					(void*) native_clone_screen_close },
		{ "show", 				"([IIII)I",				(void*) native_clone_screen_show },};

const char* get_class_name() {
	return g_pJNIREG_CLASS;
}
const char* get_class_name_internal() {
	return g_pJNIREG_CLASS_INTERNAL;
}

JNINativeMethod* get_methods(int* pCount) {
	*pCount = sizeof(g_Methods) / sizeof(g_Methods[0]);
	return g_Methods;
}
