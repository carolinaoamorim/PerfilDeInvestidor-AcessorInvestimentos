package com.acessorinvestimentos.perfilinvestidor.service;

import br.insper.investorprofile.dto.ClassificationResult;
import br.insper.investorprofile.dto.InvestorProfileResponse;
import br.insper.investorprofile.event.ProfileEventPublisher;
import br.insper.investorprofile.exception.ResourceNotFoundException;
import br.insper.investorprofile.mapper.ProfileMapper;
import br.insper.investorprofile.model.InvestorProfile;
import br.insper.investorprofile.model.QuestionnaireResponse;
import br.insper.investorprofile.model.enums.InvestorType;
import br.insper.investorprofile.repository.InvestorProfileRepository;
import br.insper.investorprofile.repository.QuestionnaireResponseRepository;
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
