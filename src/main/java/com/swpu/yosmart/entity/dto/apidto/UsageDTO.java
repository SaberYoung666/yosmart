package com.swpu.yosmart.entity.dto.apidto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsageDTO {
	private int prompt_tokens;
	private int completion_tokens;
	private int total_tokens;
	private PromptTokensDetailsDTO prompt_tokens_details;
	private int prompt_cache_hit_tokens;
	private int prompt_cache_miss_tokens;
}
