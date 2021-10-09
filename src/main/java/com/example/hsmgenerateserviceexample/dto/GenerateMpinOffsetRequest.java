package com.example.hsmgenerateserviceexample.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateMpinOffsetRequest {

    private String pinDataBlock;

    private String vpan;
}
