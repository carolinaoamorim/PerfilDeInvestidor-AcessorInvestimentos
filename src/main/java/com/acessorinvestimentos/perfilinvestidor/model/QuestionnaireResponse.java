package com.acessorinvestimentos.perfilinvestidor.model;

import br.insper.investorprofile.model.enums.ExperienceLevel;
import br.insper.investorprofile.model.enums.FinancialGoal;
import br.insper.investorprofile.model.enums.InvestmentHorizon;
import br.insper.investorprofile.model.enums.RiskTolerance;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "questionnaire_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialGoal financialGoal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskTolerance riskTolerance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvestmentHorizon investmentHorizon;

    @Column(nullable = false)
    private Boolean hasEmergencyReserve;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperienceLevel experienceLevel;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
