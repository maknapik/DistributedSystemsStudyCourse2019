package model;

import java.util.Arrays;

public enum Injury {
    HIP,
    KNEE,
    ELBOW;

    public static Injury fromString(String stringValue) {
        return Arrays.stream(Injury.values())
                .filter(messageType -> messageType.name().equals(stringValue.toUpperCase()))
                .findFirst()
                .get();
    }
}
