package com.nursena.fenlab_android.data.repository

import com.nursena.fenlab_android.core.base.BaseRepository
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.api.*
import com.nursena.fenlab_android.data.remote.dto.request.*
import com.nursena.fenlab_android.data.remote.mapper.toDomain
import com.nursena.fenlab_android.domain.model.*
import com.nursena.fenlab_android.domain.repository.*
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
) : BaseRepository(), AuthRepository {

    override suspend fun login(request: LoginRequest): ApiResult<Pair<String, User>> =
        try {
            val response = authApi.login(request)
            ApiResult.Success(Pair(response.token, response.user.toDomain()))
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            val message = when (e.code()) {
                401 -> "Kullanıcı adı veya şifre hatalı."
                400 -> "Geçersiz istek."
                500 -> "Sunucu hatası. Lütfen daha sonra tekrar deneyin."
                else -> "Beklenmeyen bir hata oluştu. (${e.code()})"
            }
            ApiResult.Error(message = message, code = e.code())
        } catch (e: IOException) {
            ApiResult.Error(message = "İnternet bağlantınızı kontrol edin.")
        } catch (e: Exception) {
            ApiResult.Error(message = e.message ?: "Bilinmeyen bir hata oluştu.")
        }

    override suspend fun register(request: RegisterRequest): ApiResult<Pair<String, User>> =
        safeApiCall {
            val response = authApi.register(request)
            Pair(response.token, response.user.toDomain())
        }
}