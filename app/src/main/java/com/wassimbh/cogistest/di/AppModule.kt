package com.wassimbh.cogistest.di

import android.content.Context
import androidx.room.Room
import com.wassimbh.cogistest.BuildConfig
import com.wassimbh.cogistest.api.ApiService
import com.wassimbh.cogistest.api.CustomInterceptor
import com.wassimbh.cogistest.dao.PoiDao
import com.wassimbh.cogistest.data.AppDatabase
import com.wassimbh.cogistest.utilities.Constants
import com.wassimbh.cogistest.utilities.ResourcesProvider
import com.wassimbh.cogistest.utilities.SharedPreferencesProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import java.util.concurrent.TimeUnit


/**
 * Dagger module that will provide and describe how should the app
 * initialize its dependencies before injection
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    /**
     * Provide the app database
     * @param context which represents the application class
     * @return AppDatabase instance of singleton db used across the app
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, Constants.DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }


    /**
     * Provide PoiDao
     * @param db AppDatabase
     * @return a singleton PoiDao instance
     */
    @Provides
    @Singleton
    fun providePoiDao(db: AppDatabase):PoiDao{
        return db.getPoiDao()
    }

    /**
     * Provide the api base url
     * @return base url that will be used for the api calls
     */
    @Provides
    fun provideBaseUrl() = BuildConfig.BASE_URL

    /**
     * Provide an OkHttpClient
     * @return OkHttpClient singleton instance
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val okhttpBuilder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            okhttpBuilder.addInterceptor(loggingInterceptor)
        }
        okhttpBuilder.addInterceptor(CustomInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
        return okhttpBuilder.build()
    }

    /**
     * Provide retrofit singleton instance
     * @return Retrofit
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, BASE_URL: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }


    /**
     * Provide the ApiService interface that contains the api calls
     * @return ApiService
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)


    /**
     * Provide the SharedPreferencesProvider that contains the sharedPref methods
     * @return SharedPreferencesProvider
     */
    @Provides
    @Singleton
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPreferencesProvider = SharedPreferencesProvider(context)

    /**
     * Provide the ResourcesProvider that contains some methods that provides strings, drawables and context
     * @return ResourcesProvider
     */
    @Provides
    @Singleton
    fun provideResourceProvider(@ApplicationContext context: Context): ResourcesProvider = ResourcesProvider(context)
}