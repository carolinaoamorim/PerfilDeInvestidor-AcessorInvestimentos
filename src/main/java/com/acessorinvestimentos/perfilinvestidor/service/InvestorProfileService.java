package com.acessorinvestimentos.perfilinvestidor.service;

import com.acessorinvestimentos.perfilinvestidor.dto.ClassificationResult;
import com.acessorinvestimentos.perfilinvestidor.dto.InvestorProfileResponse;
import com.acessorinvestimentos.perfilinvestidor.event.ProfileEventPublisher;
import com.acessorinvestimentos.perfilinvestidor.exception.ResourceNotFoundException;
import com.acessorinvestimentos.perfilinvestidor.mapper.ProfileMapper;
import com.acessorinvestimentos.perfilinvestidor.model.InvestorProfile;
import com.acessorinvestimentos.perfilinvestidor.model.QuestionnaireResponse;
import com.acessorinvestimentos.perfilinvestidor.model.enums.InvestorType;
import com.acessorinvestimentos.perfilinvestidor.repository.InvestorProfileRepository;
import com.acessorinvestimentos.perfilinvestidor.repository.QuestionnaireResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvestorProfileService {

    private final InvestorProfileRepository profileRepository;
    private final QuestionnaireResponseRepository questionnaireRepository;
    private final ProfileClassificationService classificationService;
    private final ProfileEventPublisher eventPublisher;
    private final ProfileMapper mapper;

    @Transactional(readOnly = true)
    public InvestorProfileResponse getProfileByUserId(String userId) {
        return profileRepository.findByUserId(userId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user " + userId));
    }

    /** Calcula pontuação e perfil a partir do questionário salvo, sem persistir nada. */
    @Transactional(readOnly = true)
    public ClassificationResult calculateProfile(String userId) {
        QuestionnaireResponse questionnaire = questionnaireRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Questionnaire not found for user " + userId));
        int score = classificationService.calculateScore(questionnaire);
        return new ClassificationResult(score, classificationService.classifyInvestor(score));
    }

    /** Cria ou atualiza o perfil e publica profile.updated quando o tipo de investidor muda. */
    @Transactional
    public InvestorProfileResponse updateProfile(String userId) {
        ClassificationResult result = calculateProfile(userId);

        Optional<InvestorProfile> existing = profileRepository.findByUserId(userId);
        InvestorType previousType = existing.map(InvestorProfile::getInvestorType).orElse(null);

        InvestorProfile profile = existing.orElseGet(() -> InvestorProfile.builder().userId(userId).build());
        profile.setScore(result.score());
        profile.setInvestorType(result.investorType());
        InvestorProfile saved = profileRepository.save(profile);

        if (previousType != result.investorType()) {
            eventPublisher.publishProfileUpdated(userId, result.investorType());
        }
        return mapper.toResponse(saved);
    }
}
