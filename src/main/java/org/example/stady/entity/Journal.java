package org.example.stady.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("journal")
public class Journal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String happy;
    private String fulfilled;
    private String improve;
    private String grateful;
    private String mood;
    private String note;
    private LocalDateTime createdAt;

}
