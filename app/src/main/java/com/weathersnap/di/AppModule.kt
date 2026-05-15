package com.weathersnap.di

import android.app.Application
import androidx.room.Room
import com.weathersnap.BuildConfig
import com.weathersnap.data.api.GeocodingApi
import com.weathersnap.data.api.WeatherApi
import com.weathersnap.data.local.AppDatabase
import com.weathersnap.data.local.ReportDao
import com.weathersnap.data.repository.ReportRepositoryImpl
import com.weathersnap.data.repository.WeatherRepositoryImpl
import com.weathersnap.domain.repository.ReportRepository
import com.weathersnap.domain.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            // Only attach verbose logging in debug builds
            if (BuildConfig.DEBUG_LOGGING) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                )
            }
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
        }.build()
    }

    @Provides
    @Singleton
    fun provideGeocodingApi(client: OkHttpClient): GeocodingApi {
        return Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeocodingApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherApi(client: OkHttpClient): WeatherApi {
        return Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "weathersnap_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideReportDao(db: AppDatabase): ReportDao = db.reportDao()

    @Provides
    @Singleton
    fun provideWeatherRepository(
        geocodingApi: GeocodingApi,
        weatherApi: WeatherApi
    ): WeatherRepository = WeatherRepositoryImpl(geocodingApi, weatherApi)

    @Provides
    @Singleton
    fun provideReportRepository(
        reportDao: ReportDao
    ): ReportRepository = ReportRepositoryImpl(reportDao)
}
