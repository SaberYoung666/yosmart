package com.swpu.yosmart.entity.dto.apidto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChoiceDTO {
	private int index;
	private MessageDTO message;
	private Object logprobs;
	private String finish_reason;
}
