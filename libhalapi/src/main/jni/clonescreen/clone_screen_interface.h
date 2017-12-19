/*************************************************************************
	> File Name: clone_screen.h
	> Author: 
	> Mail: 
	> Created Time: Wed 06 Jul 2016 12:42:50 PM CST
 ************************************************************************/

#ifndef _CLONE_SCREEN_H
#define _CLONE_SCREEN_H
#ifdef __cplusplus
extern "C"
{
#endif

typedef int (*clone_screen_enable)(int nEnable);

typedef int (*clone_screen_show)(int *pImg, int nImgLength, unsigned int nWidth, unsigned int nHeight);

#ifdef __cplusplus
}
#endif
#endif
