package com.shaoteemo.dto;

import lombok.*;

import java.io.Serializable;

/**
 * Create Info:
 * <br>Change Info:
 * <br>Create On 2023/8/7 11:15
 *
 * @author XiaoMo
 * @since 0.1-alpha
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User implements Serializable {

    private Integer id;

    private String name;

}
