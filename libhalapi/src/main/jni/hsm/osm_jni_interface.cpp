/*
 * osm_jni_interface.cpp
 *
 *  Created on: 2013-3-29
 *      Author: s990902
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <dlfcn.h>
#include <semaphore.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <jni.h>

#include "hal_sys_log.h"
#include "osm_jni_interface.h"
#include "object_storage_manager.h"

const char* g_pJNIREG_CLASS_WIZARPOS = "com/wizarpos/devices/jniinterface/HSMInterface";//com.cloudpos.jniinterface
const char* g_pJNIREG_CLASS_INTERNAL = "com/wizarpos/internal/jniinterface/HSMInterface";
//com.cloudpos.jniinterface
const char* g_pJNIREG_CLASS_CLOUDPOS = "com/cloudpos/jniinterface/HSMInterface";


#define FIND_METHOD_FROM_SO(pointer, type, name) 				\
	do															\
	{															\
		g_pOSMInstance->pointer = (type)dlsym(pHandle, name); 	\
		if(g_pOSMInstance->pointer == NULL )						\
		{														\
			hal_sys_error("Failed to find" name);	\
			if(name == "hsm_osm_generate_pinpad_csr" || name == "hsm_osm_enable_sensor" || name == "hsm_osm_reset" || name == "hsm_osm_save_object" || name == "hsm_osm_delete_object" ){\
				hal_sys_error("System is not support this method : " name);\
			}else	\
				goto native_hsm_osm_open_clean;						\
		}														\
	}while(0);


typedef struct HSM_OSM_interface
{
	HSM_OSM_OPEN						open;
	HSM_OSM_CLOSE						close;
	HSM_OSM_SAVE_OBJECT					saveUnionPayPrivateKey;
	HSM_OSM_DELETE_OBJECT				deleteUnionPayPrivateKey;
	/*******************************************/
	HSM_OSM_QUERY_STATUS				isTampered;
	HSM_OSM_GENERATE_KEYPAIR			generateKeyPair;
	HSM_OSM_STORE_PUBKEY_CERT			injectPublicKeyCertificate;
	HSM_OSM_STORE_ROOT_CERT				injectRootCertificate;
	HSM_OSM_RESET						resetSaveModule;

	HSM_OSM_PRIVATE_KEY_ENCRYPT_DECRYPT	privateKeyCalculation;
	HSM_OSM_GET_CERT					getCertificate;

	HSM_OSM_QUERY_CERT_LABELS			queryCertLabels;
	HSM_OSM_QUERY_CERT_COUNT			queryCertCount;
	HSM_OSM_QUERY_PRIVATE_KEY_LABELS	queryPrivateKeyLabels;
	HSM_OSM_QUERY_PRIVATE_KEY_COUNT		queryPrivateKeyCount;

	HSM_OSM_DELETE_CERT					deleteCertificate;

	HSM_OSM_DELETE_PRIVATE_KEY			deleteKeyPair;
	HSM_OSM_GET_PUBLIC_KEY				getPublicKey;
	HSM_OSM_GET_RANDOM					getRandom;
	HSM_OSM_GENERATE_CSR				generateCSR;
	HSM_OSM_GENERATE_PINPAD_CSR			generatePinpadCSR;
	HSM_OSM_ENABLE_SENSOR				enableSensor;
	void* pSoHandle;
}HSM_OSM_INSTANCE;



static HSM_OSM_INSTANCE* g_pOSMInstance = NULL;

int native_hsm_osm_open(JNIEnv* env, jclass obj)
{
	int nResult = -1;
	hal_sys_info("native_hsm_osm_open() is called");
	if(g_pOSMInstance == NULL)
	{
		void* pHandle = dlopen("libPKCS11Wrapper.so", RTLD_LAZY);
		if (!pHandle)
		{
			hal_sys_error("%s\n", dlerror());
			return -1;
		}

		g_pOSMInstance = new HSM_OSM_INSTANCE();
		FIND_METHOD_FROM_SO(open, HSM_OSM_OPEN, "hsm_osm_open");
		FIND_METHOD_FROM_SO(close, HSM_OSM_CLOSE, "hsm_osm_close");
		FIND_METHOD_FROM_SO(saveUnionPayPrivateKey, HSM_OSM_SAVE_OBJECT, "hsm_osm_save_object");
		FIND_METHOD_FROM_SO(deleteUnionPayPrivateKey, HSM_OSM_DELETE_OBJECT, "hsm_osm_delete_object");
//		FIND_METHOD_FROM_SO(delete_all, HSM_OSM_DELETE_ALL, "hsm_osm_delete_all");

//		******************************************************************************
		FIND_METHOD_FROM_SO(isTampered, HSM_OSM_QUERY_STATUS, "hsm_osm_query_status");
		FIND_METHOD_FROM_SO(generateKeyPair, HSM_OSM_GENERATE_KEYPAIR, "hsm_osm_generate_keypair");
		FIND_METHOD_FROM_SO(injectPublicKeyCertificate, HSM_OSM_STORE_PUBKEY_CERT, "hsm_osm_store_pubkey_cert");
		FIND_METHOD_FROM_SO(injectRootCertificate, HSM_OSM_STORE_ROOT_CERT, "hsm_osm_store_root_cert");
		FIND_METHOD_FROM_SO(resetSaveModule, HSM_OSM_RESET, "hsm_osm_reset");

		FIND_METHOD_FROM_SO(privateKeyCalculation, HSM_OSM_PRIVATE_KEY_ENCRYPT_DECRYPT, "hsm_osm_private_key_encrypt_decrypt");
		FIND_METHOD_FROM_SO(getCertificate, HSM_OSM_GET_CERT, "hsm_osm_get_cert");

		FIND_METHOD_FROM_SO(queryCertLabels, HSM_OSM_QUERY_CERT_LABELS, "hsm_osm_query_cert_labels");
		FIND_METHOD_FROM_SO(queryCertCount, HSM_OSM_QUERY_CERT_COUNT, "hsm_osm_query_cert_count");

		FIND_METHOD_FROM_SO(queryPrivateKeyLabels, HSM_OSM_QUERY_PRIVATE_KEY_LABELS, "hsm_osm_query_private_key_labels");
		FIND_METHOD_FROM_SO(queryPrivateKeyCount, HSM_OSM_QUERY_PRIVATE_KEY_COUNT, "hsm_osm_query_private_key_count");


		FIND_METHOD_FROM_SO(deleteCertificate, HSM_OSM_DELETE_CERT, "hsm_osm_delete_cert");

		FIND_METHOD_FROM_SO(deleteKeyPair , HSM_OSM_DELETE_PRIVATE_KEY,"hsm_osm_delete_private_key");
		FIND_METHOD_FROM_SO(getPublicKey, HSM_OSM_GET_PUBLIC_KEY, "hsm_osm_get_public_key");
		FIND_METHOD_FROM_SO(getRandom, HSM_OSM_GET_RANDOM, "hsm_osm_get_random");

		FIND_METHOD_FROM_SO(generateCSR, HSM_OSM_GENERATE_CSR, "hsm_osm_generate_csr");

		FIND_METHOD_FROM_SO(generatePinpadCSR, HSM_OSM_GENERATE_PINPAD_CSR, "hsm_osm_generate_pinpad_csr");
//		added by pengli for Q1
		FIND_METHOD_FROM_SO(enableSensor, HSM_OSM_ENABLE_SENSOR, "hsm_osm_enable_sensor");

		g_pOSMInstance->pSoHandle = pHandle;
		nResult = g_pOSMInstance->open();
		hal_sys_info("native_hsm_osm_open return value = %d\n", nResult);
		if(nResult < 0)
			goto native_hsm_osm_open_clean;
	}
	return nResult;
native_hsm_osm_open_clean:
	if(g_pOSMInstance != NULL)
	{
		if(g_pOSMInstance->pSoHandle)
			dlclose(g_pOSMInstance->pSoHandle);
		delete g_pOSMInstance;
		g_pOSMInstance = NULL;
	}
	hal_sys_info("native_hsm_osm_open return error value = %d\n", nResult);
	return nResult;
}

int native_hsm_osm_close(JNIEnv* env, jclass obj)
{
	int nResult = 0;
	hal_sys_info("native_hsm_osm_close() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	nResult = g_pOSMInstance->close();
	if(g_pOSMInstance->pSoHandle)
		dlclose(g_pOSMInstance->pSoHandle);
	delete g_pOSMInstance;
	g_pOSMInstance = NULL;
	return nResult;
}

/*
	public byte[] strID = new byte[32];
	public byte[] strLabel = new byte[32];
	public byte[] strPassword = new byte[32];
	public int nObjectType;
*/
static const char* g_strFieldsName[] = {"strID", "strLabel", "strPassword", "nObjectType"};

static int jobject_to_cstruct(JNIEnv* env, jclass obj, jobject ObjectProperty, HSM_OBJECT_PROPERTY* pObjProperty)
{
	int nResult = -1;

	jclass clazz = env->GetObjectClass(ObjectProperty);
	if(0 == clazz)
	{
		hal_sys_error("jobject_to_cstruct(), failed in GetObjectClass()");
		return -1;
	}
	for(int i = 0; i < 3; i++)
	{
		jfieldID fid = env->GetFieldID(clazz, g_strFieldsName[i], "[B");
		jbyteArray arryField = (jbyteArray)env->GetObjectField(ObjectProperty, fid);
		jbyte* strField = env->GetByteArrayElements(arryField, 0);
		int nFieldLength = env->GetArrayLength(arryField);
		switch(i)
		{
			case 0:
				//Load fields strID
				memcpy(pObjProperty->strID, strField, nFieldLength);
				pObjProperty->strID[nFieldLength] = 0;
				break;
			case 1:
				//Load fields strLabel
				memcpy(pObjProperty->strLabel, strField, nFieldLength);
				pObjProperty->strLabel[nFieldLength] = 0;
				break;
			case 2:
				//Load fields strPassword
				memcpy(pObjProperty->strPassword, strField, nFieldLength);
				pObjProperty->strPassword[nFieldLength] = 0;
				break;
			default:
				break;
		}
		env->ReleaseByteArrayElements(arryField, strField, 0);
	}

	jfieldID fid = env->GetFieldID(clazz, g_strFieldsName[3], "I");
	pObjProperty->nObjectType = (HSM_OBJECT_TYPE)env->GetIntField(ObjectProperty, fid);


	return 0;
}

//int native_hsm_osm_save_object(JNIEnv* env, jclass obj, jobject ObjectProperty, jbyteArray byteData, jint nDataLength, jint nDataType)
//{
//	int nResult = -1;
//	HSM_OBJECT_PROPERTY CObj;
//
//	hal_sys_info("native_hsm_osm_save_object() is called");
//
//	if(g_pOSMInstance == NULL)
//		return -1;
//	nResult = jobject_to_cstruct(env, obj, ObjectProperty, &CObj);
//	if(nResult < 0)
//		return nResult;
//	hal_sys_info("CObj.strID = %s\n", CObj.strID);
//	hal_sys_info("CObj.strLable = %s\n", CObj.strLabel);
//	hal_sys_info("CObj.strPassword = %s\n", CObj.strPassword);
//	hal_sys_info("CObj.nObjectType = %d\n", CObj.nObjectType);
//
//	jbyte* strData = env->GetByteArrayElements(byteData, 0);
//	hal_sys_dump("byteData", (unsigned char*)strData, (unsigned int)nDataLength);
//	nResult = g_pOSMInstance->save_object(&CObj, (unsigned char*)strData, nDataLength, (HSM_OBJECT_DATA_TYPE)nDataType);
//	env->ReleaseByteArrayElements(byteData, strData, 0);
//    hal_sys_info("Save_object() return value = %d\n", nResult);
//	return nResult;
//}

//int native_hsm_osm_delete_object(JNIEnv* env, jclass obj, jobject ObjectProperty, jbyteArray bytePin, jint nPinLength)
//{
//	int nResult = -1;
//	HSM_OBJECT_PROPERTY CObj;
//	hal_sys_info("native_hsm_osm_delete_object() is called");
//
//	if(g_pOSMInstance == NULL)
//		return -1;
//	nResult = jobject_to_cstruct(env, obj, ObjectProperty, &CObj);
//	if(nResult < 0)
//		return nResult;
//	hal_sys_info("CObj.strID = %s\n", CObj.strID);
//	hal_sys_info("CObj.strLable = %s\n", CObj.strLabel);
//	hal_sys_info("CObj.strPassword = %s\n", CObj.strPassword);
//	hal_sys_info("CObj.nObjectType = %d\n", CObj.nObjectType);
//
//	jbyte* strPin = env->GetByteArrayElements(bytePin, 0);
//	hal_sys_dump("bytePin", (unsigned char*)strPin, (unsigned int)nPinLength);
//	nResult = g_pOSMInstance->delete_object(&CObj, (char*)strPin, nPinLength);
//	env->ReleaseByteArrayElements(bytePin, strPin, 0);
//
//	return nResult;
//}

//int native_hsm_osm_delete_all(JNIEnv* env, jclass obj, jbyteArray bytePin, jint nPinLength)
//{
//	int nResult = -1;
//
//	hal_sys_info("native_hsm_osm_delete_object() is called");
//
//	if(g_pOSMInstance == NULL)
//			return -1;
//
//	jbyte* strPin = env->GetByteArrayElements(bytePin, 0);
//	hal_sys_dump("bytePin", (unsigned char*)strPin, (unsigned int)nPinLength);
//	nResult = g_pOSMInstance->delete_all((char*)strPin, nPinLength);
//	env->ReleaseByteArrayElements(bytePin, strPin, 0);
//	return nResult;
//}
int native_hsm_osm_query_status(JNIEnv* env, jclass obj){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_query_status() is called");
	if(g_pOSMInstance == NULL)
			return -1;
	nResult = g_pOSMInstance->isTampered();
	return nResult;
}

int native_hsm_osm_generate_keypair(JNIEnv* env, jclass obj, jstring alias, jint algorithm, jint keySize){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_generate_keypair() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	if(alias == NULL){
		return -1;
	}
	char const *p_alias = env->GetStringUTFChars(alias, NULL);
	nResult = g_pOSMInstance->generateKeyPair((char*)p_alias, keySize);
	env->ReleaseStringUTFChars(alias,p_alias);
	return nResult;

}
//(char* pCertLabel, char* pPriKeyLabel, unsigned char* pObjectData, unsigned int nDataLength, HSM_OBJECT_DATA_TYPE nDataType)
int native_hsm_osm_store_pubkey_cert(JNIEnv* env, jclass obj, jstring alias, jstring aliasPrivateKey, jbyteArray bufCert, jint bufLength, jint dataFormat){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_store_pubkey_cert() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	if(alias == NULL || aliasPrivateKey == NULL || bufCert == NULL){
		return -1;
	}
	char const *p_alias = env->GetStringUTFChars(alias, NULL);
	char const *p_aliasPrivateKey= env->GetStringUTFChars(aliasPrivateKey, NULL);
	jbyte* strData = env->GetByteArrayElements(bufCert, 0);

	nResult = g_pOSMInstance->injectPublicKeyCertificate((char*)p_alias,(char*)p_aliasPrivateKey,(unsigned char*)strData,bufLength,(HSM_OBJECT_DATA_TYPE)dataFormat);

	env->ReleaseStringUTFChars(alias,p_alias);
	env->ReleaseStringUTFChars(aliasPrivateKey,p_aliasPrivateKey);
	env->ReleaseByteArrayElements(bufCert, strData, 0);
	return nResult;
}
//(unsigned int nProperty, char* pCertLabel, unsigned char* pObjectData, unsigned int nDataLength, HSM_OBJECT_DATA_TYPE nDataType);
int native_hsm_osm_store_root_cert(JNIEnv* env, jclass obj, jint certType, jstring alias,jbyteArray bufCert, jint bufLength, jint dataFormat ){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_store_root_cert() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	if(alias == NULL || bufCert == NULL){
		return -1;
	}
	char const *p_alias = env->GetStringUTFChars(alias, NULL);
	jbyte* strData = env->GetByteArrayElements(bufCert, 0);
	nResult = g_pOSMInstance->injectRootCertificate(certType,(char*)p_alias, (unsigned char*)strData, bufLength, (HSM_OBJECT_DATA_TYPE)dataFormat);
	env->ReleaseStringUTFChars(alias,p_alias);
	env->ReleaseByteArrayElements(bufCert, strData, 0);
	return nResult;
}

int native_hsm_osm_reset(JNIEnv* env, jclass obj,jstring strPin){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_reset() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	if(strPin == NULL)
		return -1;
	char const *p_alias = env->GetStringUTFChars(strPin, NULL);
	jint nPinLength = env->GetStringLength(strPin);
	nResult = g_pOSMInstance->resetSaveModule((char*)p_alias, nPinLength);
	env->ReleaseStringUTFChars(strPin,p_alias);
	return nResult;
}

//(char* privKeyLabel, unsigned int nEncDecFlag, unsigned char* pData, unsigned int nDataLength)
int native_hsm_osm_private_key_encrypt(JNIEnv* env, jclass obj, jstring alias, jbyteArray bufPlain, jbyteArray bufCert, jint bufLength){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_private_key_encrypt() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	if(alias == NULL)
		return -1;
	char const *p_alias = env->GetStringUTFChars(alias, NULL);
	jbyte* p_bufPlain = env->GetByteArrayElements(bufPlain, 0);
	jbyte* strData = env->GetByteArrayElements(bufCert, 0);
	int bufPlainLength = env->GetArrayLength(bufPlain);
	hal_sys_info("+ bufPlainLength = %d",bufPlainLength);
	nResult = g_pOSMInstance->privateKeyCalculation((char*)p_alias, 0, (unsigned char*)p_bufPlain , bufPlainLength, (unsigned char*)strData, bufLength );// encrypt
	hal_sys_info("- bufPlainLength = %d",bufPlainLength);
	env->ReleaseStringUTFChars(alias,p_alias);
	env->ReleaseByteArrayElements(bufCert, strData, 0);
	env->ReleaseByteArrayElements(bufPlain, p_bufPlain, 0);
	hal_sys_info("native_hsm_osm_private_key_encrypt nResult = %d",nResult);
	return nResult;
}
int native_hsm_osm_private_key_decrypt(JNIEnv* env, jclass obj, jstring alias, jbyteArray bufPlain, jbyteArray bufCert, jint bufLength){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_private_key_decrypt() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	if(alias == NULL)
		return -1;
	char const *p_alias = env->GetStringUTFChars(alias, NULL);
	jbyte* p_bufPlain = env->GetByteArrayElements(bufPlain, 0);
	jbyte* strData = env->GetByteArrayElements(bufCert, 0);
	int bufPlainLength = env->GetArrayLength(bufPlain);
	nResult = g_pOSMInstance->privateKeyCalculation((char*)p_alias, 1, (unsigned char*)p_bufPlain , bufPlainLength, (unsigned char*)strData, bufLength );// decrypt
	env->ReleaseStringUTFChars(alias,p_alias);
	env->ReleaseByteArrayElements(bufCert, strData, 0);
	env->ReleaseByteArrayElements(bufPlain, p_bufPlain, 0);
	return nResult;
}
//(unsigned int nProperty, char* label, unsigned char* pDataBuffer, unsigned int nDataBufferLength, HSM_OBJECT_DATA_TYPE nDataType);
int native_hsm_osm_get_cert(JNIEnv* env, jclass obj,jint certType, jstring alias, jbyteArray bufCert, jint bufLength, jint dataFormat){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_get_cert() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	if(alias == NULL)
		return -1;
	char const *p_alias = env->GetStringUTFChars(alias, NULL);
	jbyte* strData = env->GetByteArrayElements(bufCert, 0);
	nResult = g_pOSMInstance->getCertificate(certType, (char*)p_alias, (unsigned char*)strData, bufLength,(HSM_OBJECT_DATA_TYPE)dataFormat);
	env->ReleaseStringUTFChars(alias,p_alias);
	env->ReleaseByteArrayElements(bufCert, strData, 0);
	return nResult;
}

int native_hsm_osm_delete_private_keypair(JNIEnv* env, jclass obj,jstring alias){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_delete_private_keypair() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	if(alias == NULL)
		return -1;
	char const *p_alias = env->GetStringUTFChars(alias, NULL);
	nResult = g_pOSMInstance->deleteKeyPair((char*)p_alias);
	env->ReleaseStringUTFChars(alias,p_alias);
	return nResult;
}

int native_hsm_osm_get_random(JNIEnv* env, jclass obj,jbyteArray bufData, jint bufLength){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_get_random() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	jbyte* strData = env->GetByteArrayElements(bufData, 0);
	nResult = g_pOSMInstance->getRandom((unsigned char*)strData,bufLength);
	env->ReleaseByteArrayElements(bufData, strData, 0);
	return nResult;
}
int native_hsm_osm_delete_cert(JNIEnv* env, jclass obj,jint certType, jstring alias){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_delete_cert() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	if(alias == NULL)
		return -1;
	char const *p_alias = env->GetStringUTFChars(alias, NULL);
	nResult = g_pOSMInstance->deleteCertificate(certType,(char*)p_alias);
	env->ReleaseStringUTFChars(alias,p_alias);
	return nResult;
}

int native_hsm_osm_query_private_key_labels(JNIEnv* env, jclass obj,jbyteArray bufData, jint bufLength){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_query_private_key_labels() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	jbyte* strData = env->GetByteArrayElements(bufData, 0);
	nResult = g_pOSMInstance->queryPrivateKeyLabels((unsigned char*)strData,bufLength);
	env->ReleaseByteArrayElements(bufData, strData, 0);
	return nResult;
}
int native_hsm_osm_query_private_key_count(JNIEnv* env, jclass obj){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_query_private_key_count() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	nResult = g_pOSMInstance->queryPrivateKeyCount();
	return nResult;
}
int native_hsm_osm_query_cert_labels(JNIEnv* env, jclass obj, jint certType,jbyteArray bufData, jint bufLength){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_query_cert_labels() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	jbyte* strData = env->GetByteArrayElements(bufData, 0);
	nResult = g_pOSMInstance->queryCertLabels(certType,(unsigned char*)strData,bufLength);
	env->ReleaseByteArrayElements(bufData, strData, 0);
	return nResult;

}
int native_hsm_osm_query_cert_count(JNIEnv* env, jclass obj, jint certType){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_query_cert_count() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	nResult = g_pOSMInstance->queryCertCount(certType);
	return nResult;
}

//native_hsm_osm_enable_sensor
int native_hsm_osm_enable_sensor(JNIEnv* env, jclass obj, jint nSensorMask){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_enable_sensor() is called : nSensorMask = %d ", nSensorMask);
	if(g_pOSMInstance == NULL)
		return -1;
	nResult = g_pOSMInstance->enableSensor(nSensorMask);
	hal_sys_info("leave native_hsm_osm_enable_sensor(), result = %d", nResult);
	return nResult;
}

//(char* pPriKeyLabel, char* pCommonName, unsigned char* pDataBuffer, unsigned int nDataBufferLength);
int native_hsm_osm_generate_csr(JNIEnv* env, jclass obj,jstring alias, jstring commonName, jbyteArray bufData, jint bufLength){
	int nResult = -1;
	hal_sys_info("+native_hsm_osm_generate_csr() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	if(alias == NULL){
		return -1;
	}
	char const *p_commonName = "";
	if(commonName == NULL){
		p_commonName = "";
	}else{
		p_commonName = env->GetStringUTFChars(commonName, NULL);
	}
	char const *p_alias = env->GetStringUTFChars(alias, NULL);
	jbyte* strData = env->GetByteArrayElements(bufData, NULL);
	nResult = g_pOSMInstance->generateCSR((char*)p_alias,(char*)p_commonName,(unsigned char*)strData, bufLength);

	env->ReleaseStringUTFChars(alias,p_alias);
	env->ReleaseStringUTFChars(commonName,p_commonName);
	env->ReleaseByteArrayElements(bufData, strData, 0);
	hal_sys_info("- native_hsm_osm_generate_csr,result=%d", nResult);
	return nResult;
}
//native_hsm_osm_generate_pinpad_csr
int native_hsm_osm_generate_pinpad_csr(JNIEnv* env, jclass obj, jbyteArray bufData, jint bufLength){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_generate_pinpad_csr() is called");
	if(g_pOSMInstance == NULL)
		return -1;
	jbyte* strData = env->GetByteArrayElements(bufData, NULL);
	nResult = g_pOSMInstance->generatePinpadCSR((unsigned char*)strData, bufLength);
	env->ReleaseByteArrayElements(bufData, strData, 0);
	return nResult;
}
int native_hsm_osm_save_unionpay_prikey(JNIEnv* env, jclass obj, jbyteArray bufData, jint bufLength ){
	int nResult = -1;
	hal_sys_info("native_hsm_osm_save() is called %s\n" + bufLength);
	HSM_OBJECT_PROPERTY CObj;
	strcpy((char*)CObj.strLabel, "pk2048");
	strcpy((char*)CObj.strID, "client2048");
	strcpy((char*)CObj.strPassword, "wizarpos");
	CObj.nObjectType = HSM_OBJECT_TYPE_private_key;
	jbyte* strData = env->GetByteArrayElements(bufData, 0);
	hal_sys_info("CObj.strID = %s\n", CObj.strID);
	hal_sys_info("CObj.strLable = %s\n", CObj.strLabel);
	hal_sys_info("CObj.strPassword = %s\n", CObj.strPassword);
	hal_sys_info("CObj.nObjectType = %d\n", CObj.nObjectType);
	nResult = g_pOSMInstance->saveUnionPayPrivateKey(&CObj, (unsigned char*)strData, bufLength, (HSM_OBJECT_DATA_TYPE)0);
	env->ReleaseByteArrayElements(bufData, strData, 0);
	hal_sys_info("native_hsm_osm_save() end result = " + nResult);
	return nResult;
}
int native_hsm_osm_del_unionpay_prikey(JNIEnv* env, jclass obj){
	int nResult = -1;
	if(g_pOSMInstance!= NULL){
		hal_sys_info("native_hsm_osm_del_unionpay_prikey() is called \n");
		HSM_OBJECT_PROPERTY CObj;
		strcpy((char*)CObj.strLabel, "pk2048");
		strcpy((char*)CObj.strID, "client2048");
		strcpy((char*)CObj.strPassword, "wizarpos");
		CObj.nObjectType = HSM_OBJECT_TYPE_private_key;
		hal_sys_info("CObj.strID = %s\n", CObj.strID);
		hal_sys_info("CObj.strLable = %s\n", CObj.strLabel);
		hal_sys_info("CObj.strPassword = %s\n", CObj.strPassword);
		hal_sys_info("CObj.nObjectType = %d\n", CObj.nObjectType);
		char* pwd = "wizarpos";
		nResult = g_pOSMInstance->deleteUnionPayPrivateKey(&CObj, pwd, 8);
	}
	hal_sys_info("native_hsm_osm_del_unionpay_prikey() end result = " + nResult);
	return nResult;
}
/*
 * "(P1P2PN...)Return"
 */

static JNINativeMethod g_Methods[] =
{
	{"open",						"()I",												(void*)native_hsm_osm_open},
	{"close",						"()I",												(void*)native_hsm_osm_close},
	{"isTampered",					"()I",												(void*)native_hsm_osm_query_status},
	{"generateKeyPair",				"(Ljava/lang/String;II)I",							(void*)native_hsm_osm_generate_keypair},
	{"injectPublicKeyCertificate",	"(Ljava/lang/String;Ljava/lang/String;[BII)I",		(void*)native_hsm_osm_store_pubkey_cert},
	{"injectRootCertificate",		"(ILjava/lang/String;[BII)I",						(void*)native_hsm_osm_store_root_cert},

	{"doRSAEncrypt",				"(Ljava/lang/String;[B[BI)I",						(void*)native_hsm_osm_private_key_encrypt},
	{"doRSADecrypt",				"(Ljava/lang/String;[B[BI)I",						(void*)native_hsm_osm_private_key_decrypt},
	{"getCertificate",				"(ILjava/lang/String;[BII)I",						(void*)native_hsm_osm_get_cert},
	{"deleteKeyPair",				"(Ljava/lang/String;)I",							(void*)native_hsm_osm_delete_private_keypair},
	{"getRandom",					"([BI)I",											(void*)native_hsm_osm_get_random},
	{"deleteCertificate",			"(ILjava/lang/String;)I",							(void*)native_hsm_osm_delete_cert},

	{"resetSaveModule",				"(Ljava/lang/String;)I",							(void*)native_hsm_osm_reset},

	{"queryPrivateKeyLabels",		"([BI)I",											(void*)native_hsm_osm_query_private_key_labels},
	{"queryPrivateKeyCount",		"()I",												(void*)native_hsm_osm_query_private_key_count},
	{"queryCertLabels",				"(I[BI)I",											(void*)native_hsm_osm_query_cert_labels},
	{"queryCertCount",				"(I)I",												(void*)native_hsm_osm_query_cert_count},
	{"generateCSR",					"(Ljava/lang/String;Ljava/lang/String;[BI)I",		(void*)native_hsm_osm_generate_csr},
	{"generatePINPadCSR",			"([BI)I",											(void*)native_hsm_osm_generate_pinpad_csr},
	{"enableSensor",				"(I)I",												(void*)native_hsm_osm_enable_sensor},
	{"saveUnionPayPrivateKey",		"([BI)I",											(void*)native_hsm_osm_save_unionpay_prikey},
	{"deleteUnionPayPrivateKey",	"()I",												(void*)native_hsm_osm_del_unionpay_prikey}
};



const char* osm_get_class_name_wizarpos()
{
	return g_pJNIREG_CLASS_WIZARPOS;
}
const char* osm_get_class_name_cloudpos()
{
	return g_pJNIREG_CLASS_CLOUDPOS;
}
const char* get_class_name_internal()
{
	return g_pJNIREG_CLASS_INTERNAL;
}
JNINativeMethod* osm_get_methods(int* pCount)
{
	*pCount = sizeof(g_Methods) /sizeof(g_Methods[0]);
	return g_Methods;
}
