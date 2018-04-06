package services;

import domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import repositories.NewsPaperRepository;
import security.Authority;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Service
@Transactional
public class NewsPaperService {
    // Managed repository -----------------------------------------------------

    @Autowired
    private NewsPaperRepository newsPaperRepository;

    // Supporting services ----------------------------------------------------

    @Autowired
    private UserService userService;

    @Autowired
    private AdministratorService administratorService;

    @Autowired
    private ArticleService articleService;

    // Constructors -----------------------------------------------------------

    public NewsPaperService() {
        super();
    }

    // Simple CRUD methods ----------------------------------------------------

    public NewsPaper create() {
        NewsPaper res= null;
        User publisher= null;
        Collection<Article> articles= new ArrayList<>();
        Collection<Customer> customers= new ArrayList<>();
        publisher=userService.findByPrincipal();
        res= new NewsPaper();
        res.setPublisher(publisher);
        res.setArticles(articles);
        res.setCustomers(customers);
       // publisher.getNewsPapers().add(res);

        return res;
    }

    public NewsPaper save(NewsPaper newsPaper){
        NewsPaper res= null;
        Assert.isTrue(checkByPrincipal(newsPaper));
        res= newsPaperRepository.save(newsPaper);
        return res;
    }

    public Collection<NewsPaper> findAll(){
        Collection<NewsPaper> res= null;
        res= this.newsPaperRepository.findAll();
        return  res;
    }

    public NewsPaper findOne(int newsPaperId){
        NewsPaper res= null;
        res= this.newsPaperRepository.findOne(newsPaperId);
        return  res;
    }
    public NewsPaper findOneToEdit(int newsPaperId){
        NewsPaper res= null;
        res= this.newsPaperRepository.findOne(newsPaperId);
        Assert.isTrue(checkByPrincipal(res) || checkByPrincipalAdmin(res));
        return  res;
    }

    public void delete(NewsPaper newsPaper){
        Assert.notNull(newsPaper);
        Assert.isTrue(checkByPrincipalAdmin(newsPaper) || checkByPrincipal(newsPaper));
        this.articleService.deleteAll(newsPaper.getArticles());
        this.newsPaperRepository.delete(newsPaper);
    }

    // Other business methods -------------------------------------------------

    public void findOneToPublish(NewsPaper newsPaper){
        Collection<Article> articles= newsPaper.getArticles();
        for(Article a:articles){
            if(a.getFinalMode()){
                newsPaper.setPublished(true);
                newsPaper.setPublicationDate(new Date());
                a.setMoment(new Date());
            }else{
                newsPaper.setPublished(false);
            }
        }
    }

    public boolean checkByPrincipal(NewsPaper newsPaper) {
        Boolean res = null;
        User principal = null;

        res = false;
        principal = this.userService.findByPrincipal();

        if (newsPaper.getPublisher().equals(principal))
            res = true;

        return res;
    }

    public boolean checkByPrincipalAdmin(NewsPaper newsPaper){
        Boolean res= false;
        Administrator administrator = administratorService.findByPrincipal();
        if(administrator!=null) {
            Collection<Authority> authorities = administrator.getUserAccount().getAuthorities();
            String authority = authorities.toArray()[0].toString();
            res = authority.equals("ADMINISTRATOR");
        }
        return res;

    }

    public Collection<NewsPaper> findNewsPapersPrivate(int customerId){

        Collection<NewsPaper> result;
        result = newsPaperRepository.findNewsPapersPrivate(customerId);
        return result;
    }

    public Collection<NewsPaper> findPublishedNewsPaper(){
        return newsPaperRepository.findPublishedNewsPaper();
    }

    public Collection<NewsPaper> findAllNewsPaperByUserAndNotPublished(int userId) {
        return this.newsPaperRepository.findAllNewsPaperByUserAndNotPublished(userId);
    }
}
