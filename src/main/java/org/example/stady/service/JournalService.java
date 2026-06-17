package org.example.stady.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.stady.entity.Journal;
import org.example.stady.mapper.JournalMapper;
import org.example.stady.utils.UserContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JournalService {
   private final JournalMapper mapper;
   public JournalService(JournalMapper mapper) {
       this.mapper = mapper;
   }

    @CacheEvict(value = "journals", allEntries = true)
    public Journal create(Journal journal) {
       journal.setUserId(UserContext.getUserId());
       mapper.insert(journal);
       return journal;
    }

    @Cacheable(value = "journals", key = "'journal_' + #id + '_' + T(org.example.stady.utils.UserContext).getUserId()")
    public Journal getById(Long id) {
       QueryWrapper<Journal> wrapper = new QueryWrapper<>();
       wrapper.eq("user_id", UserContext.getUserId())
               .eq("id", id);
       return mapper.selectOne(wrapper);
    }

    @Cacheable(value = "journals", key = "'all_' + T(org.example.stady.utils.UserContext).getUserId()")
    public List<Journal> getAll() {
        QueryWrapper<Journal> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", UserContext.getUserId())
                .orderByDesc("created_at");
        return mapper.selectList(wrapper);
    }

    public Page<Journal> getPage(Page<Journal> page) {
        QueryWrapper<Journal> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", UserContext.getUserId())
                .orderByDesc("created_at");
        return mapper.selectPage(page, wrapper);
    }

    @CacheEvict(value = "journals", allEntries = true)
    public Journal update(Journal journal) {
         // 安全：只改自己的记录
         QueryWrapper<Journal> wrapper = new QueryWrapper<>();
         wrapper.eq("user_id", UserContext.getUserId()).eq("id", journal.getId());
         mapper.update(journal, wrapper);
         return mapper.selectOne(wrapper);
    }

    @CacheEvict(value = "journals", allEntries = true)
    public void delete(Long id) {
       QueryWrapper<Journal> wrapper = new QueryWrapper<>();
       wrapper.eq("user_id", UserContext.getUserId()).eq("id", id);
       mapper.delete(wrapper);
    }
}
