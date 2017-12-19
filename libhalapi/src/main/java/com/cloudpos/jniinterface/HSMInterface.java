/*
 * HSMInterface.java
 *
 * Copyright (c) 2013 - 2016 wizarPOS. All rights reserved.
 *
 *
 * WIZARPOS PROPRIETARY/CONFIDENTIAL.
 *
 */

package com.cloudpos.jniinterface;

import android.util.Log;


public class HSMInterface {
	private static final String TAG = "HSMInterface";
    static {
    	try {
    		System.loadLibrary("jni_cloudpos_hsm");
		} catch (Throwable e) {
			Log.e(TAG, "Can't find jni_cloudpos_hsm.so");
			e.printStackTrace();
		}
    }
    public static final int FORMAT_PEM = 0;
    public static final int FORMAT_DER = 1;
    
    public static final int CERT_TYPE_OWNER = 1;
    public static final int CERT_TYPE_PUBLIC_KEY = 2;
    public static final int CERT_TYPE_APP_ROOT = 3;
    public static final int CERT_TYPE_COMMUNICATE = 4;
    
    public static final int ALGORITHM_RSA = 1;
    /**
     * Open the device.<br>
     * Require one of the SAFE_MODULE_READONLY, SAFE_MODULE or SAFE_MODULE_RESET permission.
     *  
     * @return value  >= 0, success in starting the process; value < 0, error code
     * */
    public static synchronized native int open();
    /**
     * close the device
     * @return value  >= 0, success in starting the process; value < 0, error code
     * */
    public static synchronized native int close();
    /**
     * Check the security module is tampered or not. If the security module is tampered, all data in the security module should not be trusted.<br>
     * This method requires SAFE_MODULE_READONLY or SAFE_MODULE permission.
     * 
     * @return  0   Not tampered.<br>
     *          1   Tampered.
     */
    public static synchronized native int isTampered();
    
    /**
     * Get the real random buffer from safe module.
     * <p>
     * This method requires SAFE_MODULE_READONLY or SAFE_MODULE permission.
     * 
     * @param bufRandom     the buffer to store random bytes.
     * @param length        the length of the buffer.
     * @return  >=0   success<br>
     *          <0  error
     */
    public static synchronized native int getRandom(byte[] bufRandom, int length);

    /**
     * Request security module to generate a key pair inside the module.
     * <br>
     * This method requires SAFE_MODULE permission.
     * 
     * @param alias         the alias of the private key.
     * @param algorithm     the algorithm of the key pair. Currently, only ALGORITHM_RSA is supported.
     * @param keySize       the bit size of the key. Currently, only 2048 is supported.
     * @return      >=0 success<br>
     *              <0 the error code	
     */
    
    public static synchronized native int generateKeyPair(String alias, int algorithm, int keySize);
    /**
     * 
     * Inject the certificate of the existing key pair.
     * 
     * @param alias             the alias of the certificate.
     * @param aliasPrivateKey   the alias of the key pair, usually it's the private key's alias.
     * @param bufCert           the data of the certificate.
     * @param bufLength         the length of the data buffer.enforceCalliUpdateServicengPermission
     * @param dataFormat        the format of the buffer, Currently, only "PEM" is supported.
     * @return      >=0 success<br>
     *              <0 error code
     */
    public static synchronized native int injectPublicKeyCertificate(String alias, String aliasPrivateKey, byte[] bufCert, int bufLength, int dataFormat);

    /**
     * Inject the root certificates to security module.<br>
     * All the certificate must signed by the terminal's owner certificate.
     * The keyUsage flag must be set as define:<br>
     * <li>CERT_TYPE_OWNER certificate's keyUsage flag must be set as critical, and the KeyEncipherment, CertificateSign and CRLSign must be set, other flags are cleared.
     * <li>CERT_TYPE_APP_ROOT certificate's keyUsage flag must be set as critical, and the DigitalSignature, CertificateSign must be set, other flags are cleared.
     * <li>CERT_TYPE_COMMUNICATE certificate's keyUsage flag must be set as non-critical and DigitalSignature, KeyEncipherment, DataEncipherment must be set, other flags are cleared.
     * <br>
     * This method required SAFE_MODULE permission.
     *  
     * @param certType      the certificate type, could be CERT_TYPE_OWNER, CERT_TYPE_APP_ROOT or CERT_TYPE_COMMUNICATE.
     * @param alias         the alias of the certificate.getCertificate
     * @param bufCert       the data of the certificate.
     * @param bufLength         the length of the data buffer.
     * @param dataFormat    the format of the buffer, Currently, only FORMAT_PEM is supported.
     * @return  >=0 success<br>
     *          <0  the error code
     */
    public static synchronized native int injectRootCertificate(int certType, String alias, byte[] bufCert, int bufLength, int dataFormat);
    
    /**
     * Get the certificate data.
     * <br>
     * This method requires SAFE_MODULE_READONLY permission.
     * 
     * @param certType      the certificate type, could be CERT_TYPE_OWNER, CERT_TYPE_PUBLIC_KEY, CERT_TYPE_APP_ROOT or CERT_TYPE_COMMUNICATE.
     * @param alias         the alias of the certificate
     * @param bufCert       the output buffer to store the certificate PEM data.
     * @param bufMaxLength  the max length of the result buffer.
     * @param dataFormat    the format of the buffer, Currently, only FORMAT_PEM is supported.
     * @return      >=0     the length of the certificate PEM data.<br>
     *              <0      the error code.
     */
    
    public static synchronized native int getCertificate(int certType, String alias, byte[] bufCert, int bufMaxLength , int dataFormat);
    /**
     * Remove the certificate of the given alias.
     * <br>
     * The OWNER certificate can't be removed.
     * <br>
     * This method requires SAVE_MODULE permission.
     * 
     * @param certType      the certificate type, could be CERT_TYPE_PUBLIC_KEY, CERT_TYPE_APP_ROOT or CERT_TYPE_COMMUNICATE.
     * @param alias         the alias of the certificate
     * @return  >=0 success<br>
     *          <0  the error code
     */
    public static synchronized native int deleteCertificate(int certType, String alias);
    
    /**
     * Remove the key pair of the given alias.
     * <br>
     * This method requires SAVE_MODULE permission.
     * 
     * @param aliasPrivateKey         the alias of the private key.
     * @return  >=0 success<br>
     *          <0  the error code
     */
    public static synchronized native int deleteKeyPair(String aliasPrivateKey);
    
    /**
     * Generate the CSR for given private key.
     * <br>
     * This method requires SAVE_MODULE permission.
     * 
     * @param alias         the alias of the private key
     * 
     * @param aliasPrivateKey   the alias of the private key
     * @param commonName        the DN of the commonName
     * @param bufResult         the buffer to store the CSR data.
     * @param resMaxLength      the max length of the result buffer.
     * 
     * @return  >=0 success, with the valid length of the bufResult.<br>
     *          <0  the error code
     */
    public static synchronized native int generateCSR(String aliasPrivateKey, String commName, byte[] bufResult, int resMaxLength);

    /**
     * Do encryption by the given private key. The result data is in PKCS#1 padding format.
     * <br>
     * This method requires SAFE_MODULE permission.
     * 
     * @param aliasPrivateKey   the alias of the given private key.
     * @param bufPlain          the buffer of the plain data. 
     * @param bufResult         the buffer for the output cipher data.
     * @param resMaxLength      the max length of the output buffer.
     * @return      >=0 encrypt success and return the length of the bufResult.<br>
     *              <0 the error code.
     */
    public static synchronized native int doRSAEncrypt(String aliasPrivateKey, byte[] bufPlain, byte[] bufResult, int resMaxLength);
    
    public static synchronized native int doRSADecrypt(String aliasPrivateKey, byte[] bufCipher, byte[] bufResult, int resMaxLength);    
    
    public static synchronized native int queryPrivateKeyLabels(byte[] bufLabels, int length);
    
    public static synchronized native int queryPrivateKeyCount();
    
    public static synchronized native int queryCertLabels(int certType, byte[] bufLabels, int length);
    
    public static synchronized native int queryCertCount(int certType);
    
    public static synchronized native int resetSaveModule(String pwd);
    
    public static synchronized native int generatePINPadCSR( byte[] bufResult, int length);
    
    public static synchronized native int enableSensor(int key);
    
    public static synchronized native int saveUnionPayPrivateKey(byte[] byteData, int nDataLength);
    
    public static synchronized native int deleteUnionPayPrivateKey();
    
}
