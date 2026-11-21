package com.zaurh.notefirelocal.core.di

import javax.inject.Qualifier

/** A scope that outlives screens: used for writes that must finish after a screen closes. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
