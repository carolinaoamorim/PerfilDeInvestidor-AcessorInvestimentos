package com.acessorinvestimentos.perfilinvestidor.exception;

public class QuestionnaireAlreadyExistsException extends RuntimeException {

    public QuestionnaireAlreadyExistsException(String userId) {
        super("Questionnaire already submitted for user " + userId + ". Use PUT to update it.");
    }
}
