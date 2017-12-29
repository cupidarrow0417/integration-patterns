package com.rewedigital.examples.msintegration.productdetailpage.infrastructure.eventing.exception;

import com.rewedigital.examples.msintegration.productdetailpage.infrastructure.eventing.EventProcessingState;

public class UnexpectedMessageProcessingException extends MessageProcessingException {

    public UnexpectedMessageProcessingException(final String message, final Exception e) {
        super(EventProcessingState.UNEXPECTED_ERROR, message, e);
    }

    public UnexpectedMessageProcessingException(final String message) {
        super(EventProcessingState.UNEXPECTED_ERROR, message);
    }

}
