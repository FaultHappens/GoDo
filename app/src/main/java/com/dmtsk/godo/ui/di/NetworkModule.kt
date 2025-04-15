package com.dmtsk.godo.ui.di

import com.dmtsk.godo.BuildConfig
import com.dmtsk.godo.data.network.apiservice.GoogleMapsApiService
import com.dmtsk.godo.data.repository.GoogleMapsRepositoryImpl
import com.dmtsk.godo.domain.repository.GoogleMapsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {
	
	@Provides
	@Singleton
	fun providerRetrofit() : Retrofit
	{
		val client = OkHttpClient.Builder()
			.addInterceptor(MapsApiKeyInterceptor())
			.addInterceptor(HttpLoggingInterceptor().apply {
				level = HttpLoggingInterceptor.Level.BODY // Log body, headers, etc.
			})
			.build()
		return Retrofit.Builder()
			.baseUrl("https://maps.googleapis.com/maps/api/")
			.addConverterFactory(GsonConverterFactory.create())
			.client(client)
			.build()
	}
	
	@Provides
	@Singleton
	fun providerGoogleMapsApiService(retrofit: Retrofit) : GoogleMapsApiService {
		return retrofit.create(GoogleMapsApiService::class.java)
	}
	
	@Provides
	@Singleton
	fun providerGoogleMapsRepositoryImpl(apiService: GoogleMapsApiService) : GoogleMapsRepository
	{
		return GoogleMapsRepositoryImpl(apiService)
	}
	
}

class MapsApiKeyInterceptor : Interceptor
{
	override fun intercept(chain: Interceptor.Chain): Response
	{
		val original = chain.request()
		val originalUrl = original.url
		
		val newUrl = originalUrl.newBuilder()
			.addQueryParameter("key", BuildConfig.MAPS_API_KEY)
			.build()
		
		val newRequest = original.newBuilder()
			.url(newUrl)
			.build()
		
		return chain.proceed(newRequest)
	}
}