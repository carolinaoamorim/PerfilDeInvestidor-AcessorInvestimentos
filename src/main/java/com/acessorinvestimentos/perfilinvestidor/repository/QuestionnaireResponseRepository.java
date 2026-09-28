package com.acessorinvestimentos.perfilinvestidor.repository;

import br.insper.investorprofile.model.QuestionnaireResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuestionnaireResponseRepository extends JpaRepository<QuestionnaireResponse, UUID> {

    Optional<QuestionnaireResponse> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
