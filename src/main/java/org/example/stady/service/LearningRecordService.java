package org.example.stady.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.stady.entity.LearningRecord;
import org.example.stady.mapper.LearningRecordMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LearningRecordService {

    private final LearningRecordMapper mapper;

    public LearningRecordService(LearningRecordMapper mapper) {
        this.mapper = mapper;
    }

    @CacheEvict(value = "records", allEntries = true)
    public LearningRecord create(LearningRecord record) {
        mapper.insert(record);
        return record;
    }

    @Cacheable(value = "records", key = "'record_' + #id")
    public LearningRecord getById(Long id) {
        return mapper.selectById(id);
    }

    @Cacheable(value = "records", key = "'all'")
    public List<LearningRecord> getAll() {
        QueryWrapper<LearningRecord> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at");
        return mapper.selectList(wrapper);
    }

    public Page<LearningRecord> getPage(Page<LearningRecord> page) {
        QueryWrapper<LearningRecord> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at");
        return mapper.selectPage(page, wrapper);
    }

    @CacheEvict(value = "records", allEntries = true)
    public LearningRecord update(LearningRecord record) {
        mapper.updateById(record);
        return mapper.selectById(record.getId());
    }

    @CacheEvict(value = "records", allEntries = true)
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
