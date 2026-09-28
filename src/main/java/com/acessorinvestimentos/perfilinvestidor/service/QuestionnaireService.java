package com.acessorinvestimentos.perfilinvestidor.service;

import com.acessorinvestimentos.perfilinvestidor.dto.InvestorProfileResponse;
import com.acessorinvestimentos.perfilinvestidor.dto.QuestionnaireDto;
import com.acessorinvestimentos.perfilinvestidor.dto.QuestionnaireRequest;
import com.acessorinvestimentos.perfilinvestidor.exception.IncompleteQuestionnaireException;
import com.acessorinvestimentos.perfilinvestidor.exception.QuestionnaireAlreadyExistsException;
import com.acessorinvestimentos.perfilinvestidor.exception.ResourceNotFoundException;
import com.acessorinvestimentos.perfilinvestidor.mapper.ProfileMapper;
import com.acessorinvestimentos.perfilinvestidor.model.QuestionnaireResponse;
import com.acessorinvestimentos.perfilinvestidor.repository.QuestionnaireResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionnaireService {

    private final QuestionnaireResponseRepository questionnaireRepository;
    private final InvestorProfileService investorProfileService;
    private final ProfileMapper mapper;

    @Transactional
    public InvestorProfileResponse submitQuestionnaire(String userId, QuestionnaireRequest request) {
        validateUserId(userId);
        validateAnswers(request);
        if (questionnaireRepository.existsByUserId(userId)) {
            throw new QuestionnaireAlreadyExistsException(userId);
        }
        questionnaireRepository.save(mapper.toEntity(userId, request));
        return investorProfileService.updateProfile(userId);
    }

    @Transactional
    public InvestorProfileResponse updateQuestionnaire(String userId, QuestionnaireRequest request) {
        validateUserId(userId);
        validateAnswers(request);
        QuestionnaireResponse questionnaire = findByUserId(userId);
        mapper.updateEntity(questionnaire, request);
        questionnaireRepository.save(questionnaire);
        return investorProfileService.updateProfile(userId);
    }

    @Transactional(readOnly = true)
    public QuestionnaireDto getQuestionnaireByUserId(String userId) {
        return mapper.toDto(findByUserId(userId));
    }

    /** Garante que todas as perguntas foram respondidas e informa quais faltam. */
    public void validateAnswers(QuestionnaireRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Questionnaire answers are required");
        }
        List<String> missing = new ArrayList<>();
        if (request.financialGoal() == null) missing.add("financialGoal");
        if (request.riskTolerance() == null) missing.add("riskTolerance");
        if (request.investmentHorizon() == null) missing.add("investmentHorizon");
        if (request.hasEmergencyReserve() == null) missing.add("hasEmergencyReserve");
        if (request.experienceLevel() == null) missing.add("experienceLevel");
        if (!missing.isEmpty()) {
            throw new IncompleteQuestionnaireException(missing);
        }
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
    }

    private QuestionnaireResponse findByUserId(String userId) {
        return questionnaireRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Questionnaire not found for user " + userId));
    }
}
