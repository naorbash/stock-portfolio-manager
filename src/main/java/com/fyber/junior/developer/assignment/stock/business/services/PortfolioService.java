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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This service responsible to manage all of the logic required while working with the clients portfolios.
 */
@Service
@Transactional
public class PortfolioService {
    private String stockCSVFilePath = "stocks.csv";
    private int supportedStockHistoryInDays = 8;
    private ClientRepository clientRepository;
    private StockRepository stockRepository;

    //Dependency Injection
    @Autowired
    public PortfolioService(ClientRepository clientRepository, StockRepository stockRepository) {
        this.clientRepository = clientRepository;
        this.stockRepository = stockRepository;
    }

    /**
     * This method is responsible to return all the registered stocks.
     *
     * @return List<Stock>
     */

    public List<Stock> getAllStocks() {
        Iterable<Stock> stocks = this.stockRepository.findAll();
        List<Stock> stocksList = new ArrayList<>();
        stocks.forEach(stockInRepository -> stocksList.add(stockInRepository));
        return stocksList;

    }

    /**
     * This method is responsible to return all the registered clients.
     *
     * @return List<Client>
     */

    public List<Client> getAllClients() {
        Iterable<Client> clients = this.clientRepository.findAll();
        List<Client> clientsList = new ArrayList<>();
        clients.forEach(clientInRepository -> clientsList.add(clientInRepository));
        return clientsList;
    }

    /**
     * This service method is responsible to create a new client and attaching to it
     * the incoming portfolio.
     *
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
     *
     * @param clientId     the id of the client which to replace his portfolio
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
     *
     * @param clientId       the id of the client which to update his portfolio
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
     *
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

            //calculate the client's portfolio value
            for (Stock stockInList : clientStocks) {
                Double stockValue = stockInList.getStockAmount() * getStockLastValue(stockInList.getstockSymbol());
                portfolioValue += stockValue;
            }

            //returning the client's portfolio value
            return portfolioValue;

        } else {
            return 0.0;
        }
    }


    public String mostPerformingStock(Long clientId, int pastDays) {

        //If the requested stock history is not supported by the data in the file
        if(!(pastDays<=supportedStockHistoryInDays)){
            throw new BadArgumentException("number of days '" +pastDays+ "' is currently not supported");
        }

        //validating the client id
        validateClient(clientId);

        //get all of the client's stocks
        List<Stock> clientStocks = stockRepository.findByClientId(clientId);

        //if his stocks list is not empty
        if (clientStocks != null && clientStocks.size() > 0) {
            Map<String, List<Double>> stockHistoryMap = getStocksHistoryMap(pastDays);
            for (Map.Entry<String, List<Double>> entry : stockHistoryMap.entrySet())
            {
                System.out.println(entry.getKey() + "/" + entry.getValue());
            }
        }

        return null;
    }


    /**
     * This aid method is responsible to return the last value of a giving stock
     * @param requestedStockSymbol the symbol of the stock to return its value
     * @return Double, the stock's last value
     */
    private Double getStockLastValue(String requestedStockSymbol) {
        File stocksFile = new File(stockCSVFilePath);
        BufferedReader br = null;
        String line ;
        boolean stockFound = false;
        double stockValue = 0;

        try {
            br = new BufferedReader(new FileReader(stocksFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] stockLine = line.split(",");

                //Extracting the stock symbol in the stock-file
                String stockSymbolInFile = stockLine[0];

                //when reaching the requested stock in the file
                if (requestedStockSymbol.equals(stockSymbolInFile)) {
                    stockFound = true;
                    stockValue = Double.parseDouble(stockLine[1]);
                }

                //returning the last value when reaching the final occurrence of the stock in the file.
                if (!requestedStockSymbol.equals(stockSymbolInFile) && stockFound) {
                    return stockValue;
                }
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
        return 0.0;
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
                while ((line = br.readLine()) != null ) {
                    stockLine = line.split(",");

                    //if we are still on the same stock
                    if (stockSymbolInFile.equals(stockLine[0])) {
                        //if we are iterating in the limits of past days,enter the value to the list of values
                        if(i < pastDays){
                            valuesList.add(Double.parseDouble(stockLine[1]));
                            i++;
                        }
                    } else {
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
