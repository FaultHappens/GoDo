package com.dmtsk.godo.ui.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.google.android.gms.location.LocationServices

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {
	
	@Provides
	@Singleton
	fun provideFusedLocationProviderClient(
		@ApplicationContext context: Context
	): FusedLocationProviderClient =
		LocationServices.getFusedLocationProviderClient(context)
}