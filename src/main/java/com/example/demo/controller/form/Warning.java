package com.example.demo.controller.form;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@Getter
public class Warning {
    private  String error;
    public Warning() {
        this.error = "";
    }
}
