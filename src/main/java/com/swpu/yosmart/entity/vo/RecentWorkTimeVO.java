package com.swpu.yosmart.entity.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RecentWorkTimeVO {
	private LocalDate workDate;
	private Integer totalDuration;
}
