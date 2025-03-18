package com.swpu.yosmart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
@Getter
@Setter
@TableName("StepCount")
public class StepCount {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private LocalDate date;
    private String type;
    private Integer value;
    private Integer userId;
    private String device;
}