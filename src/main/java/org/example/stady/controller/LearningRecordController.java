package org.example.stady.controller;

import org.example.stady.common.Result;
import org.example.stady.entity.LearningRecord;
import org.example.stady.service.LearningRecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
public class LearningRecordController {

    private final LearningRecordService service;

    public LearningRecordController(LearningRecordService service) {
        this.service = service;
    }

    @PostMapping
    public Result<LearningRecord> create(@RequestBody LearningRecord record) {
        return Result.ok(service.create(record));
    }

    @GetMapping("/{id}")
    public Result<LearningRecord> getById(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @GetMapping
    public Result<List<LearningRecord>> getAll() {
        return Result.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public Result<LearningRecord> update(@PathVariable Long id, @RequestBody LearningRecord record) {
        record.setId(id);
        return Result.ok(service.update(record));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }
}
