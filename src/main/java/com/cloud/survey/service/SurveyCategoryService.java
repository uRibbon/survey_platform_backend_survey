package com.cloud.survey.service;

import com.cloud.survey.dto.PageRequestDTO;
import com.cloud.survey.dto.PageResultDTO;
import com.cloud.survey.dto.SurveyCategoryDTO;
import com.cloud.survey.entity.SurveyCategory;

public interface SurveyCategoryService {
    PageResultDTO<SurveyCategoryDTO, SurveyCategory> getCategoryList(PageRequestDTO requestDTO);
    void insertCategory(SurveyCategory surveyCategory);
    void deleteCategory(Integer surCatId);

    default SurveyCategory dtoToEntity(SurveyCategoryDTO dto) {
        SurveyCategory surveyCategory = SurveyCategory.builder()
                .surCatId(dto.getSurCatId())
                .content(dto.getContent())
                .build();
        return surveyCategory;
    }

    default SurveyCategoryDTO entityToDTO(SurveyCategory surveyCategory) {
        SurveyCategoryDTO dto = SurveyCategoryDTO.builder()
                .surCatId(surveyCategory.getSurCatId())
                .content(surveyCategory.getContent())
                .build();
        return dto;
    }

}