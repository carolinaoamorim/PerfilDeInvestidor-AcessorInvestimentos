package com.acessorinvestimentos.perfilinvestidor.repository;

import com.acessorinvestimentos.perfilinvestidor.model.InvestorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvestorProfileRepository extends JpaRepository<InvestorProfile, UUID> {

    Optional<InvestorProfile> findByUserId(String userId);
}
