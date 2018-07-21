package com.fyber.junior.developer.assignment.stock.rest.resources;

import com.fyber.junior.developer.assignment.stock.business.services.PortfolioService;
import com.fyber.junior.developer.assignment.stock.model.entity.Client;
import com.fyber.junior.developer.assignment.stock.model.entity.Stock;
import org.hibernate.annotations.GeneratorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/portfolio")
public class PortfolioResource
{
    @Autowired
    public RequestMappingHandlerMapping requestMappingHandlerMapping;

    private PortfolioService portfolioService;

    @Autowired
    public void setPortfolioService(PortfolioService portfolioService){
        this.portfolioService = portfolioService;
    }



    @GetMapping(path="/performance/{clientId}",params = {"pastDays"})
    public String getMostPerformingStock(@PathVariable long clientId,@RequestParam("pastDays") Integer pastDays){
        return portfolioService.mostPerformingStock(clientId,pastDays);
    }

    @GetMapping(path="/value/{clientId}")
    public double getClientPortfolioValue(@PathVariable long clientId){
        return portfolioService.getPortfolioValue(clientId);
    }

    @PostMapping()
    public Long createNewPortfolio(@Valid @RequestBody List<Stock> listOfNewStocks){
        return portfolioService.addNewClientPortfolio(listOfNewStocks);
    }

    @PutMapping(path="/replace/{clientId}")
    public void replacePortfolio(@Valid @RequestBody List<Stock> listOfNewStocks, @PathVariable long clientId){
        portfolioService.replaceClientPortfolio(clientId,listOfNewStocks);
    }

    @PutMapping(path="/update/{clientId}")
    public void updatePortfolio(@Valid @RequestBody List<Stock> listOfNewStocks, @PathVariable long clientId){
        portfolioService.updateClientPortfolio(clientId,listOfNewStocks);
    }


    @GetMapping(path="/stocks")
    public List<Stock> getAllStocks(){
        return portfolioService.getAllStocks();
    }

    @GetMapping(path="/clients")
    public List<Client> getAllClients(){
        return portfolioService.getAllClients();
    }

    @RequestMapping("/rest")
    public @ResponseBody
    Object showEndpointsAction() throws SQLException
    {
        return requestMappingHandlerMapping.getHandlerMethods().keySet().stream().map(t ->
                (t.getMethodsCondition().getMethods().size() == 0 ? "GET" : t.getMethodsCondition().getMethods().toArray()[0]) + " " +
                        t.getPatternsCondition().getPatterns().toArray()[0]
        ).toArray();
    }

}
