package com.yl.redis.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 用户
 * </p>
 *
 * @author zhengqingya
 * @description
 * @date 2021/01/13 10:11
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_user")
@EqualsAndHashCode(callSuper = true)
public class User extends Model<User> {

    @TableId(type = IdType.AUTO)
    private Long userId;
    private String username;
    private String password;
    /**
     * sex值为空时，MP更新数据库时不忽略此字段值
     */
    @TableField(value = "sex", updateStrategy = FieldStrategy.IGNORED)
    private Byte sex;
    private String remark;

    @TableField(exist = false)
    private Integer page;

    @TableField(exist = false)
    private Integer pageSize;

}
