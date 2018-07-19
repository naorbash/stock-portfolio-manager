package com.fyber.junior.developer.assignment.stock.business.services;

import com.fyber.junior.developer.assignment.stock.business.validator.PortfolioValidator;
import com.fyber.junior.developer.assignment.stock.model.entity.Client;
import com.fyber.junior.developer.assignment.stock.model.entity.Stock;
import com.fyber.junior.developer.assignment.stock.model.repository.ClientRepository;
import com.fyber.junior.developer.assignment.stock.model.repository.StockRepository;
import com.fyber.junior.developer.assignment.stock.rest.Exceptions.BadArgumentException;
import com.fyber.junior.developer.assignment.stock.rest.Exceptions.ConflictException;
import com.fyber.junior.developer.assignment.stock.rest.Exceptions.EntityNotFoundException;
import com.fyber.junior.developer.assignment.stock.rest.Exceptions.InternalServerErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This service responsible to manage all of the logic required while working with the clients portfolios.
 */
@Service
public class PortfolioService
{
    private String stockCSVFilePath = "stocks.csv";

    private ClientRepository clientRepository;
    private StockRepository stockRepository;

    //Dependency Injection
    @Autowired
    public PortfolioService(ClientRepository clientRepository, StockRepository stockRepository)
    {
        this.clientRepository = clientRepository;
        this.stockRepository = stockRepository;
    }

    /**
     * This service method is responsible to create a new client and attaching to it
     * the incoming portfolio.
     * @param newStockList
     * @return Long the new client's id
     */
    public long addNewClientPortfolio(List<Stock> newStockList) {

        if (newStockList == null || newStockList.size() < 1) {
            throw new BadArgumentException("no stocks found at your request");
        }

        //validating the incoming stocks
        PortfolioValidator.validatePortfolio(newStockList);

        //creating a new Client Object with generated id by saving it to the DB
        Client newClient = clientRepository.save(new Client());

        //Connecting the stocks to the new client id
        newStockList.forEach(stock ->stock.setClientId(newClient.getClientId()));

        newClient.setStocksList(newStockList);

        //returning the saved client's id
        return clientRepository.save(newClient).getClientId();
    }

    public void replaceClientPortfolio(Long clientId, List<Stock> newStockList) {
        //if id is valid
        if (clientId != null && clientId > 0) {

            //if client exist in the DB
            Client client = clientRepository.findByClientId(clientId);
            if (client != null) {

                //If new stock list is not empty
                if (newStockList != null && newStockList.size() > 1) {

                    //validating the incoming stocks
                    PortfolioValidator.validatePortfolio(newStockList);

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
            } else {
                throw new EntityNotFoundException("client id '" + clientId + "' does not exist");
            }
        } else {
            throw new BadArgumentException("client id '" + clientId + "' is not valid");
        }
    }

    /**
     * This service method is responsible to return a client's portfolio value
     * according to the received client's id.
     * @param clientId
     * @return Double the client's portfolio value
     */
    public Double getPortfolioValue(Long clientId) {
        //if id is valid
        if (clientId != null && clientId > 0) {

            //if client exist in the DB
            if (clientRepository.findByClientId(clientId) != null) {
                //get all of his stocks
                List<Stock> clientStocks = stockRepository.findByClientId(clientId);

                //if his stocks list is not empty
                if (clientStocks != null && clientStocks.size() > 1) {
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
            } else {
                throw new EntityNotFoundException("client id '" + clientId + "' does not exist");
            }
        } else {
            throw new BadArgumentException("client id '" + clientId + "' is not valid");
        }
    }

    /**
     * This aid method is responsible to return the last value of a giving stock
     * @param requestedStockSymbol
     * @return Double the stock's last value
     */
    private Double getStockLastValue(String requestedStockSymbol) {
        File stocksFile = new File(stockCSVFilePath);
        BufferedReader br = null;
        String line = "";
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
     * This method is responsible to return all the registered stocks.
     *
     * @return List<Stock>
     */

    public List<Stock> getAllStocks()
    {
        Iterable<Stock> stocks = this.stockRepository.findAll();
        List<Stock> stocksList = new ArrayList<>();
        stocks.forEach(stockInRepository ->
        {
            stocksList.add(stockInRepository);
        });
        return stocksList;
    }

    /**
     * This method is responsible to return all the registered clients.
     *
     * @return List<Client>
     */

    public List<Client> getAllClients()
    {
        Iterable<Client> clients = this.clientRepository.findAll();
        List<Client> ClientsList = new ArrayList<>();
        clients.forEach(clientInRepository ->
        {
            ClientsList.add(clientInRepository);
        });
        return ClientsList;
    }


    /**
     * This method is responsible to return all of the Client entities in the Clients table.
     * @return List<Client>
     */
    /*
    public List<Client> getAll() {
        Iterable<Client> clients = this.clientRepository.findAll();
        List<Client> clientsList = new ArrayList<>();
        clients.forEach(clientInRepository -> {
            clientsList.add(clientInRepository);
        });
        return clientsList;
    }
    */


    /**
     * This method is responsible to return a client object by a giving machine-name.
     * @param machineName
     * @return Client on successes
     * @throws EntityNotFoundException on failure.
     */
    /*
    public Client getClientByMachineName(String machineName) {
        if (machineName != null && !machineName.equals("")) {
            Client client = clientRepository.findByMachineName(machineName);
            if (client != null) {
                return client;
            }
        }
        throw new EntityNotFoundException("machine name not found: " + machineName);
    }
    */

    /**
     * This method is responsible to create a new client in the Client Table
     * Returning the created object
     * @param clientToCreate
     * @return Client on successes, null on failure.
     * @throws ConflictException on failure.
     */
    /*
    public Client addClient(Client clientToCreate) {
        String machineName = clientToCreate.getMachineName();

        //If client instance dose not yet exist,create a new Client instance and save it.
        if (clientRepository.findByMachineName(machineName) == null) {
            Client createdClient = clientRepository.save(clientToCreate);
            return createdClient;
        }
        throw new ConflictException("Conflict with client: " + machineName);
    }
*/

    /**
     * This method is responsible to update an existing client in the Clients Table
     * Returning the updated object
     * @param clientToUpdate
     * @return Client on successes.
     * @throws EntityNotFoundException on failure.
     */
    /*
    public Client updateClient(Client clientToUpdate) {
        Client clientToChange = getClientByMachineName(clientToUpdate.getMachineName());
        if (clientToChange != null) {
            Client updatedClient = clientRepository.save(clientToUpdate);
            return updatedClient;
        }
        throw new EntityNotFoundException("machine name not found: " + clientToUpdate.getMachineName());
    }
    */

    /**
     * This method is responsible to delete a client by a giving machine-name from the Clients table.
     * @param machineName
     * @return boolean true on successes.
     * @throws EntityNotFoundException on failure.
     */
    /*
    public boolean deleteClientByMachineName(String machineName) {
            Client clientToDelete = clientRepository.findByMachineName(machineName);
            if (clientToDelete != null) {
                clientRepository.delete(clientToDelete);
                return true;
            }
        throw new EntityNotFoundException("machine not found: " + machineName);
    }
    */

}
