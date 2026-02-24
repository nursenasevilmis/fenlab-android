package com.nursena.fenlab_android.domain.model

import com.nursena.fenlab_android.domain.model.enums.UserRole

data class UserSummary(
    val id: Long,
    val username: String,
    val fullName: String,
    val role: UserRole,
    val profileImageUrl: String?
) {
    val isTeacher: Boolean get() = role == UserRole.TEACHER
    val displayRole: String get() = if (isTeacher) "Öğretmen" else "Kullanıcı"
    val displayName: String get() = fullName.ifBlank { username }
    val initials: String get() = fullName
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { username.take(2).uppercase() }
}