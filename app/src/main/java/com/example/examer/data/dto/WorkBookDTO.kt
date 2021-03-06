package com.example.examer.data.dto

import com.example.examer.data.domain.ExamerAudioFile
import com.example.examer.data.domain.MultiChoiceQuestion

/**
 * A DTO object equivalent to [WorkBookDTO].
 */
data class WorkBookDTO(
    val id: String,
    val audioFile: AudioFileDTO,
    val questions: List<MultiChoiceQuestionDTO>
)