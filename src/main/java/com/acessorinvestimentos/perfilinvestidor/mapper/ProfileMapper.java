package com.acessorinvestimentos.perfilinvestidor.mapper;

import br.insper.investorprofile.dto.InvestorProfileResponse;
import br.insper.investorprofile.dto.QuestionnaireDto;
import br.insper.investorprofile.dto.QuestionnaireRequest;
import br.insper.investorprofile.model.InvestorProfile;
import br.insper.investorprofile.model.QuestionnaireResponse;
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
