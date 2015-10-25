package webDataExtractorTool;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

//This is the database helper class containing all the reuired methods for interacting with the DB.
public class DBHandler {

	private static ServiceRegistry serviceRegistry;
	private static SessionFactory factory;

	private static final String DB_URL = "jdbc:mysql://localhost:3306/";
	private static final String DB_NAME = "PRODUCTS";
	private static final String PRODUCT_TABLE = "PRODUCT";
	// Database credentials
	private static final String USER = "admin";
	private static final String PASSWORD = "dbadmin";

	// This method will check if the PRODUCTS DB exists and if not it will
	// create it.
	public void createDB() {
		Connection dbConn = null;
		PreparedStatement stmt = null;
		try {

			Boolean dbExists = false;
			// Connect to database server.
			dbConn = DriverManager.getConnection(DB_URL, USER, PASSWORD);

			ResultSet resultSet = dbConn.getMetaData().getCatalogs();
			// Check if the DB already exists.
			while (resultSet.next()) {
				String databaseName = resultSet.getString(1);
				if (databaseName.equalsIgnoreCase(DB_NAME)) {
					dbExists = true;
				}
			}
			// If the DB does not exist then create one.
			if (!dbExists) {
				stmt = dbConn.prepareStatement("CREATE DATABASE PRODUCTS");
				stmt.execute();
				stmt.close();
			}
			resultSet.close();
			dbConn.close();
		} catch (SQLException se) {
			// Handle any SQL errors.
			se.printStackTrace();
			// Exit.
			System.exit(0);
		}
	}

	// Create PRODUCT table if it does not exist.
	public void createTable() {
		Connection dbConn = null;
		PreparedStatement stmt = null;

		try {
			// Connect to the database.
			dbConn = DriverManager.getConnection(DB_URL + DB_NAME, USER,
					PASSWORD);
			DatabaseMetaData dbm = dbConn.getMetaData();
			// check if PRODUCT table exists.
			ResultSet resultSet = dbm
					.getTables(null, null, PRODUCT_TABLE, null);

			if (resultSet.next()) {
				return;
			} else {
				// If the PRODUCT table does not exist then create it.
				stmt = dbConn
						.prepareStatement("create table PRODUCT (id INT NOT NULL auto_increment, product_name VARCHAR(255) default NULL, item_num INT default NULL, price DOUBLE default NULL, PRIMARY KEY (id))");
				stmt.executeUpdate();
				stmt.close();
			}
			resultSet.close();
			dbConn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// Exit.
			System.exit(0);
		}
	}

	// Insert the product into the DB with unique identifier id.
	public int insertProduct(Product product) {
		Transaction tx = null;
		Integer productId = null;

		Session session = createSession();

		try {
			tx = session.beginTransaction();
			// Insert the product into the DB and return the unique id.
			productId = (Integer) session.save(product);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			// Exit.
			System.exit(0);
		} finally {
			session.close();
		}
		return productId;
	}

	// Retrieve all products from the DB.
	@SuppressWarnings("unchecked")
	public Collection<Product> getAllProducts() {
		Transaction tx = null;
		Collection<Product> products = null;

		Session session = createSession();

		try {
			tx = session.beginTransaction();
			// Search DB for all products and return as a collection.
			Criteria criteria = session.createCriteria(Product.class);
			products = criteria.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			// Exit.
			System.exit(0);
		} finally {
			session.close();
		}
		return products;
	}

	// Retrieve a product using the id Primary key
	public Product getProduct(int id) {
		Transaction tx = null;
		Product product = null;

		Session session = createSession();

		try {
			// Retrieve product from DB using the unique id.
			tx = session.beginTransaction();
			product = (Product) session.get(Product.class, id);
			tx.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			if (tx != null) {
				tx.rollback();
			}
			// Exit.
			System.exit(0);
		} finally {
			session.close();
		}
		return product;
	}

	// Create the Session using sessionFactory and serviceRegistyBuilder
	private Session createSession() {
		Configuration configuration = new Configuration();
		configuration.configure();
		serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
				configuration.getProperties()).build();
		factory = configuration.buildSessionFactory(serviceRegistry);
		return factory.openSession();
	}
}
