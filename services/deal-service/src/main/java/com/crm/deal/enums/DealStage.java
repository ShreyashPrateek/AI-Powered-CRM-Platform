package com.crm.deal.enums;

public enum DealStage {
    LEAD,
    QUALIFIED,
    PROPOSAL,
    NEGOTIATION,
    CLOSED_WON,
    CLOSED_LOST;

    public boolean isTerminal() {
        return this == CLOSED_WON || this == CLOSED_LOST;
    }
}
