package com.sihai.springbootinit.model.dto.aiIntelligent;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AiIntelligentEditRequest implements Serializable {

    /**
     *
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户输入信息
     */
    private String inputMessage;
    /**
     * AI生成的信息
     */
    private String aiResult;

    /**
     * wait-等待,running-生成中,succeed-成功生成,failed-生成失败
     */
    private String questionStatus;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
