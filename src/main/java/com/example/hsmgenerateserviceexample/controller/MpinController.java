package com.example.hsmgenerateserviceexample.controller;

import com.example.hsmgenerateserviceexample.dto.GenerateMpinOffsetRequest;
import com.example.hsmgenerateserviceexample.dto.GenerateMpinOffsetResponse;
import com.example.hsmgenerateserviceexample.dto.VerifyMpinRequest;
import com.example.hsmgenerateserviceexample.dto.VerifyMpinResponse;
import com.example.hsmgenerateserviceexample.service.MpinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mpin")
public class MpinController {

    @Autowired
    private MpinService mpinService;

    @PostMapping("/generateMpin")
    public GenerateMpinOffsetResponse generateMpin(@RequestBody GenerateMpinOffsetRequest request) {
        return mpinService.generateMpin(request);
    }

    @PostMapping("/verifyMpin")
    public VerifyMpinResponse verifyMpin(@RequestBody VerifyMpinRequest request) {
        return mpinService.verifyMpin(request);
    }
}
