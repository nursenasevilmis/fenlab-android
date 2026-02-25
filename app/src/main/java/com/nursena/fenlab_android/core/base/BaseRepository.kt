package com.nursena.fenlab_android.core.base

import com.nursena.fenlab_android.core.network.ApiResult
import retrofit2.HttpException
import java.io.IOException

abstract class BaseRepository {

    protected suspend fun <T> safeApiCall(
        call: suspend () -> T
    ): ApiResult<T> = try {
        ApiResult.Success(call())

    } catch (e: HttpException) {
        val message = when (e.code()) {
            400 -> "Geçersiz istek."
            401 -> "Oturum süreniz doldu. Lütfen tekrar giriş yapın."
            403 -> "Bu işlem için yetkiniz yok."
            404 -> "İstenen içerik bulunamadı."
            409 -> "Bu işlem zaten gerçekleştirilmiş."
            422 -> "Gönderilen veriler geçersiz."
            500 -> "Sunucu hatası. Lütfen daha sonra tekrar deneyin."
            else -> "Beklenmeyen bir hata oluştu. (${e.code()})"
        }
        ApiResult.Error(message = message, code = e.code())

    } catch (e: IOException) {
        ApiResult.Error(message = "İnternet bağlantınızı kontrol edin.")

    } catch (e: Exception) {
        ApiResult.Error(message = e.message ?: "Bilinmeyen bir hata oluştu.")
    }
}