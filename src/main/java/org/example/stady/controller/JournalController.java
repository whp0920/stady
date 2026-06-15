package org.example.stady.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.stady.common.Result;
import org.example.stady.entity.Journal;
import org.example.stady.service.JournalService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journal")
public class JournalController {

    private final JournalService service;

    public JournalController(JournalService service) {
        this.service = service;
    }

    @PostMapping
    public Result<Journal> create(@RequestBody Journal journal) {
        return Result.ok(service.create(journal));
    }

    @GetMapping("/page")
    public Result<Page<Journal>> getPage(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        Page<Journal> p = new Page<>(page, size);
        return Result.ok(service.getPage(p));
    }

    @GetMapping("/{id}")
    public Result<Journal> getById(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @GetMapping
    public Result<List<Journal>> getAll() {
        return Result.ok(service.getAll());
    }
    @PutMapping("/{id}")
    public Result<Journal> update(@PathVariable Long id, @RequestBody Journal journal) {
        journal.setId(id);
        return Result.ok(service.update(journal));
    }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }


}
