package com.nursena.fenlab_android.data.remote.mapper


import com.nursena.fenlab_android.data.remote.dto.response.ExperimentSummaryResponse
import com.nursena.fenlab_android.domain.model.Experiment

// FavoriteApi ExperimentSummaryResponse döndürdüğü için
// ExperimentMapper.toDomain() yeterli — bu dosya ayrı mapper'a ihtiyaç duymaz.
// ExperimentSummaryResponse.toDomain() → ExperimentMapper.kt içinde tanımlı.
