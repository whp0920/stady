package org.example.stady.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.stady.dto.StatsDTO;
import org.example.stady.entity.LearningRecord;
import org.example.stady.mapper.LearningRecordMapper;
import org.example.stady.utils.UserContext;
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
        record.setUserId(UserContext.getUserId());
        mapper.insert(record);
        return record;
    }

    @Cacheable(value = "records", key = "'record_' + #id + '_' + T(org.example.stady.utils.UserContext).getUserId()")
    public LearningRecord getById(Long id) {
        QueryWrapper<LearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", UserContext.getUserId()).eq("id", id);
        return mapper.selectOne(wrapper);
    }

    @Cacheable(value = "records", key = "'all_' + T(org.example.stady.utils.UserContext).getUserId()")
    public List<LearningRecord> getAll() {
        QueryWrapper<LearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", UserContext.getUserId()).orderByDesc("created_at");
        return mapper.selectList(wrapper);
    }

    public Page<LearningRecord> getPage(Page<LearningRecord> page) {
        QueryWrapper<LearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", UserContext.getUserId()).orderByDesc("created_at");
        return mapper.selectPage(page, wrapper);
    }

    @Caching(evict = {
        @CacheEvict(value = "records", allEntries = true),
        @CacheEvict(value = "stats", allEntries = true)
    })
    public LearningRecord update(LearningRecord record) {
        QueryWrapper<LearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", UserContext.getUserId()).eq("id", record.getId());
        mapper.update(record, wrapper);
        return mapper.selectOne(wrapper);
    }

    @Caching(evict = {
        @CacheEvict(value = "records", allEntries = true),
        @CacheEvict(value = "stats", allEntries = true)
    })
    public void delete(Long id) {
        QueryWrapper<LearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", UserContext.getUserId()).eq("id", id);
        mapper.delete(wrapper);
    }

    @Cacheable(value = "stats", key = "'stats_' + T(org.example.stady.utils.UserContext).getUserId()")
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

        List<StatsDTO> result = new ArrayList<>();
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            StatsDTO dto = new StatsDTO();
            dto.setName(e.getKey());
            dto.setMinutes(e.getValue());
            result.add(dto);
        }

        return result;
    }
}
