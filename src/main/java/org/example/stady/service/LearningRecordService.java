package org.example.stady.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.stady.dto.StatsDTO;
import org.example.stady.entity.LearningRecord;
import org.example.stady.mapper.LearningRecordMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LearningRecordService {

    private final LearningRecordMapper mapper;

    public LearningRecordService(LearningRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Caching(evict = {
        @CacheEvict(value = "records", allEntries = true),
        @CacheEvict(value = "stats", allEntries = true)
    })
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

    @Caching(evict = {
        @CacheEvict(value = "records", allEntries = true),
        @CacheEvict(value = "stats", allEntries = true)
    })
    public LearningRecord update(LearningRecord record) {
        mapper.updateById(record);
        return mapper.selectById(record.getId());
    }

    @Caching(evict = {
        @CacheEvict(value = "records", allEntries = true),
        @CacheEvict(value = "stats", allEntries = true)
    })
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Cacheable(value = "stats", key = "'stats'")
    public List<StatsDTO> getStats() {
        List<LearningRecord> all = getAll();

        // for 循环版：分组求和
        Map<String, Integer> map = new HashMap<>();
        for (LearningRecord r : all) {
            String cat = r.getCategory();
            map.put(cat, map.getOrDefault(cat, 0) + r.getDuration());
        }

        // Stream 版：完全等价（对照学习用，实际只用一份）
        // Map<String, Integer> map = all.stream()
        //     .collect(Collectors.groupingBy(
        //         LearningRecord::getCategory,
        //         Collectors.summingInt(LearningRecord::getDuration)
        //     ));

        // for 循环版：Map → DTO 列表
        List<StatsDTO> result = new ArrayList<>();
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            StatsDTO dto = new StatsDTO();
            dto.setName(e.getKey());
            dto.setMinutes(e.getValue());
            result.add(dto);
        }

        // Stream 版：完全等价
        // List<StatsDTO> result = map.entrySet().stream()
        //     .map(e -> {
        //         StatsDTO dto = new StatsDTO();
        //         dto.setName(e.getKey());
        //         dto.setMinutes(e.getValue());
        //         return dto;
        //     })
        //     .collect(Collectors.toList());

        return result;
    }
}
