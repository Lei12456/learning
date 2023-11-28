package com.yl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName tb_user
 */
@TableName(value ="tb_user")
@Data
public class User implements Serializable {
    /**
     * 替代主键，优化B+树
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 公司ID
     */
    private String corpid;

    /**
     * 员工唯一标识ID（不可修改）
     */
    private String userId;

    /**
     * 员工权限
     */
    private String roleIds;

    /**
     * 成员名称
     */
    private String name;

    /**
     * 添加时间
     */
    private Integer addTime;

    /**
     * 更新时间
     */
    private Integer updateTime;

    /**
     * 删除标记
     */
    private Integer del;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        User other = (User) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getCorpid() == null ? other.getCorpid() == null : this.getCorpid().equals(other.getCorpid()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getRoleIds() == null ? other.getRoleIds() == null : this.getRoleIds().equals(other.getRoleIds()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getAddTime() == null ? other.getAddTime() == null : this.getAddTime().equals(other.getAddTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getDel() == null ? other.getDel() == null : this.getDel().equals(other.getDel()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getCorpid() == null) ? 0 : getCorpid().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getRoleIds() == null) ? 0 : getRoleIds().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getAddTime() == null) ? 0 : getAddTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getDel() == null) ? 0 : getDel().hashCode());
        return result;
    }

    @Override
    public String toString() {
        String sb = getClass().getSimpleName() +
                " [" +
                "Hash = " + hashCode() +
                ", id=" + id +
                ", corpid=" + corpid +
                ", userId=" + userId +
                ", roleIds=" + roleIds +
                ", name=" + name +
                ", addTime=" + addTime +
                ", updateTime=" + updateTime +
                ", del=" + del +
                ", serialVersionUID=" + serialVersionUID +
                "]";
        return sb;
    }
}