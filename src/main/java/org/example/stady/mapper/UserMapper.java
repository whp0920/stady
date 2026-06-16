package org.example.stady.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.stady.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
