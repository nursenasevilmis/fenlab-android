package com.nursena.fenlab_android.ui.preview

import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.model.UserSummary
import com.nursena.fenlab_android.domain.model.enums.DifficultyLevel
import com.nursena.fenlab_android.domain.model.enums.EnvironmentType
import com.nursena.fenlab_android.domain.model.enums.SubjectType
import com.nursena.fenlab_android.domain.model.enums.UserRole

object MockData {

    val mockUsers = listOf(
        UserSummary(
            id              = 1L,
            username        = "ahmet_ogretmen",
            fullName        = "Ahmet Yılmaz",
            role            = UserRole.TEACHER,
            profileImageUrl = null
        ),
        UserSummary(
            id              = 2L,
            username        = "elif_bilim",
            fullName        = "Elif Kaya",
            role            = UserRole.TEACHER,
            profileImageUrl = null
        ),
        UserSummary(
            id              = 3L,
            username        = "mehmet_fen",
            fullName        = "Mehmet Demir",
            role            = UserRole.USER,
            profileImageUrl = null
        )
    )

    val mockExperiments = listOf(
        Experiment(
            id                       = 1L,
            author                   = mockUsers[0],
            title                    = "Suyun Elektriği İletmesi",
            description              = "Bu deneyde suyun elektriği iletip iletmediğini ve hangi maddelerin eklenmesiyle iletkenliğin arttığını keşfediyoruz.",
            gradeLevel               = 6,
            subject                  = SubjectType.SCIENCE,
            environment              = EnvironmentType.LABORATORY,
            topic                    = "Elektrik İletkenliği",
            difficulty               = DifficultyLevel.EASY,
            createdAt                = "2025-03-10T10:00:00",
            thumbnailUrl             = "https://picsum.photos/seed/deney1/600/400",
            videoUrl                 = null,
            favoriteCount            = 142L,
            averageRating            = 4.7,
            commentCount             = 23L,
            isFavoritedByCurrentUser = false
        ),
        Experiment(
            id                       = 2L,
            author                   = mockUsers[1],
            title                    = "Renk Filtrasyonu ile Beyaz Işık Ayrıştırma",
            description              = "Prizma ve renkli ışık filtreleri kullanarak beyaz ışığı bileşen renklerine ayırıyoruz. Optik ilkeler gözlemleniyor.",
            gradeLevel               = 9,
            subject                  = SubjectType.PHYSICS,
            environment              = EnvironmentType.LABORATORY,
            topic                    = "Optik",
            difficulty               = DifficultyLevel.MEDIUM,
            createdAt                = "2025-03-08T14:30:00",
            thumbnailUrl             = "https://picsum.photos/seed/deney2/600/400",
            videoUrl                 = "https://example.com/video.mp4",
            favoriteCount            = 89L,
            averageRating            = 4.5,
            commentCount             = 11L,
            isFavoritedByCurrentUser = true
        ),
        Experiment(
            id                       = 3L,
            author                   = mockUsers[0],
            title                    = "Asit-Baz Göstergesi Yapımı",
            description              = "Lahana suyundan doğal pH göstergesi yaparak asit ve baz çözeltilerini test ediyoruz.",
            gradeLevel               = 8,
            subject                  = SubjectType.CHEMISTRY,
            environment              = EnvironmentType.HOME,
            topic                    = "Asit-Baz",
            difficulty               = DifficultyLevel.EASY,
            createdAt                = "2025-03-05T09:15:00",
            thumbnailUrl             = "https://picsum.photos/seed/deney3/600/400",
            videoUrl                 = null,
            favoriteCount            = 215L,
            averageRating            = 4.9,
            commentCount             = 37L,
            isFavoritedByCurrentUser = false
        ),
        Experiment(
            id                       = 4L,
            author                   = mockUsers[1],
            title                    = "DNA Ekstraksiyonu",
            description              = "Ev koşullarında meyve hücrelerinden DNA izole etme deneyi. Bulanık ipliksi yapılar gözlemleniyor.",
            gradeLevel               = 11,
            subject                  = SubjectType.BIOLOGY,
            environment              = EnvironmentType.LABORATORY,
            topic                    = "Genetik",
            difficulty               = DifficultyLevel.HARD,
            createdAt                = "2025-03-01T16:00:00",
            thumbnailUrl             = "https://picsum.photos/seed/deney4/600/400",
            videoUrl                 = "https://example.com/video2.mp4",
            favoriteCount            = 56L,
            averageRating            = 4.3,
            commentCount             = 8L,
            isFavoritedByCurrentUser = false
        ),
        Experiment(
            id                       = 5L,
            author                   = mockUsers[2],
            title                    = "Mıknatıs ile Demir Tozu Şekilleri",
            description              = "Demir tozunu mıknatısın manyetik alan çizgileri boyunca düzenleme. Manyetik alan görselleştirme deneyi.",
            gradeLevel               = 5,
            subject                  = SubjectType.SCIENCE,
            environment              = EnvironmentType.HOME,
            topic                    = "Manyetizma",
            difficulty               = DifficultyLevel.EASY,
            createdAt                = "2025-02-28T11:00:00",
            thumbnailUrl             = "https://picsum.photos/seed/deney5/600/400",
            videoUrl                 = null,
            favoriteCount            = 178L,
            averageRating            = 4.6,
            commentCount             = 29L,
            isFavoritedByCurrentUser = true
        )
    )
}