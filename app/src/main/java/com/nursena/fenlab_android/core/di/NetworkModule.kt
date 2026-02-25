package com.nursena.fenlab_android.core.di

import com.nursena.fenlab_android.core.network.AuthInterceptor
import com.nursena.fenlab_android.core.network.RetrofitClient
import com.nursena.fenlab_android.data.remote.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        RetrofitClient.buildOkHttpClient(authInterceptor)

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        RetrofitClient.buildRetrofit(okHttpClient)

    @Provides @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides @Singleton
    fun provideExperimentApi(retrofit: Retrofit): ExperimentApi =
        retrofit.create(ExperimentApi::class.java)

    @Provides @Singleton
    fun provideCommentApi(retrofit: Retrofit): CommentApi =
        retrofit.create(CommentApi::class.java)

    @Provides @Singleton
    fun provideQuestionApi(retrofit: Retrofit): QuestionApi =
        retrofit.create(QuestionApi::class.java)

    @Provides @Singleton
    fun provideFavoriteApi(retrofit: Retrofit): FavoriteApi =
        retrofit.create(FavoriteApi::class.java)

    @Provides @Singleton
    fun provideRatingApi(retrofit: Retrofit): RatingApi =
        retrofit.create(RatingApi::class.java)

    @Provides @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi =
        retrofit.create(NotificationApi::class.java)

    @Provides @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Provides @Singleton
    fun provideFileUploadApi(retrofit: Retrofit): FileUploadApi =
        retrofit.create(FileUploadApi::class.java)

    @Provides @Singleton
    fun providePdfApi(retrofit: Retrofit): PdfApi =
        retrofit.create(PdfApi::class.java)
}

