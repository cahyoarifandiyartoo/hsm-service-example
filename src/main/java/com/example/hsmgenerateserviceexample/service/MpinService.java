package com.example.hsmgenerateserviceexample.service;

import com.example.hsmgenerateserviceexample.dto.GenerateMpinOffsetRequest;
import com.example.hsmgenerateserviceexample.dto.GenerateMpinOffsetResponse;
import com.example.hsmgenerateserviceexample.dto.VerifyMpinRequest;
import com.example.hsmgenerateserviceexample.dto.VerifyMpinResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MpinService {

    @Autowired
    private HSMService hsmService;

    public GenerateMpinOffsetResponse generateMpin(GenerateMpinOffsetRequest request) {
        String pinOffset = hsmService.getNewPINOffset(request.getVpan(), request.getPinDataBlock());

        return GenerateMpinOffsetResponse.builder()
                .pinOffset(pinOffset)
                .build();
    }

    public VerifyMpinResponse verifyMpin(VerifyMpinRequest request) {
        String responseCode = hsmService.verifyPIN(request.getVpan(), request.getPinDataBlock(), request.getPinOffset());

        Integer counter = 0;
        if (!Objects.equals(responseCode, "00")) {
            counter += 1;
        }

        return VerifyMpinResponse.builder()
                .responseCode(responseCode)
                .build();
    }
}
