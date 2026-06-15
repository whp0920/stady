package org.example.stady.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.stady.entity.Journal;
import org.example.stady.mapper.JournalMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JournalService {
   private final JournalMapper  mapper;
   public JournalService(JournalMapper mapper) {
       this.mapper = mapper;
   }

    @CacheEvict(value = "journals", allEntries = true)
    public Journal create(Journal journal) {
       mapper.insert(journal);
       return journal;
    }

    @Cacheable(value = "journals", key = "'journal_' + #id")
    public Journal getById(Long id) {
       return mapper.selectById(id);
    }

    @Cacheable(value = "journals", key = "'all'")
    public List<Journal> getAll() {
        QueryWrapper<Journal> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at");
        return mapper.selectList(wrapper);
    }

    public Page<Journal> getPage(Page<Journal> page) {
        QueryWrapper<Journal> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at");
        return mapper.selectPage(page, wrapper);
    }

    @CacheEvict(value = "journals", allEntries = true)
    public Journal update(Journal journal) {
         mapper.updateById(journal);
         return mapper.selectById(journal.getId());
    }

    @CacheEvict(value = "journals", allEntries = true)
    public void delete(Long id) {
       mapper.deleteById(id);
    }
}
