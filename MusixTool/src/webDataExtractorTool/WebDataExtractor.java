/* Web Information Data Extractor Tool
 * Author: Darragh Carroll 
 * Date: 19/07/2014
 * 
 * Database: SQL
 * Libraries: Hibernate, SQL
 * 
 * Tool will take in a url of a web page to extract data from.
 * Connect to web page and extract data based on requirements.
 * Create a Product object from extracted data and insert into DB.
 * Tool will then retrieve all products from DB and display details to screen.
 * 
 * */

package webDataExtractorTool;

import java.io.IOException;
import java.net.URL;

import javax.swing.text.BadLocationException;

public class WebDataExtractor {

	// This is the main class of the tool which executes all the required
	// methods to create, store and print a new product.
	public static void main(String[] args) {
		DBHandler dbHelper = new DBHandler();
		DataExtractorHelper toolHelper = new DataExtractorHelper();

		// create the database if it does not exist.
		dbHelper.createDB();

		// Create the PRODUCT table if it does not exist.
		dbHelper.createTable();

		URL url;
		Product product = new Product();
		try {
			// Ask user to enter url and store it as variable.
			url = toolHelper.getUrl();

			// Scan web page and create product.
			product = toolHelper.scanWebPage(url);
		} catch (IOException e) {
			handleException(e);
		} catch (BadLocationException e) {
			handleException(e);
		}

		// Insert product into DB.
		dbHelper.insertProduct(product);

		// Retrieve all products from DB and display details.
		toolHelper.printDetails();

		// Exit.
		System.exit(0);
	}

	// Handle any errors that occur.
	private static void handleException(Exception e) {
		System.out
				.println("An error occured, please check the URL is correct.");
		e.printStackTrace();
		// Exit.
		System.exit(0);
	}
}
