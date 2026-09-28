package com.acessorinvestimentos.perfilinvestidor.controller;

import br.insper.investorprofile.dto.InvestorProfileResponse;
import br.insper.investorprofile.dto.QuestionnaireDto;
import br.insper.investorprofile.dto.QuestionnaireRequest;
import br.insper.investorprofile.service.InvestorProfileService;
import br.insper.investorprofile.service.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    /**
     * Enquanto o Auth0 não entra, o userId chega por este header.
     * Na etapa do Auth0 ele passa a vir do "sub" do JWT.
     */
    public static final String USER_ID_HEADER = "X-User-Id";

    private final QuestionnaireService questionnaireService;
    private final InvestorProfileService investorProfileService;

    @PostMapping("/questionnaire")
    public ResponseEntity<InvestorProfileResponse> submitQuestionnaire(
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestBody QuestionnaireRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionnaireService.submitQuestionnaire(userId, request));
    }

    @PutMapping("/questionnaire")
    public ResponseEntity<InvestorProfileResponse> updateQuestionnaire(
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestBody QuestionnaireRequest request) {
        return ResponseEntity.ok(questionnaireService.updateQuestionnaire(userId, request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<InvestorProfileResponse> getProfile(@PathVariable String userId) {
        return ResponseEntity.ok(investorProfileService.getProfileByUserId(userId));
    }

    @GetMapping("/{userId}/questionnaire")
    public ResponseEntity<QuestionnaireDto> getQuestionnaire(@PathVariable String userId) {
        return ResponseEntity.ok(questionnaireService.getQuestionnaireByUserId(userId));
    }
}
