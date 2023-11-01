package com.sihai.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户编号
 * @author sihai
 */
@Data
public class UserCodeVO extends UserVO implements Serializable {

    /**
     * id
     */
    private Long id;


    private static final long serialVersionUID = 1L;
}
