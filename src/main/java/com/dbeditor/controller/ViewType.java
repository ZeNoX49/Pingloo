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
        switch (value) {
            case "MCD" : return MCD;
            case "MLD" : return MLD;
            case "DF" : return DF;
            case "DD" : return DD;
            case "SDF" : return SDF;
            case "Value" : return VALUE;
        } return null;
    }

    public View getController() {
        switch (this) {
            case MCD : return new McdController();
            case MLD : return new MldController();
            case DF : return new DfController();
            case DD : return new DdController();
            case SDF : return new SdfController();
            case VALUE : return new ValueController();
            case SQL : return new SqlController();
        } return null;
    }
}