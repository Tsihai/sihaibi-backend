package com.sihai.springbootinit.model.dto.frequency;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sihai
 * 使用次数
 */
@Data
public class FrequencyRequest implements Serializable {
    private int frequency;
}
