#include <fcntl.h>
#include <dlfcn.h>
#include <unistd.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "emv_kernal_interface.h"
#include "emv_kernal_jni_interface.h"
#include <android/log.h>
#include "hal_sys_log.h"

const char* g_pJNIREG_CLASS_INTERNAL = "com/wizarpos/internal/jniinterface/EMVKernelInterface";

const char* g_pJNIREG_CLASS = "com/cloudpos/jniinterface/EMVKernelInterface";

//const char* g_pLIB          = "libEMVKernal.so";//libCloudposSDK_EMVKernal.so
const char* g_pLIB          = "/data/cloudpossdk/libs/libCloudposSDK_EMVKernal.so";

EMV_KERNEL_INSTANCE* g_emv_kernel_instance = NULL;

JavaVM *g_jvm2 = NULL;
jobject g_obj2 = NULL;
jclass g_cls = NULL;

static void detachThread() {
	g_jvm2->DetachCurrentThread();
}

jbyte native_load(JNIEnv * env, jclass obj)
{
	if (g_emv_kernel_instance == NULL)
	{
		void* pHandle = dlopen(g_pLIB, RTLD_LAZY);
		if (!pHandle)
		{
			hal_sys_error("can't open emv kernel: %s\n", dlerror());
			return -2;
		}

		g_emv_kernel_instance = new EMV_KERNEL_INSTANCE();
		g_emv_kernel_instance->pHandle = pHandle;

		const char * funcName;
		if (// card functions
			   NULL == (g_emv_kernel_instance->open_reader = (OPEN_READER)dlsym(pHandle, funcName = "open_reader"))
			|| NULL == (g_emv_kernel_instance->close_reader = (CLOSE_READER)dlsym(pHandle, funcName = "close_reader"))
			|| NULL == (g_emv_kernel_instance->get_card_type = (GET_CARD_TYPE)dlsym(pHandle, funcName = "get_card_type"))
			|| NULL == (g_emv_kernel_instance->get_card_atr = (GET_CARD_ATR)dlsym(pHandle, funcName = "get_card_atr"))
			|| NULL == (g_emv_kernel_instance->transmit_card = (TRANSMIT_CARD)dlsym(pHandle, funcName = "transmit_card"))
			// EMV Functions
			|| NULL == (g_emv_kernel_instance->emv_kernel_initialize = (EMV_KERNEL_INITIALIZE)dlsym(pHandle, funcName = "emv_kernel_initialize"))
			|| NULL == (g_emv_kernel_instance->emv_is_tag_present = (EMV_IS_TAG_PRESENT)dlsym(pHandle, funcName = "emv_is_tag_present"))
			|| NULL == (g_emv_kernel_instance->emv_get_tag_data = (EMV_GET_TAG_DATA)dlsym(pHandle, funcName = "emv_get_tag_data"))
			|| NULL == (g_emv_kernel_instance->emv_get_tag_list_data = (EMV_GET_TAG_LIST_DATA)dlsym(pHandle, funcName = "emv_get_tag_list_data"))
			|| NULL == (g_emv_kernel_instance->emv_set_tag_data = (EMV_SET_TAG_DATA)dlsym(pHandle, funcName = "emv_set_tag_data"))
			|| NULL == (g_emv_kernel_instance->emv_preprocess_qpboc = (EMV_PREPROCESS_QPBOC)dlsym(pHandle, funcName = "emv_preprocess_qpboc"))
			|| NULL == (g_emv_kernel_instance->emv_trans_initialize = (EMV_TRANS_INITIALIZE)dlsym(pHandle, funcName = "emv_trans_initialize"))
			|| NULL == (g_emv_kernel_instance->emv_get_version_string = (EMV_GET_VERSION_STRING)dlsym(pHandle, funcName = "emv_get_version_string"))
			|| NULL == (g_emv_kernel_instance->emv_set_trans_amount = (EMV_SET_TRANS_AMOUNT)dlsym(pHandle, funcName = "emv_set_trans_amount"))
			|| NULL == (g_emv_kernel_instance->emv_set_other_amount = (EMV_SET_OTHER_AMOUNT)dlsym(pHandle, funcName = "emv_set_other_amount"))
			|| NULL == (g_emv_kernel_instance->emv_set_trans_type = (EMV_SET_TRANS_TYPE)dlsym(pHandle, funcName = "emv_set_trans_type"))
			|| NULL == (g_emv_kernel_instance->emv_set_kernel_type = (EMV_SET_KERNEL_TYPE)dlsym(pHandle, funcName = "emv_set_kernel_type"))
			|| NULL == (g_emv_kernel_instance->emv_process_next = (EMV_PROCESS_NEXT)dlsym(pHandle, funcName = "emv_process_next"))
			|| NULL == (g_emv_kernel_instance->emv_is_need_advice = (EMV_IS_NEED_ADVICE)dlsym(pHandle, funcName = "emv_is_need_advice"))
			|| NULL == (g_emv_kernel_instance->emv_is_need_signature = (EMV_IS_NEED_SIGNATURE)dlsym(pHandle, funcName = "emv_is_need_signature"))
			|| NULL == (g_emv_kernel_instance->emv_set_force_online = (EMV_SET_FORCE_ONLINE)dlsym(pHandle, funcName = "emv_set_force_online"))
			|| NULL == (g_emv_kernel_instance->emv_get_card_record = (EMV_GET_CARD_RECORD)dlsym(pHandle, funcName = "emv_get_card_record"))
			|| NULL == (g_emv_kernel_instance->emv_get_candidate_list = (EMV_GET_CANDIDATE_LIST)dlsym(pHandle, funcName = "emv_get_candidate_list"))
			|| NULL == (g_emv_kernel_instance->emv_set_candidate_list_result = (EMV_SET_CANDIDATE_LIST_RESULT)dlsym(pHandle, funcName = "emv_set_candidate_list_result"))
			|| NULL == (g_emv_kernel_instance->emv_set_id_check_result = (EMV_SET_ID_CHECK_RESULT)dlsym(pHandle, funcName = "emv_set_id_check_result"))
			|| NULL == (g_emv_kernel_instance->emv_set_online_pin_entered = (EMV_SET_ONLINE_PIN_ENTERED)dlsym(pHandle, funcName = "emv_set_online_pin_entered"))
			|| NULL == (g_emv_kernel_instance->emv_set_pin_bypass_confirmed = (EMV_SET_PIN_BYPASS_CONFIRMED)dlsym(pHandle, funcName = "emv_set_pin_bypass_confirmed"))
			|| NULL == (g_emv_kernel_instance->emv_set_online_result = (EMV_SET_ONLINE_RESULT)dlsym(pHandle, funcName = "emv_set_online_result"))
			|| NULL == (g_emv_kernel_instance->emv_aidparam_clear = (EMV_AIDPARAM_CLEAR)dlsym(pHandle, funcName = "emv_aidparam_clear"))
			|| NULL == (g_emv_kernel_instance->emv_aidparam_add = (EMV_AIDPARAM_ADD)dlsym(pHandle, funcName = "emv_aidparam_add"))
			|| NULL == (g_emv_kernel_instance->emv_capkparam_clear = (EMV_CAPKPARAM_CLEAR)dlsym(pHandle, funcName = "emv_capkparam_clear"))
			|| NULL == (g_emv_kernel_instance->emv_capkparam_add = (EMV_CAPKPARAM_ADD)dlsym(pHandle, funcName = "emv_capkparam_add"))
			|| NULL == (g_emv_kernel_instance->emv_terminal_param_set = (EMV_TERMINAL_PARAM_SET)dlsym(pHandle, funcName = "emv_terminal_param_set"))
			|| NULL == (g_emv_kernel_instance->emv_exception_file_clear = (EMV_EXCEPTION_FILE_CLEAR)dlsym(pHandle, funcName = "emv_exception_file_clear"))
			|| NULL == (g_emv_kernel_instance->emv_exception_file_add = (EMV_EXCEPTION_FILE_ADD)dlsym(pHandle, funcName = "emv_exception_file_add"))
			|| NULL == (g_emv_kernel_instance->emv_revoked_cert_clear = (EMV_REVOKED_CERT_CLEAR)dlsym(pHandle, funcName = "emv_revoked_cert_clear"))
			|| NULL == (g_emv_kernel_instance->emv_revoked_cert_add = (EMV_REVOKED_CERT_ADD)dlsym(pHandle, funcName = "emv_revoked_cert_add"))
		) {
			hal_sys_error("can't open %s: %s\n", funcName, strerror(errno));
			return -1;
		}
		g_emv_kernel_instance->emv_get_kernel_type = (EMV_GET_KERNEL_TYPE)dlsym(pHandle, funcName = "emv_get_kernel_type");
		g_emv_kernel_instance->open_reader_ex = (OPEN_READER_EX)dlsym(pHandle, funcName = "open_reader_ex");
		g_emv_kernel_instance->emv_reload_upcash_balance = (EMV_RELOAD_UPCASH_BALANCE)dlsym(pHandle, funcName = "emv_reload_upcash_balance");
		g_emv_kernel_instance->emv_set_fastest_qpboc_process = (EMV_SET_FASTEST_QPBOC_PROCESS)dlsym(pHandle, funcName = "emv_set_fastest_qpboc_process");
	}

	g_emv_kernel_instance->g_jni_env = env;
	g_emv_kernel_instance->g_jni_obj = obj;
	env->GetJavaVM(&g_jvm2);
	g_obj2 = env->NewGlobalRef(obj);
	jclass cls = env->FindClass(g_pJNIREG_CLASS);
	if (cls == NULL) {
		env->ExceptionClear();
		hal_sys_error("Find class [%s] failed .....", g_pJNIREG_CLASS);
		cls = env->FindClass(g_pJNIREG_CLASS_INTERNAL);
	}

	g_cls = reinterpret_cast<jclass>(env->NewGlobalRef(cls));
	if (g_cls == NULL) {
		hal_sys_error("FindClass() Error.....");	// 为何此时未返回示错代码??? DuanCS@[20150901]
	}

	env->DeleteLocalRef(cls);
	return 0;
}

jbyte native_close(JNIEnv * env, jclass obj)
{
	int nResult = -1;
	if (g_emv_kernel_instance != NULL) {
		nResult = 0;
		dlclose(g_emv_kernel_instance->pHandle);
		delete g_emv_kernel_instance;
		g_emv_kernel_instance = NULL;
	}
	return nResult;
}

// 回调函数
static void emvProcessCallback(uchar* data)
{
	JNIEnv *env;
	jboolean isAttached = JNI_FALSE;
	if (g_jvm2->GetEnv((void **)&env, JNI_VERSION_1_6) < 0) {
		if (g_jvm2->AttachCurrentThread(&env, NULL) < 0) {
			hal_sys_error("%s: AttachCurrentThread() failed", __FUNCTION__);
			return;
		}
		hal_sys_info("+emvProcessCallback()");
		isAttached = JNI_TRUE;
	}

	jmethodID method = env->GetStaticMethodID(g_cls, "emvProcessCallback", "([B)V");
	if (env->ExceptionCheck()) {
		hal_sys_error("jni can't find java emvProcessCallback");
//		return;	// 除BUG: 未 detachThread()! DuanCS@[20150906]
	} else {
		jbyteArray aryBuffer = env->NewByteArray(2);
		char * aryChar = (char*)env->GetByteArrayElements(aryBuffer, 0);
		aryChar[0] = data[0];
		aryChar[1] = data[1];
		env->ReleaseByteArrayElements(aryBuffer, (jbyte*) aryChar, 0);

		env->CallStaticVoidMethod( g_cls, method, aryBuffer);
		if (env->ExceptionCheck()) {
			hal_sys_error("jni can't call java emvProcessCallback");
		}
	}

	if(isAttached) {
		hal_sys_info("-emvProcessCallback()");
		detachThread();
	}
}

static void cardEventOccured(int eventType)
{
	JNIEnv *env;
	if (g_jvm2->AttachCurrentThread(&env, NULL) != JNI_OK) {
		hal_sys_error("%s: AttachCurrentThread() failed", __FUNCTION__);
		return;
	}
	hal_sys_info("+cardEventOccured()");
	jmethodID method = env->GetStaticMethodID(g_cls, "cardEventOccured", "(I)V");
	if (env->ExceptionCheck()) {
		hal_sys_error("jni can't find java cardEventOccured");
	} else {
		env->CallStaticVoidMethod( g_cls, method,eventType);
		if (env->ExceptionCheck()) {
			hal_sys_error("jni can't call java cardEventOccured");
		}
	}
	hal_sys_info("-cardEventOccured()");
	detachThread();
}

// card functions
jint native_open_reader_ex(JNIEnv * env, jclass obj, jint reader, jint extraParam)
{
	if (   g_emv_kernel_instance == NULL
		|| g_emv_kernel_instance->open_reader_ex == NULL
	   )
	{
		return -1;
	}
	return g_emv_kernel_instance->open_reader_ex(reader, extraParam);
}

jint native_open_reader(JNIEnv * env, jclass obj, jint reader)
{
//	return native_open_reader_ex(env, obj, reader, 0);	// 确实等价吗??? DuanCS@[20150901]
	if (g_emv_kernel_instance == NULL) {
		if (g_emv_kernel_instance->open_reader == NULL) {
			hal_sys_info("jni invoke g_emv_kernel_instance->open_reader null\n");
		}
		return 0;	// 0 代表成功还是失败 ??? DuanCS@[20150901]
	}
	return g_emv_kernel_instance->open_reader(reader);
}

void native_close_reader(JNIEnv *env, jclass obj, jint reader)
{
	g_emv_kernel_instance->close_reader(reader);
}

jint native_get_card_type(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->get_card_type();
}

jint native_get_card_atr(JNIEnv *env, jclass obj, jbyteArray atr)
{
	jbyte* bData = env->GetByteArrayElements(atr, NULL);
	jint iResult = g_emv_kernel_instance->get_card_atr((uchar *)bData);
	env->ReleaseByteArrayElements(atr, bData, 0);
	return iResult;
}

jint native_transmit_card(JNIEnv *env, jclass obj, jbyteArray cmd, jint cmdLength, jbyteArray respData, jint respDataLength)
{
	jbyte* bCmd = env->GetByteArrayElements(cmd, NULL);
	jbyte* bRespData = env->GetByteArrayElements(respData, NULL);
	jint iResult = g_emv_kernel_instance->transmit_card((uchar *)bCmd, cmdLength, (uchar*)bRespData, respDataLength);
	env->ReleaseByteArrayElements(cmd, bCmd, 0);
	env->ReleaseByteArrayElements(respData, bRespData, 0);
	return iResult;
}

// emv functions
void native_emv_kernel_initialize(JNIEnv *env, jclass obj)
{
	EMV_INIT_PARAM initParam;
	memset(&initParam,0,sizeof(initParam));
	// 初始化回调函数
	initParam.pCardEventOccured = (CARD_EVENT_OCCURED)cardEventOccured;
	initParam.pEMVProcessCallback = (EMV_PROCESS_CALLBACK)emvProcessCallback;
	g_emv_kernel_instance->emv_kernel_initialize(&initParam);
	return;
}

// 1
jint native_emv_is_tag_present(JNIEnv *env, jclass obj, jint tag)
{
	return g_emv_kernel_instance->emv_is_tag_present(tag);
}

// 2
jint native_emv_get_tag_data(JNIEnv *env, jclass obj, jint tag, jbyteArray data, jint dataLength)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_get_tag_data(tag, (uchar*)bData,dataLength);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

// 3
jint native_emv_get_tag_list_data(JNIEnv *env, jclass obj, jintArray tagList, jint tagCount, jbyteArray data, jint dataLength)
{
	jint* iTagList = env->GetIntArrayElements(tagList, NULL);
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_get_tag_list_data((int*)iTagList, tagCount, (uchar*)bData, dataLength);
	env->ReleaseIntArrayElements(tagList, iTagList, 0);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

// 4
jint native_emv_set_tag_data(JNIEnv *env, jclass obj, jint tag, jbyteArray data, jint dataLength)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_set_tag_data(tag, (uchar*)bData, dataLength);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

// 5
jint native_emv_preprocess_qpboc(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_preprocess_qpboc();
}

// 6
void native_emv_trans_initialize(JNIEnv *env, jclass obj)
{
	EMV_INIT_PARAM initParam;
	memset(&initParam,0,sizeof(initParam));
	// 初始化回调函数
	initParam.pEMVProcessCallback = (EMV_PROCESS_CALLBACK)emvProcessCallback;
	g_emv_kernel_instance->emv_trans_initialize();
}

// 7
jint native_emv_get_version_string(JNIEnv *env, jclass obj, jbyteArray data, jint dataLength)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_get_version_string((uchar*)bData, dataLength);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

// 8
jint native_emv_set_trans_amount(JNIEnv *env, jclass obj, jbyteArray amount)
{
	jbyte* bAmount = env->GetByteArrayElements(amount, NULL);
	jint iResult = g_emv_kernel_instance->emv_set_trans_amount((uchar*)bAmount);
	env->ReleaseByteArrayElements(amount, bAmount, 0);
	return iResult;
}

// 9
jint native_emv_set_other_amount(JNIEnv *env, jclass obj, jbyteArray amount)
{
	jbyte* bAmount = env->GetByteArrayElements(amount, NULL);
	jint iResult = g_emv_kernel_instance->emv_set_other_amount((uchar*)bAmount);
	env->ReleaseByteArrayElements(amount, bAmount, 0);
	return iResult;
}

// 10
jint native_emv_set_trans_type(JNIEnv *env, jclass obj, jbyte transType)
{
	return g_emv_kernel_instance->emv_set_trans_type(transType);
}

// 11
jint native_emv_set_kernel_type(JNIEnv *env, jclass obj, jbyte kernelType)
{
	return g_emv_kernel_instance->emv_set_kernel_type(kernelType);
}

// 12
jint native_emv_get_kernel_type(JNIEnv *env, jclass obj)
{
	if (   g_emv_kernel_instance == NULL
		|| g_emv_kernel_instance->emv_get_kernel_type == NULL
	   )
	{
		return -1;
	}
	return g_emv_kernel_instance->emv_get_kernel_type();
}

// 13
jint native_emv_process_next(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_process_next();
}

// 14
jint native_emv_is_need_advice(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_is_need_advice();
}

// 15
jint native_emv_is_need_signature(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_is_need_signature();
}

// 16
jint native_emv_set_force_online(JNIEnv *env, jclass obj, jint flag)
{
	return g_emv_kernel_instance->emv_set_force_online(flag);
}

// 17
jint native_emv_get_card_record(JNIEnv *env, jclass obj, jbyteArray data, jint dataLength)
{
	jbyte* bData = env->GetByteArrayElements(data,NULL);
	jint iResult = g_emv_kernel_instance->emv_get_card_record((uchar*)bData, dataLength);
	env->ReleaseByteArrayElements(data,bData,0);
	return iResult;
}

// 18
jint native_emv_get_candidate_list(JNIEnv *env, jclass obj, jbyteArray data, jint dataLength)
{
	jbyte* bData = env->GetByteArrayElements(data,NULL);
	jint iResult = g_emv_kernel_instance->emv_get_candidate_list((uchar*)bData, dataLength);
	env->ReleaseByteArrayElements(data,bData,0);
	return iResult;
}

// 19
jint native_emv_set_candidate_list_result(JNIEnv *env, jclass obj, jint index)
{
	return g_emv_kernel_instance->emv_set_candidate_list_result(index);
}

// 20
jint native_emv_set_id_check_result(JNIEnv *env, jclass obj, jint result)
{
	return g_emv_kernel_instance->emv_set_id_check_result(result);
}

// 21
jint native_emv_set_online_pin_entered(JNIEnv *env, jclass obj, jint result)
{
	return g_emv_kernel_instance->emv_set_online_pin_entered(result);
}

// 22
jint native_emv_set_pin_bypass_confirmed(JNIEnv *env, jclass obj, jint result)
{
	return g_emv_kernel_instance->emv_set_pin_bypass_confirmed(result);
}

// 23
jint native_emv_set_online_result(JNIEnv *env, jclass obj, jint result, jbyteArray respCode, jbyteArray issuerRespData, jint issuerRespDataLength)
{
	jint iResult = -1;
	jbyte* bRespCode = env->GetByteArrayElements(respCode, NULL);
	if (issuerRespData == NULL || issuerRespDataLength == 0) {
		iResult = g_emv_kernel_instance->emv_set_online_result(result, (uchar *)bRespCode, NULL, 0);
	} else {
		jbyte* bIssuerRespData = env->GetByteArrayElements(issuerRespData, NULL);
		iResult = g_emv_kernel_instance->emv_set_online_result(result, (uchar *)bRespCode, (uchar *)bIssuerRespData, issuerRespDataLength);
		env->ReleaseByteArrayElements(issuerRespData, bIssuerRespData, 0);
	}
	env->ReleaseByteArrayElements(respCode, bRespCode, 0);
	return iResult;
}

// 24
jint native_emv_aidparam_clear(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_aidparam_clear();
}

// 25
jint native_emv_aidparam_add(JNIEnv *env, jclass obj, jbyteArray data, jint dataLength)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_aidparam_add((uchar *)bData, dataLength);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

// 26
jint native_emv_capkparam_clear(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_capkparam_clear();
}

// 27
jint native_emv_capkparam_add(JNIEnv *env, jclass obj, jbyteArray data, jint dataLength)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_capkparam_add((uchar *)bData, dataLength);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

// 28
jint native_emv_terminal_param_set(JNIEnv *env, jclass obj, jbyteArray data)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_terminal_param_set((uchar *)bData);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

// 30
jint native_emv_exception_file_clear(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_exception_file_clear();
}

// 31
jint native_emv_exception_file_add(JNIEnv *env, jclass obj, jbyteArray data)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_exception_file_add((uchar *)bData);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

// 32
jint native_emv_revoked_cert_clear(JNIEnv *env, jclass obj)
{
	return g_emv_kernel_instance->emv_revoked_cert_clear();
}

// 33
jint native_emv_revoked_cert_add(JNIEnv *env, jclass obj, jbyteArray data)
{
	jbyte* bData = env->GetByteArrayElements(data, NULL);
	jint iResult = g_emv_kernel_instance->emv_revoked_cert_add((uchar *)bData);
	env->ReleaseByteArrayElements(data, bData, 0);
	return iResult;
}

// 34
jint native_emv_reload_upcash_balance(JNIEnv *env, jclass obj)
{
	if (   g_emv_kernel_instance == NULL
		|| g_emv_kernel_instance->emv_reload_upcash_balance == NULL
	   )
	{
		return -1;
	}
	return g_emv_kernel_instance->emv_reload_upcash_balance();
}

// 35
jint native_emv_set_fastest_qpboc_process(JNIEnv *env, jclass obj, jint result)
{
	if(   g_emv_kernel_instance == NULL
	   || g_emv_kernel_instance->emv_set_fastest_qpboc_process == NULL
	  )
	{
		return -1;
	}
	return g_emv_kernel_instance->emv_set_fastest_qpboc_process(result);
}

static JNINativeMethod g_Methods[] =
{
	{"loadEMVKernel",					"()B",				(void*)native_load},
	{"exitEMVKernel",					"()B",				(void*)native_close},
	// card functions
	{"open_reader",   					"(I)I",			    (void*)native_open_reader},
	{"open_reader_ex",   				"(II)I",			(void*)native_open_reader_ex},
	{"close_reader",					"(I)V",				(void*)native_close_reader},
	{"get_card_type",					"()I",				(void*)native_get_card_type},
	{"get_card_atr",					"([B)I",	        (void*)native_get_card_atr},
	{"transmit_card",					"([BI[BI)I",	    (void*)native_transmit_card},
	// emv Functions
	{"emv_kernel_initialize",		   	"()V",	       	    (void*)native_emv_kernel_initialize},   // 0
	{"emv_is_tag_present",				"(I)I",				(void*)native_emv_is_tag_present},		// 1
	{"emv_get_tag_data",				"(I[BI)I",			(void*)native_emv_get_tag_data},		// 2
	{"emv_get_tag_list_data",			"([II[BI)I",		(void*)native_emv_get_tag_list_data},	// 3
	{"emv_set_tag_data",				"(I[BI)I",			(void*)native_emv_set_tag_data},		// 4
	{"emv_preprocess_qpboc",			"()I",	        	(void*)native_emv_preprocess_qpboc},    // 5
	{"emv_trans_initialize",		   	"()V",	       	    (void*)native_emv_trans_initialize},    // 6
	{"emv_get_version_string",			"([BI)I",			(void*)native_emv_get_version_string},  // 7
	{"emv_set_trans_amount",			"([B)I",			(void*)native_emv_set_trans_amount},    // 8
	{"emv_set_other_amount",			"([B)I",			(void*)native_emv_set_other_amount},    // 9
	{"emv_set_trans_type",				"(B)I",				(void*)native_emv_set_trans_type},      // 10
	{"emv_set_kernel_type",				"(B)I",				(void*)native_emv_set_kernel_type},     // 11
	{"emv_get_kernel_type",				"()I",				(void*)native_emv_get_kernel_type},     // 12
	{"emv_process_next",				"()I",	            (void*)native_emv_process_next},        // 13
	{"emv_is_need_advice",		    	"()I",				(void*)native_emv_is_need_advice},      // 14
	{"emv_is_need_signature",			"()I",				(void*)native_emv_is_need_signature},   // 15
	{"emv_set_force_online",			"(I)I",				(void*)native_emv_set_force_online},    // 16
	{"emv_get_card_record",		    	"([BI)I",			(void*)native_emv_get_card_record},     // 17
	{"emv_get_candidate_list",	    	"([BI)I",	        (void*)native_emv_get_candidate_list},  // 18
	{"emv_set_candidate_list_result",	"(I)I",	            (void*)native_emv_set_candidate_list_result},  // 19
	{"emv_set_id_check_result",		    "(I)I",				(void*)native_emv_set_id_check_result},        // 20
	{"emv_set_online_pin_entered",	    "(I)I",	            (void*)native_emv_set_online_pin_entered},     // 21
	{"emv_set_pin_bypass_confirmed",	"(I)I",	            (void*)native_emv_set_pin_bypass_confirmed},   // 22
	{"emv_set_online_result",	        "(I[B[BI)I",	    (void*)native_emv_set_online_result},          // 23
	{"emv_aidparam_clear",				"()I",				(void*)native_emv_aidparam_clear},             // 24
	{"emv_aidparam_add",				"([BI)I",			(void*)native_emv_aidparam_add},               // 25
	{"emv_capkparam_clear",				"()I",				(void*)native_emv_capkparam_clear},            // 26
	{"emv_capkparam_add",				"([BI)I",			(void*)native_emv_capkparam_add},              // 27
	{"emv_terminal_param_set",			"([B)I",	        (void*)native_emv_terminal_param_set},         // 28

	{"emv_exception_file_clear",		"()I",				(void*)native_emv_exception_file_clear},       // 30
	{"emv_exception_file_add",			"([B)I",		    (void*)native_emv_exception_file_add},         // 31
	{"emv_revoked_cert_clear",			"()I",				(void*)native_emv_revoked_cert_clear},         // 32
	{"emv_revoked_cert_add",			"([B)I",		    (void*)native_emv_revoked_cert_add},           // 33
	{"emv_reload_upcash_balance",		"()I",				(void*)native_emv_reload_upcash_balance},      // 34
	{"emv_set_fastest_qpboc_process",	"(I)I",				(void*)native_emv_set_fastest_qpboc_process}   // 35
};

const char* get_internal_class_name()
{
	return g_pJNIREG_CLASS_INTERNAL;
}
const char* get_class_name()
{
	return g_pJNIREG_CLASS;
}


JNINativeMethod* emv_kernal_get_methods(int* pCount)
{
	*pCount = sizeof(g_Methods) /sizeof(g_Methods[0]);
	return g_Methods;
}
