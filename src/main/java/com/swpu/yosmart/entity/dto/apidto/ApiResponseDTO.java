package com.swpu.yosmart.entity.dto.apidto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiResponseDTO {
	private String id;
	private String object;
	private long created;
	private String model;
	private List<ChoiceDTO> choices;
	private UsageDTO usage;
	private String system_fingerprint;
}
