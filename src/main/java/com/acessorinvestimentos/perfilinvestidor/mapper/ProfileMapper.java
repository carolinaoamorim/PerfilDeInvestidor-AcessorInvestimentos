package com.acessorinvestimentos.perfilinvestidor.mapper;

import com.acessorinvestimentos.perfilinvestidor.dto.InvestorProfileResponse;
import com.acessorinvestimentos.perfilinvestidor.dto.QuestionnaireDto;
import com.acessorinvestimentos.perfilinvestidor.dto.QuestionnaireRequest;
import com.acessorinvestimentos.perfilinvestidor.model.InvestorProfile;
import com.acessorinvestimentos.perfilinvestidor.model.QuestionnaireResponse;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    public QuestionnaireResponse toEntity(String userId, QuestionnaireRequest request) {
        return QuestionnaireResponse.builder()
                .userId(userId)
                .financialGoal(request.financialGoal())
                .riskTolerance(request.riskTolerance())
                .investmentHorizon(request.investmentHorizon())
                .hasEmergencyReserve(request.hasEmergencyReserve())
                .experienceLevel(request.experienceLevel())
                .build();
    }

    public void updateEntity(QuestionnaireResponse entity, QuestionnaireRequest request) {
        entity.setFinancialGoal(request.financialGoal());
        entity.setRiskTolerance(request.riskTolerance());
        entity.setInvestmentHorizon(request.investmentHorizon());
        entity.setHasEmergencyReserve(request.hasEmergencyReserve());
        entity.setExperienceLevel(request.experienceLevel());
    }

    public QuestionnaireDto toDto(QuestionnaireResponse entity) {
        return new QuestionnaireDto(
                entity.getId(),
                entity.getUserId(),
                entity.getFinancialGoal(),
                entity.getRiskTolerance(),
                entity.getInvestmentHorizon(),
                entity.getHasEmergencyReserve(),
                entity.getExperienceLevel(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public InvestorProfileResponse toResponse(InvestorProfile profile) {
        return new InvestorProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getInvestorType(),
                profile.getScore(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
