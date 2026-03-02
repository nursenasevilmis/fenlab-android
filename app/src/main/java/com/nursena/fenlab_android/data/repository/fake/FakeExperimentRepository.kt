package com.nursena.fenlab_android.data.repository.fake

import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.dto.request.ExperimentCreateRequest
import com.nursena.fenlab_android.data.remote.dto.request.ExperimentUpdateRequest
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.model.ExperimentDetail
import com.nursena.fenlab_android.domain.model.Material
import com.nursena.fenlab_android.domain.model.Media
import com.nursena.fenlab_android.domain.model.PaginatedData
import com.nursena.fenlab_android.domain.model.Step
import com.nursena.fenlab_android.domain.model.enums.DifficultyLevel
import com.nursena.fenlab_android.domain.repository.ExperimentRepository
import com.nursena.fenlab_android.ui.preview.MockData
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Backend olmadan UI testi için sahte repository.
 *
 * Kullanmak için RepositoryModule.kt'de:
 *   @Binds fun bindExperiment(impl: FakeExperimentRepository): ExperimentRepository
 * satırını geçici olarak değiştir.
 */
class FakeExperimentRepository @Inject constructor() : ExperimentRepository {

    // Listeyi değiştirebilmek için mutable kopyalıyoruz
    private val experiments = MockData.mockExperiments.toMutableList()

    override suspend fun getAllExperiments(
        search: String?, subject: String?, environment: String?,
        gradeLevel: Int?, difficulty: String?, sortType: String,
        page: Int, size: Int
    ): ApiResult<PaginatedData<Experiment>> {
        delay(600) // gerçekçi ağ gecikmesi simülasyonu

        var result = experiments.toList()

        // Arama filtresi
        if (!search.isNullOrBlank()) {
            result = result.filter {
                it.title.contains(search, ignoreCase = true) ||
                        it.description.contains(search, ignoreCase = true)
            }
        }
        // Ders filtresi
        if (!subject.isNullOrBlank()) {
            result = result.filter { it.subject?.name == subject }
        }
        // Ortam filtresi
        if (!environment.isNullOrBlank()) {
            result = result.filter { it.environment?.name == environment }
        }
        // Zorluk filtresi
        if (!difficulty.isNullOrBlank()) {
            result = result.filter { it.difficulty.name == difficulty }
        }

        return ApiResult.Success(
            PaginatedData(
                content       = result,
                page          = 0,
                size          = result.size,
                totalElements = result.size.toLong(),
                totalPages    = 1,
                isFirst       = true,
                isLast        = true
            )
        )
    }

    override suspend fun getExperimentById(experimentId: Long): ApiResult<ExperimentDetail> {
        delay(400)
        val exp = experiments.firstOrNull { it.id == experimentId }
            ?: return ApiResult.Error("Deney bulunamadı")

        return ApiResult.Success(
            ExperimentDetail(
                id                       = exp.id,
                author                   = exp.author,
                title                    = exp.title,
                description              = exp.description,
                gradeLevel               = exp.gradeLevel,
                subject                  = exp.subject,
                environment              = exp.environment,
                topic                    = exp.topic,
                difficulty               = exp.difficulty,
                expectedResult           = "Beklenen sonuç: Deney başarıyla tamamlanacak ve hipotez doğrulanacak.",
                safetyNotes              = "Güvenlik notu: Göz temasından kaçının, eldiven kullanın.",
                isPublished              = true,
                createdAt                = exp.createdAt,
                updatedAt                = null,
                materials                = listOf(
                    Material(1L, "Su", "200 ml"),
                    Material(2L, "Tuz", "1 yemek kaşığı"),
                    Material(3L, "Ampul", "1 adet"),
                    Material(4L, "Pil", "2 adet AA")
                ),
                steps                    = listOf(
                    Step(1L, 1, "Kaba 200 ml su doldurun."),
                    Step(2L, 2, "İçine 1 yemek kaşığı tuz ekleyip karıştırın."),
                    Step(3L, 3, "Devreyi kurun ve ampulün yandığını gözlemleyin."),
                    Step(4L, 4, "Saf su ile aynı deneyi yaparak farkı karşılaştırın.")
                ),
                media                    = emptyList(),
                favoriteCount            = exp.favoriteCount,
                averageRating            = exp.averageRating,
                commentCount             = exp.commentCount,
                questionCount            = 3L,
                isFavoritedByCurrentUser = exp.isFavoritedByCurrentUser,
                currentUserRating        = null
            )
        )
    }

    override suspend fun createExperiment(request: ExperimentCreateRequest): ApiResult<ExperimentDetail> =
        ApiResult.Error("Mock modda deney oluşturulamaz.")

    override suspend fun updateExperiment(
        experimentId: Long, request: ExperimentUpdateRequest
    ): ApiResult<ExperimentDetail> = ApiResult.Error("Mock modda güncelleme yapılamaz.")

    override suspend fun deleteExperiment(experimentId: Long): ApiResult<Unit> =
        ApiResult.Error("Mock modda silme yapılamaz.")

    override suspend fun getUserExperiments(
        userId: Long, page: Int, size: Int
    ): ApiResult<PaginatedData<Experiment>> {
        delay(400)
        val result = experiments.filter { it.author.id == userId }
        return ApiResult.Success(
            PaginatedData(result, 0, result.size, result.size.toLong(), 1, true, true)
        )
    }

    override suspend fun getAllSubjects(): ApiResult<List<String>> =
        ApiResult.Success(listOf("SCIENCE", "PHYSICS", "CHEMISTRY", "BIOLOGY", "OTHER"))
}