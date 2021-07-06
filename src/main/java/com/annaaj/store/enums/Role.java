package com.annaaj.store.enums;

public enum Role {
    user("user"),
    communityLeader("community_leader"),
    admin("admin");

    private String text;

    Role(final String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static Role fromString(String text) {
        for (Role accountType : Role.values()) {
            if (accountType.text.equalsIgnoreCase(text)) {
                return accountType;
            }
        }
        throw new IllegalArgumentException("No Role with text " + text + " found");
    }
}
