package com.fyber.junior.developer.assignment.stock.business.services;

import com.fyber.junior.developer.assignment.stock.business.validator.PortfolioValidator;
import com.fyber.junior.developer.assignment.stock.model.entity.Client;
import com.fyber.junior.developer.assignment.stock.model.entity.Stock;
import com.fyber.junior.developer.assignment.stock.model.repository.ClientRepository;
import com.fyber.junior.developer.assignment.stock.model.repository.StockRepository;
import com.fyber.junior.developer.assignment.stock.rest.Exceptions.BadArgumentException;
import com.fyber.junior.developer.assignment.stock.rest.Exceptions.EntityNotFoundException;
import com.fyber.junior.developer.assignment.stock.rest.Exceptions.InternalServerErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * This service responsible to manage all of the logic required while working with the clients portfolios.
 */
@Service
@Transactional
public class PortfolioService {
    private String stockCSVFilePath = "stocks.csv";
    private int supportedStockHistoryInDays = 8;
    private int numberOfCharactersInAFileRowLimit = 100;
    private ClientRepository clientRepository;
    private StockRepository stockRepository;

    //Dependency Injection
    @Autowired
    public PortfolioService(ClientRepository clientRepository, StockRepository stockRepository) {
        this.clientRepository = clientRepository;
        this.stockRepository = stockRepository;
    }


    /**
     * This service method is responsible to create a new client and attaching to it
     * the incoming portfolio.
     * @param newStockList the new client's portfolio list
     * @return Long the new client's id
     */
    public long addNewClientPortfolio(List<Stock> newStockList) {

        if (newStockList == null || newStockList.size() < 1) {
            throw new BadArgumentException("no stocks found at your request");
        }

        //validating the incoming stocks
        PortfolioValidator.validatePortfolio(newStockList, false);

        //creating a new Client Object with generated id by saving it to the DB
        Client newClient = clientRepository.save(new Client());

        //Connecting the stocks to the new client id
        newStockList.forEach(stock -> stock.setClientId(newClient.getClientId()));

        newClient.setStocksList(newStockList);

        //returning the saved client's id
        return clientRepository.save(newClient).getClientId();
    }

    /**
     * This service method is responsible to replace the entire portfolio of the client
     * to the new incoming portfolio.
     * @param clientId  the id of the client which to replace his portfolio
     * @param newStockList the list of the client's new stocks
     */
    public void replaceClientPortfolio(Long clientId, List<Stock> newStockList) {

        //validating the client id
        validateClient(clientId);

        //getting the client Object from the DB
        Client client = clientRepository.findByClientId(clientId);

        //If new stock list is not empty
        if (newStockList != null && newStockList.size() > 0) {

            //validating the incoming stocks
            PortfolioValidator.validatePortfolio(newStockList, false);

            //deleting the client's old stocks
            stockRepository.deleteByClientId(clientId);

            //Connecting the new stocks to the client
            newStockList.forEach(stock -> stock.setClientId(client.getClientId()));

            //setting the client's new stocks list
            client.setStocksList(newStockList);

            //saving changed client
            clientRepository.save(client);

        } else {
            throw new BadArgumentException("no stocks found at your request");
        }
    }

    /**
     * This service method is responsible to Update all\some of the client's stocks
     * Incoming stocks has to be owned by the user.
     * @param clientId  the id of the client which to update his portfolio
     * @param stocksToUpdate the list of stocks to update
     */
    public void updateClientPortfolio(Long clientId, List<Stock> stocksToUpdate) {

        //validating the client id
        validateClient(clientId);

        //getting the client Object from the DB
        Client client = clientRepository.findByClientId(clientId);

        //If the stock list is not empty
        if (stocksToUpdate != null && stocksToUpdate.size() > 0) {

            //validating the incoming stocks
            PortfolioValidator.validatePortfolio(stocksToUpdate, true);

            //validating the user indeed own all of the incoming stocks
            for (Stock incomingStock : stocksToUpdate) {
                if (!client.getStocksList().contains(incomingStock)) {
                    throw new BadArgumentException("The stock '" + incomingStock.getstockSymbol() +
                            "' doesn't exist in the client's portfolio");
                }
            }

            //for each stock to update
            for (Stock stockToUpdate : stocksToUpdate) {
                // get the matching stock from the DB
                Stock stockInDB = stockRepository.findByStockSymbolAndClientId(
                        stockToUpdate.getstockSymbol(), client.getClientId());

                //if the amount changes to 0, delete it from the client portfolio
                if (stockToUpdate.getStockAmount() == 0) {
                    client.getStocksList().remove(stockInDB);
                    stockRepository.delete(stockInDB);

                    //otherwise, update its value
                } else {
                    stockInDB.setStockAmount(stockToUpdate.getStockAmount());
                }
            }
        } else {
            throw new BadArgumentException("no stocks found at your request");
        }
    }


    /**
     * This service method is responsible to return a client's portfolio value
     * according to the received client's id.
     * @param clientId the client of which to return his portfolio value
     * @return Double the client's portfolio value
     */
    public Double getPortfolioValue(Long clientId) {

        //validating the client id
        validateClient(clientId);

        //get all of the client's stocks
        List<Stock> clientStocks = stockRepository.findByClientId(clientId);

        //if his stocks list is not empty
        if (clientStocks != null && clientStocks.size() > 0) {
            Double portfolioValue = 0.0;

            //getting the stocks-values map passing 1 day back as an argument to get the last value
            Map<String, List<Double>> stockHistoryMap = getStocksHistoryMap(1);

            //calculate the client's portfolio value
            for (Stock clientStock : clientStocks) {
                Double latestStockValue= stockHistoryMap.get(clientStock.getstockSymbol()).get(0);
                Double stockValue = clientStock.getStockAmount() * latestStockValue;
                portfolioValue += stockValue;
            }

            //returning the client's portfolio value
            return portfolioValue;

        }
        throw new EntityNotFoundException("No stocks founded for user '" +clientId+ "'");
    }

    /**
     * This service method is responsible to calculate the most performing client's stock.
     * Most performing stock is the one that raised the most in value during the giving days entered.
     * @param clientId the client of which to calculate the most performing stock.
     * @param pastDays how many days to go back in the history of the stocks values
     * @return The performing stocks symbol
     */
    public String mostPerformingStock(Long clientId, int pastDays) {

        //If the requested stock history is not supported by the data in the file
        if (!(pastDays <= supportedStockHistoryInDays)) {
            throw new BadArgumentException("number of days '" + pastDays + "' is currently not supported");
        }

        //validating the client id
        validateClient(clientId);

        //get all of the client's stocks
        List<Stock> clientStocks = stockRepository.findByClientId(clientId);

        //if his stocks list is not empty
        if (clientStocks != null && clientStocks.size() > 0) {
            String performingStock = null;
            Double highestStockDiff = 0.0;

            //getting the stocks-values map passing pastDays argument which hold the number of days to go back in history
            Map<String, List<Double>> stockHistoryMap = getStocksHistoryMap(pastDays);

            //For each of the client's stock,calculating the difference in value
            for (Stock clientStock : clientStocks) {
                List<Double> stockValues = stockHistoryMap.get(clientStock.getstockSymbol());
                Double currentStockDiff = stockValues.get(0) - stockValues.get(pastDays - 1);
                if (highestStockDiff < currentStockDiff) {
                    highestStockDiff = currentStockDiff;
                    performingStock = clientStock.getstockSymbol();
                }
            }

            //If all stocks fell in value
            if (highestStockDiff == 0.0) {
                return "No stock raised in value in last '" + pastDays + "' days";
            } else {
                return performingStock;
            }

        }
        throw new EntityNotFoundException("No stocks found for client '" + clientId + "'");
    }

    /**
     * This service method is responsible to calculate the most stable client's stock.
     * Most stable stock is the one with least value fluctuation during the giving days entered.
     * @param clientId the client of which to calculate the most stable stock.
     * @param pastDays how many days to go back in the history of the stocks values.
     * @return The most stable stock symbol
     */
    public String mostStableStock(Long clientId, int pastDays) {
        //If the requested stock history is not supported by the data in the file
        if (!(pastDays <= supportedStockHistoryInDays)) {
            throw new BadArgumentException("number of days '" + pastDays + "' is currently not supported");

        }//If the requested stock history is less than 1, most stable stock function cannot perform
        else if (pastDays < 2) {
            throw new BadArgumentException("the minimum past days for this function: '2' ");
        }

        //validating the client id
        validateClient(clientId);

        //get all of the client's stocks
        List<Stock> clientStocks = stockRepository.findByClientId(clientId);

        //if his stocks list is not empty
        if (clientStocks != null && clientStocks.size() > 0) {
            String stableStock;
            Double minStockFluctuation;

            //getting the stocks-values map
            Map<String, List<Double>> stocksHistoryMap = getStocksHistoryMap(pastDays);

            //using the first stock to initializing the minimum fluctuation variable.
            List<Double> firstStockValues = stocksHistoryMap.get(clientStocks.get(0).getstockSymbol());
            Double firstStockMin = firstStockValues.get(0);
            Double firstStockMax = firstStockValues.get(0);
            for (Double value : firstStockValues) {
                firstStockMin = firstStockMin < value ? firstStockMin : value;
                firstStockMax = firstStockMax > value ? firstStockMax : value;
            }
            minStockFluctuation = Math.abs(firstStockMin - firstStockMax);
            stableStock = clientStocks.get(0).getstockSymbol();

            //For the rest of the client's stocks,calculating the fluctuation and finding the minimum.
            for (int i = 1; i < clientStocks.size(); i++) {

                List<Double> stockValues = stocksHistoryMap.get(clientStocks.get(i).getstockSymbol());
                Double stockMin = stockValues.get(0);
                Double stockMax = stockValues.get(0);

                //finding min and max in the stock's values list
                for (Double value : stockValues) {
                    stockMin = stockMin < value ? stockMin : value;
                    stockMax = stockMax > value ? stockMax : value;
                }
                Double currStockFluctuation = Math.abs(stockMax - stockMin);
                if (currStockFluctuation < minStockFluctuation) {
                    minStockFluctuation = currStockFluctuation;
                    stableStock = clientStocks.get(i).getstockSymbol();
                }
            }
            return stableStock;
        }
        throw new EntityNotFoundException("No stocks found for client '" + clientId + "'");
    }

    /**
     * This service method is responsible to return a recommendation to the client
     * of the best (not owned by the client) stock to buy.
     * Best stock is the one that whose current value is the highest among all stocks.
     * @param clientId the client to send the recommendation to according to his portfolio.
     * @return The best stock symbol
     */
    public String bestStock(Long clientId) {

        //validating the client id
        validateClient(clientId);

        //get all of the client's stocks
        List<Stock> clientStocks = stockRepository.findByClientId(clientId);

        //getting the stocks-values map passing 1 day back as an argument to get the last value
        Map<String, List<Double>> stockHistoryMap = getStocksHistoryMap(1);

            Double highestStockValue = 0.0;
            String bestStockSymbol = null;

            //looping on all of the existing stocks
            for (Map.Entry<String, List<Double>> entry : stockHistoryMap.entrySet()) {

                //if the current stock is not owned by the user, perform the max comparison
                if (!clientStocks.contains(new Stock(entry.getKey()))) {
                    Double currStockLastValue = entry.getValue().get(0);
                    if(highestStockValue<currStockLastValue){
                        highestStockValue = currStockLastValue;
                        bestStockSymbol = entry.getKey();
                }
            }
        }
        return bestStockSymbol;
    }



    /**
     * This aid method is responsible to validate if a client exist
     * @param clientId the client's id
     */
    private void validateClient(Long clientId){
        if (clientId == null || clientId < 0) {
            throw new BadArgumentException("client id '" + clientId + "' is not valid");
        }else if(clientRepository.findByClientId(clientId)==null){
            throw new EntityNotFoundException("client id '" + clientId + "' does not exist");
        }
    }

    /**
     * This aid method is responsible to build a map where the key is a stock symbol
     * and the value is a List of all the stock's values in the past days.
     * the size of the list represents the amount of days back and passes as an argument.
     * @param pastDays how many days to go back in the history of the stock's values
     * @return Map<String, List<Double>>, The stocks map
     */
    private Map<String, List<Double>> getStocksHistoryMap(int pastDays) {
        File stocksFile = new File(stockCSVFilePath);
        Map<String, List<Double>> stocksMap = new HashMap<>();
        BufferedReader br = null;
        String line;

        try {
            br = new BufferedReader(new FileReader(stocksFile));
            while ((line = br.readLine()) != null) {

                //creating a list that will contains all previous values of the stock
                List<Double> valuesList = new ArrayList<>();

                // use comma as separator
                String[] stockLine = line.split(",");

                //Extracting the stock symbol in the stock-file
                String stockSymbolInFile = stockLine[0];

                //Extracting the first value of that giving stock
                valuesList.add(Double.parseDouble(stockLine[1]));

                //Running on an inner loop on all of the founded stock appearances and saving their values
                //loop is limited to the pastDays variable
                int i = 1;
                while ((line = br.readLine()) != null) {
                    stockLine = line.split(",");

                    //if we are still on the same stock
                    if (stockSymbolInFile.equals(stockLine[0])) {
                        //if we are iterating in the limits of past days,enter the value to the list of values
                        if (i < pastDays) {
                            valuesList.add(Double.parseDouble(stockLine[1]));
                            i++;
                        }
                        //marking to current stock line
                        br.mark(numberOfCharactersInAFileRowLimit);
                    } else {
                        //When reaching a new stock, resetting the reader one line back for next iteration
                        br.reset();
                        break;
                    }
                }
                //Inserting the stock and its list of value-history to the map
                stocksMap.put(stockSymbolInFile, valuesList);
            }
        } catch (IOException e) {
            throw new InternalServerErrorException("Error while processing your request, please try again later");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new InternalServerErrorException("Error while processing your request, please try again later");
                }
            }
        }
        return stocksMap;
    }

}
