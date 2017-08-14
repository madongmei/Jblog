package cn.tzy.Jblog.controller;

import cn.tzy.Jblog.model.*;
import cn.tzy.Jblog.service.ArticleService;
import cn.tzy.Jblog.service.TagService;
import cn.tzy.Jblog.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

/**
 * Created by tuzhenyu on 17-8-14.
 * @author tuzhenyu
 */
@Controller
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @Autowired
    private TagService tagService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/page/{pageId}")
    public String article(Model model, @PathVariable("pageId")int pageId){
        List<Article> articles = articleService.getLatestArticles((pageId-1)*4,4);
        ViewObject pagination = new ViewObject();
        int count = articleService.getArticleCount();

        pagination.set("current",pageId);
        pagination.set("nextPage",pageId+1);
        pagination.set("prePage",pageId-1);
        pagination.set("lastPage",count/4+1);

        User user = hostHolder.getUser();
        if (user==null||"admin".equals(user.getRole())){
            model.addAttribute("create",1);
        }else {
            model.addAttribute("create",0);
        }

        model.addAttribute("articles",articles);
        model.addAttribute("pagination",pagination);
        return "index";
    }

    @RequestMapping("/article/add")
    public String addArticle(@RequestParam("title")String title,@RequestParam("category")String category,
                             @RequestParam("tag")String tag,@RequestParam("describe")String describe,
                             @RequestParam("content")String content){
        Article article = new Article();
        article.setTitle(title);
        article.setDescribes(describe);
        article.setCreatedDate(new Date());
        article.setCommentCount(0);
        article.setContent(WendaUtil.tranfer(content));
        article.setCategory(category);
        int articleId = articleService.addArticle(article);

        String[] tags = tag.split(",");
        for (String t : tags){
            Tag tag1 = tagService.selectByName(t);
            if (tag1==null){
                Tag tag2 = new Tag();
                tag2.setName(t);
                tag2.setCount(1);
                int tagId = tagService.addTag(tag2);

                ArticleTag articleTag = new ArticleTag();
                articleTag.setTagId(tagId);
                articleTag.setArticleId(articleId);
                tagService.addArticleTag(articleTag);
            }else {
                tagService.updateCount(tag1.getId(),tag1.getCount()+1);

                ArticleTag articleTag = new ArticleTag();
                articleTag.setTagId(tag1.getId());
                articleTag.setArticleId(articleId);
                tagService.addArticleTag(articleTag);
            }
        }

        return "redirect:/";
    }
}