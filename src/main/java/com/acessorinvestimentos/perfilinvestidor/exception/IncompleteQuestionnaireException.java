package com.acessorinvestimentos.perfilinvestidor.exception;

import java.util.List;

public class IncompleteQuestionnaireException extends RuntimeException {

    private final List<String> missingFields;

    public IncompleteQuestionnaireException(List<String> missingFields) {
        super("All questions must be answered");
        this.missingFields = List.copyOf(missingFields);
    }

    public List<String> getMissingFields() {
        return missingFields;
    }
}
