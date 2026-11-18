package com.dbeditor.model;

public enum CardinalityValue {
    _01_, _11_, _0N_, _1N_;

    public static CardinalityValue getCardinalityValue(String value) {
        return switch (value) {
            case "0,1" -> CardinalityValue._01_;
            case "1,1" -> CardinalityValue._11_;
            case "0,n" -> CardinalityValue._0N_;
            case "1,n" -> CardinalityValue._1N_;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case _01_ -> "0,1";
            case _11_ -> "1,1";
            case _0N_ -> "0,n";
            case _1N_ -> "1,n";
            default -> null;
        };
    }
}