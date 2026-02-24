package com.nursena.fenlab_android.domain.model

import com.nursena.fenlab_android.domain.model.enums.UserRole

data class User(
    val id: Long,
    val username: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val branch: String?,
    val experienceYears: Int?,
    val bio: String?,
    val profileImageUrl: String?,
    val createdAt: String,
    val lastLogin: String?,
    val experimentCount: Long
) {
    val isTeacher: Boolean get() = role == UserRole.TEACHER
    val displayRole: String get() = if (isTeacher) "Öğretmen" else "Kullanıcı"
    val displayName: String get() = fullName.ifBlank { username }
}