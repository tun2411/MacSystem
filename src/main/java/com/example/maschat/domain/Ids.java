package com.example.maschat.domain;

import java.util.UUID;

public final class Ids {
    private Ids() {}

    public static String newUuid() {
        return UUID.randomUUID().toString();
    }
}


