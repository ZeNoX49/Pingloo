package com.dbeditor.model.mcd;

public enum CardinalityValue {
    _01_, _11_, _0N_, _1N_;

    public static CardinalityValue getCardinalityValue(String value) {
        switch (value) {
            case "0,1": return CardinalityValue._01_;
            case "1,1": return CardinalityValue._11_;
            case "0,n": return CardinalityValue._0N_;
            case "1,n": return CardinalityValue._1N_;
            default: return null;
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case _01_: return "0,1";
            case _11_: return "1,1";
            case _0N_: return "0,n";
            case _1N_: return "1,n";
            default: return null;
        }
    }
}