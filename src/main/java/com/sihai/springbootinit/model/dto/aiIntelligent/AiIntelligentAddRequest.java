package com.sihai.springbootinit.model.dto.aiIntelligent;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

@Data
public class AiIntelligentAddRequest  implements Serializable {

    /**
     * 用户输入信息
     */
    private String inputMessage;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
