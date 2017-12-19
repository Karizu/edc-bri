#ifndef _HSM_OBJECT_PROPERTY_H
#define _HSM_OBJECT_PROPERTY_H

typedef enum Object_data_type
{
	HSM_OBJECT_DATA_TYPE_pem,
	HSM_OBJECT_DATA_TYPE_der,
	HSM_OBJECT_DATA_TYPE_p7b,
	HSM_OBJECT_DATA_TYPE_pfx
}HSM_OBJECT_DATA_TYPE;

typedef enum object_type_
{
	HSM_OBJECT_TYPE_private_key,
	HSM_OBJECT_TYPE_public_key,
	HSM_OBJECT_TYPE_cert
}HSM_OBJECT_TYPE;

typedef struct object_proptery_
{
	unsigned char strID[32];
	unsigned char strLabel[32];
	unsigned char strPassword[32];
	HSM_OBJECT_TYPE nObjectType;
}HSM_OBJECT_PROPERTY;

#endif
