package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.core.Constants
import com.nursena.fenlab_android.data.remote.dto.response.UserResponse
import com.nursena.fenlab_android.data.remote.dto.response.UserSummaryResponse
import com.nursena.fenlab_android.domain.model.User
import com.nursena.fenlab_android.domain.model.UserSummary
import com.nursena.fenlab_android.domain.model.enums.UserRole

// Minio URL'lerindeki IP'yi aktif ağ IP'siyle eşleştir (ExperimentMapper ile aynı mantık)
private val ipPattern = Regex(
    """(10\.0\.[23]\.2|localhost|127\.0\.0\.1|192\.168\.\d+\.\d+|172\.\d+\.\d+\.\d+)"""
)

private fun String.fixMinioUrl(): String =
    ipPattern.replace(this, Constants.SERVER_IP)

/**
 * profileImageUrl backend'den iki farklı formatta gelebilir:
 *   1. Sadece path: "fenlab-profiles/abc.jpg"  → Minio base URL eklenmeli
 *   2. Tam URL:     "http://192.168.x.x:9000/fenlab-profiles/abc.jpg" → IP düzeltilmeli
 */
private fun String?.toProfileImageUrl(): String? {
    if (this.isNullOrBlank()) return null
    return if (this.startsWith("http")) {
        // Tam URL gelmiş — sadece IP'yi düzelt
        this.fixMinioUrl()
    } else {
        // Sadece path gelmiş — Minio base URL ekle
        "${Constants.MINIO_URL}/${this.trimStart('/')}"
    }
}

fun UserResponse.toDomain(): User = User(
    id               = id,
    username         = username,
    fullName         = fullName,
    email            = email,
    role             = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.USER),
    branch           = branch,
    experienceYears  = experienceYears,
    bio              = bio,
    profileImageUrl  = profileImageUrl.toProfileImageUrl(),
    createdAt        = createdAt,
    lastLogin        = lastLogin,
    experimentCount  = experimentCount
)

fun UserSummaryResponse.toDomain(): UserSummary = UserSummary(
    id              = id,
    username        = username,
    fullName        = fullName,
    role            = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.USER),
    profileImageUrl = profileImageUrl.toProfileImageUrl()
)