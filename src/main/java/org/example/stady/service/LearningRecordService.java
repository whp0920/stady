package org.example.stady.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.stady.entity.LearningRecord;
import org.example.stady.mapper.LearningRecordMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LearningRecordService {

    private final LearningRecordMapper mapper;

    public LearningRecordService(LearningRecordMapper mapper) {
        this.mapper = mapper;
    }

    public LearningRecord create(LearningRecord record) {
        mapper.insert(record);
        return record;
    }

    public LearningRecord getById(Long id) {
        return mapper.selectById(id);
    }

    public List<LearningRecord> getAll() {
        QueryWrapper<LearningRecord> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at");
        return mapper.selectList(wrapper);
    }

    public LearningRecord update(LearningRecord record) {
        mapper.updateById(record);
        return mapper.selectById(record.getId());
    }

    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
