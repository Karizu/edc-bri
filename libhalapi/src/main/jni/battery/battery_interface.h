/*************************************************************************
 > File Name: fingerprint_interface.h
 > Author:
 > Mail:
 > Created Time: Fri 15 Jan 2016 09:31:10 AM CST
 ************************************************************************/

#ifndef _BATTERY_INTERFACE_H
#define _BATTERY_INTERFACE_H
#ifdef __cplusplus
extern "C" {
#endif

typedef int (*battery_open)(void);

typedef int (*battery_close)(void);

typedef int (*battery_query_info)(int *capacity, int *voltage);

#ifdef __cplusplus
}
#endif
#endif
