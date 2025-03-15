package com.swpu.yosmart.entity.dto.apidto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDTO {
	private String role;
	private String content; // 需要二次解析的content字段
}
