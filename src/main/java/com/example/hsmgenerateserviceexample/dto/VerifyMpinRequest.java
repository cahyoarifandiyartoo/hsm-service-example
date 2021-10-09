package com.example.hsmgenerateserviceexample.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyMpinRequest {

    private String vpan;

    private String pinDataBlock;

    private String pinOffset;
}
