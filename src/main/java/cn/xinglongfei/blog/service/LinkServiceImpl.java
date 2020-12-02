package cn.xinglongfei.blog.service;

import cn.xinglongfei.blog.NotFoundException;
import cn.xinglongfei.blog.dao.LinkCategoryResposiory;
import cn.xinglongfei.blog.dao.LinkResposiory;
import cn.xinglongfei.blog.po.Link;
import cn.xinglongfei.blog.po.LinkCategory;
import cn.xinglongfei.blog.util.MyBeanUtils;
import cn.xinglongfei.blog.vo.LinkQuery;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

/**
 * Created by Phoenix on 2020/11/30
 */
@Service
public class LinkServiceImpl implements LinkService{

    @Autowired
    private LinkResposiory linkResposiory;

    @Autowired
    private LinkCategoryResposiory linkCategoryResposiory;

    @Override
    public Long countLink() {
        return linkResposiory.count();
    }

    @Override
    public Map<String, List<Link>> archiveLink() {
        List<LinkCategory> linkCategoryList = linkCategoryResposiory.findAll();
        //按照优先级排序，优先级相同则按照ID排序
        Collections.sort(linkCategoryList);
        Map<String,List<Link>> listMap = new TreeMap<>();
        List<Link> linkTemp = new ArrayList<>();
        for (LinkCategory linkCategory:linkCategoryList){
            linkTemp = linkResposiory.findAllByLinkCategory(linkCategory);
            Collections.sort(linkTemp);
            listMap.put(linkCategory.getName(),linkTemp);
        }
        return listMap;
    }


    @Override
    public Page<Link> listLink(Pageable pageable) {
        return linkResposiory.findAll(pageable);
    }

    @Override
    public Page<Link> listLink(Pageable pageable, LinkQuery link) {
        return linkResposiory.findAll(new Specification<Link>() {
            @Override
            public Predicate toPredicate(Root<Link> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (!"".equals(link.getTitle()) && link.getTitle() != null) {
                    predicates.add(cb.like(root.<String>get("title"),"%"+link.getTitle()+"%"));
                }
                if(link.getLinkCategoryId() !=null){
                    predicates.add(cb.equal(root.<LinkCategory>get("linkCategory").get("id"),link.getLinkCategoryId()));
                }
                cq.where(predicates.toArray(new Predicate[predicates.size()]));
                return null;
            }
        }, pageable);
    }

    @Override
    public List<Link> listLink() {
        return linkResposiory.findAll();
    }

    @Transactional
    @Override
    public Link saveLink(Link link) {
        if(link.getId() == null){
            link.setCreateTime(new Date());
        }
        return linkResposiory.save(link);
    }

    @Override
    public Link getLink(Long id) {
        return linkResposiory.getOne(id);
    }

    @Transactional
    @Override
    public Link updateLink(Long id, Link link) {
        Link linkTemp = linkResposiory.getOne(id);
        if (linkTemp == null) {
            throw new NotFoundException("该外链不存在");
        }
        BeanUtils.copyProperties(link, linkTemp, MyBeanUtils.getNullPropertyNames(link));
        return linkResposiory.save(linkTemp);
    }

    @Transactional
    @Override
    public void deleteLink(Long id) {
        linkResposiory.deleteById(id);
    }
}
