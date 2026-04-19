package com.dbeditor.controller;

import com.dbeditor.controller.view.DdController;
import com.dbeditor.controller.view.DfController;
import com.dbeditor.controller.view.McdController;
import com.dbeditor.controller.view.MldController;
import com.dbeditor.controller.view.SdfController;
import com.dbeditor.controller.view.SqlController;
import com.dbeditor.controller.view.ValueController;
import com.dbeditor.controller.view.View;

public enum ViewType {
    MCD, MLD, DF, DD, SDF, VALUE, SQL;

    @Override
    public String toString() {
        if (this.equals(VALUE)) {
            return "Value";
        } return super.toString();
    }

    public static ViewType toEnum(String value) {
        return switch (value) {
            case "MCD" -> MCD;
            case "MLD" -> MLD;
            case "DF" -> DF;
            case "DD" -> DD;
            case "SDF" -> SDF;
            case "Value" -> VALUE;
            case "SQL" -> SQL;
            default -> null;
        };
    }

    public View getController() {
        return switch (this) {
            case MCD -> new McdController();
            case MLD -> new MldController();
            case DF -> new DfController();
            case DD -> new DdController();
            case SDF -> new SdfController();
            case VALUE -> new ValueController();
            case SQL -> new SqlController();
            default -> null;
        };
    }
}