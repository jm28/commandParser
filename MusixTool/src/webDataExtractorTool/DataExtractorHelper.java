package webDataExtractorTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

//This is a helper class containing various methods for creating a new product.
public class DataExtractorHelper {

	public URL url;
	private DBHandler dbHelper = new DBHandler();

	// Specify the HTML elements to search for to extract required data.
	private static final String HTML_ITEM_PROP = "itemprop";
	private static final String HTML_ITEM_NAME = "name";
	private static final String HTML_ITEM_NUMBER = "tr-prod-artnr";
	private static final String HTML_ITEM_PRICE = "price";

	// Ask user to enter url and read back next line entered.
	public URL getUrl() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out
				.println("Please enter url to add a product or 'print' to view existing products: ");
		// Read line entered by user and convert to URL or if 'print' print
		// all products to screen and exit.
		String userEntry = br.readLine();
		if ("print".equals(userEntry)) {
			printDetails();
			// Exit.
			System.exit(0);
		} else {
			url = new URL(userEntry);
			return url;
		}
		return url;
	}

	// Create HTML document from web page. Then search document to find required
	// values to create product.
	public Product scanWebPage(URL url) throws IOException,
			BadLocationException {
		Element element;
		String productName = "";
		int itemNum = 0;
		Double price = 0.0;

		// Retrieve HTML document from web page so that the required information
		// can be retrieved.
		HTMLEditorKit htmlKit = new HTMLEditorKit();
		HTMLDocument htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
		htmlDoc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
		Reader rd = new InputStreamReader(url.openConnection().getInputStream());
		htmlKit.read(rd, htmlDoc, 0);
		ElementIterator iterator = new ElementIterator(htmlDoc);

		// Loop through all elements of the HTML doc searching for the required
		// values.
		while ((element = iterator.next()) != null) {
			AttributeSet as = element.getAttributes();
			Object elementName = as.getAttribute(StyleConstants.NameAttribute);
			// Extract data by finding the specified HTML element by matching on
			// attributes and element names.
			// Once the required HTML element is found retrieve text.
			if (elementName == HTML.Tag.H1
					&& HTML_ITEM_NAME.equals(as.getAttribute(HTML_ITEM_PROP))) {
				productName = getHtmlString(element, htmlDoc);
			}
			if (elementName == HTML.Tag.TD
					&& HTML_ITEM_NUMBER.equals(as
							.getAttribute(HTML.Attribute.ID))) {
				itemNum = Integer.parseInt(getHtmlString(element, htmlDoc)
						.replaceAll("[^\\d]", ""));
			}
			if (elementName == HTML.Tag.DIV
					&& HTML_ITEM_PRICE.equals(as.getAttribute(HTML_ITEM_PROP))) {
				String strPrice = getHtmlString(element, htmlDoc);
				// Replace all non-digits except for ','.
				strPrice = strPrice.replaceAll("[^\\d,]+", "");
				// Replace ',' with '.' to create decimal point for double.
				price = Double.parseDouble(strPrice.replaceAll(",", "\\."));
			}
		}
		// Create a new product with the extracted information.
		return createProduct(productName, itemNum, price);
	}

	// Once the desired HTML element has been found, extract the string value.
	private String getHtmlString(Element element, HTMLDocument htmlDoc)
			throws BadLocationException {
		Element child = element.getElement(0);
		int startOffset = child.getStartOffset();
		int endOffset = child.getEndOffset();
		int length = endOffset - startOffset;
		return htmlDoc.getText(startOffset, length);
	}

	// Create a new product using values from webpage.
	public Product createProduct(String productName, int itemNum, double price) {
		Product product = new Product();
		product.setProductName(productName);
		product.setItemNum(itemNum);
		product.setPrice(price);
		return product;
	}

	// Retrieve all the products in the DB and print details to screen
	public void printDetails() {
		Collection<Product> products = dbHelper.getAllProducts();
		for (Product product : products) {
			System.out.println("Product Id: " + product.getId());
			System.out.println("Product Name: " + product.getProductName());
			System.out.println("Product Item Number: " + product.getItemNum());
			System.out.println("Product Price: " + product.getPrice() + "\n");
		}
	}

}
