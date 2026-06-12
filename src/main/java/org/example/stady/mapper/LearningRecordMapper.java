package org.example.stady.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.stady.entity.LearningRecord;

@Mapper
public interface LearningRecordMapper extends BaseMapper<LearningRecord> {
    // BaseMapper 自带 insert、selectById、selectList、updateById、deleteById
    // 不需要写任何方法和 XML
}
