package com.arcrecord.service;

import com.arcrecord.entity.Role;
import com.arcrecord.entity.RoleCollection;
import com.arcrecord.mapper.HeroMapper;
import com.arcrecord.service.impl.HeroServiceimpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class HeroService implements HeroServiceimpl {
    @Resource
    private HeroMapper heroMapper;

    @Override
    public List<Role> getHeroList() {
        return heroMapper.getHeroList();
    }

    @Override
    public Role getHeroById(String id) {
        return heroMapper.getHeroById(id);
    }

    @Override
    public Integer insertHero(Role hero) {
        return heroMapper.insertHero(hero);
    }

    @Override
    public Integer updateHero(Role hero) {
        return heroMapper.updateHero(hero);
    }

    @Override
    public Integer deleteHero(String id) {
        return heroMapper.deleteHero(id);
    }

    public List<RoleCollection> getRoleCollectionByHeroId(String heroId){
        return heroMapper.getRoleCollectionByHeroId(heroId);
    };

}
