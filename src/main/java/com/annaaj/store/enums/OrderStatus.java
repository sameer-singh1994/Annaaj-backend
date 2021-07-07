package com.annaaj.store.enums;

public enum OrderStatus {
    PLACED("placed"),
    DELIVERED_TO_COMMUNITY_LEADER("delivered_to_community_leader"),
    COMPLETED("completed");

    private String text;

    OrderStatus(final String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static OrderStatus fromString(String text) {
        for (OrderStatus orderStatus : OrderStatus.values()) {
            if (orderStatus.text.equalsIgnoreCase(text)) {
                return orderStatus;
            }
        }
        throw new IllegalArgumentException("No Status with text " + text + " found");
    }
}
