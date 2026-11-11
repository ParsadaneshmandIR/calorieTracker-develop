package com.parsa.calorietracker.web;

import com.parsa.calorietracker.dto.CalcRequest;
import com.parsa.calorietracker.dto.CalcResponse;
import com.parsa.calorietracker.service.CalcService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calc")
public class CalcController {

    private final CalcService calcService;

    public CalcController(CalcService calcService) {
        this.calcService = calcService;
    }

    @PostMapping
    public CalcResponse calculate(@Valid @RequestBody CalcRequest request) {
        return calcService.calculate(request);
    }
}
