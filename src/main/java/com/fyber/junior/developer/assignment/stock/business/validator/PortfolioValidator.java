package com.fyber.junior.developer.assignment.stock.business.validator;

import com.fyber.junior.developer.assignment.stock.model.entity.Stock;
import com.fyber.junior.developer.assignment.stock.rest.Exceptions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class PortfolioValidator
{
    private static String supportedStocksFilePath = "supportedStocks.txt";

    /**
     * This Function is responsible to validate a given portfolio
     * Will throw a fitting exception if the portfolio is violating the defined validation rules
     * @param stockList a list of the incoming stocks
     * @throws BadArgumentException,EntityNotFoundException
     */
    public static void validatePortfolio(List<Stock> stockList){
        stockList.forEach(stock->{
            validateAmount(stock);
            validateSymbolPattern(stock);
            validateStockInFile(stock);
        });

        validateRepeatingStocks(stockList);
    }

    /**
     * This method is responsible to check that the amount of the stock is positive
     * @param stockToValidate the stock to validate
     */
    private static void validateAmount(Stock stockToValidate){
        if(stockToValidate.getStockAmount()<1){
            throw new BadArgumentException("Invalid amount '" +stockToValidate.getStockAmount()+ "' for stock '" +
                    stockToValidate.getstockSymbol()+ "'");
        }
    }

    /**
     * This method is responsible to check if the pattern of the stock is legit
     * @param stockToValidate the stock to validate
     */
    private static void validateSymbolPattern(Stock stockToValidate){
        if(!stockToValidate.getstockSymbol().matches("^[A-Z]*$")){
            throw new BadArgumentException("Invalid stock symbol'" +stockToValidate.getstockSymbol()+ "'");
        }
    }

    /**
     * This method is responsible to check if the stock is supported by the system.
     * It is doing so by checking in the supported-stocks file.
     * @param stockToValidate the stock to validate
     */
    private static void validateStockInFile(Stock stockToValidate){
        File supportedStocksFile = new File(supportedStocksFilePath);
        BufferedReader br = null;
        String line = "";
        boolean stockFound = false;

        try {
            br = new BufferedReader(new FileReader(supportedStocksFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] stocksSymbolsInFile= line.split(",");

                //for every stock-symbol in file
                for (String stockSymbol:stocksSymbolsInFile)
                {
                    //check if the incoming stock symbol equals to it
                    if(stockSymbol.equals(stockToValidate.getstockSymbol())){
                        stockFound = true;
                        break;
                    }
                }
                if(!stockFound){
                    throw new BadArgumentException("The stock '" +stockToValidate.getstockSymbol()+ "' is not supported");
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
    }

    /**
     * This method is responsible to check there is no repeating in the incoming stocks.
     * @param incomingStockList the list of the incoming stocks
     */
    private static void validateRepeatingStocks(List<Stock> incomingStockList){

        for (int i =0; i <incomingStockList.size();i++)
            for(int j=i+1; j<incomingStockList.size();j++)
            {
                if(incomingStockList.get(i).getstockSymbol().equals(incomingStockList.get(j).getstockSymbol())){
                    throw new BadArgumentException("The stock '" +incomingStockList.get(i).getstockSymbol()+
                            "' appears more than once");
                }
            }
    }
}
