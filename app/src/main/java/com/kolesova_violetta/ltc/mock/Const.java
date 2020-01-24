package com.kolesova_violetta.ltc.mock;

public class Const {
    //PreferenceCategory [GENERAL]
    public static final String PREF_DRIVER_NAME = "tractor_driver_name_et";
    public static final String PREF_TYPE_INSTALLATION = "tractor_type_installation_list";
    public static final String PREF_AXLE_UNDER_CAB = "tractor_circuits_under_cab_switch";
    //end [GENERAL]
    //PreferenceCategory [TRACTOR]
    public static final String PREF_TRACTOR_MARK = "tractor_mark_list";
    public static final String PREF_TRACTOR_MODEL = "tractor_model_list";
    public static final String PREF_TRACTOR_NUMBER = "tractor_state_number_et";
    public static final String PREF_TRACTOR_VIN = "tractor_vin_et";
    public static final String PREF_TRACTOR_WHEEL_FORMULA = "tractor_wheel_formula_list";
    public static final String PREF_TRACTOR_YEAR = "tractor_year_of_issue_et";
    public static final String PREF_TRACTOR_CIRCUITS = "tractor_number_circuits_list";
    //end [TRACTOR]
    //PreferenceCategory [TRAILER]
    public static final String PREF_TRAILER_MARK = "trailer_mark_list";
    public static final String PREF_TRAILER_TYPE = "trailer_type_list";
    public static final String PREF_TRAILER_NUMBER = "trailer_state_number_et";
    public static final String PREF_TRAILER_VIN = "trailer_vin_et";
    public static final String PREF_TRAILER_ID = "trailer_id_et";
    public static final String PREF_TRAILER_YEAR = "trailer_year_et";
    public static final String PREF_TRAILER_SWITCH = "trailer_switch";
    public static final String PREF_TRAILER_CIRCUITS = "trailer_number_circuits_list";
    public static final int CIRCUITS_COUNT = 4;
    //end [TRAILER]

    public static final String PREF_TYPE = "circuit_type_list";
    public static final String PREF_VARIABLE = "circuit_variable_list";
    public static final String PREF_VARIABLE_LEFT = "circuit_variable_left_list";
    public static final String PREF_VARIABLE_RIGHT = "circuit_variable_right_list";
    // Часть, которая содержится только в PREF_VARIABLE / LEFT / RIGHT
    public static final String PREF_AXES = "circuit_number_axes_et";
    public static final int START_POSITION_PREF_TYPE_TRACTOR = 1;
    public static final int START_POSITION_PREF_TYPE_TRAILER = CIRCUITS_COUNT + 1;

    public static final String ERROR_VALUE = "-1";

    public static final String NAME_PREF_AXLE_TRACTOR = "tractor_axle_";
    public static final String NAME_PREF_AXLE_TRAILER = "trailer_axle_";

}
