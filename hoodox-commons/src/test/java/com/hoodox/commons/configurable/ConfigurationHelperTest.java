package com.hoodox.commons.configurable;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.FileNotFoundException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import javax.xml.parsers.DocumentBuilderFactory;

import org.h2.jdbcx.JdbcDataSource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.hoodox.commons.exceptions.XMLAccessException;

public class ConfigurationHelperTest {

	private JdbcDataSource dataSource;

	@BeforeClass
	public void createConfigRepository() throws Exception {
		dataSource = new JdbcDataSource();
		dataSource.setURL("jdbc:h2:mem:configRepository;DB_CLOSE_DELAY=-1");

		Connection connection = dataSource.getConnection();
		try {
			Statement create = connection.createStatement();
			create.execute("create table config_repository ("
					+ "APPLICATION_NAME varchar(50) not null, SERVER_ID varchar(50) not null, "
					+ "FILE_NAME varchar(50) not null, CONTENT clob not null, "
					+ "primary key (APPLICATION_NAME, SERVER_ID, FILE_NAME))");
			create.close();

			PreparedStatement insert = connection
					.prepareStatement("insert into config_repository values (?, ?, ?, ?)");
			insert.setString(1, "app");
			insert.setString(2, "server");
			insert.setString(3, "sample.xml");
			insert.setString(4, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><fromDatabase id=\"d-1\"/>");
			insert.executeUpdate();
			insert.close();
		} finally {
			connection.close();
		}
	}

	@Test
	public void findsFileAtClasspathRoot() throws Exception {
		ConfigurationHelper helper = new ConfigurationHelper();
		helper.createDomFromFile("sample.xml");

		Element root = parseRoot(helper.getDomAsString());
		assertEquals(root.getTagName(), "sample");
		assertEquals(root.getAttribute("id"), "s-1");
		assertEquals(root.getAttribute("owner"), "operon");
		Element item = (Element) root.getElementsByTagName("item").item(0);
		assertEquals(item.getAttribute("name"), "first");
		assertEquals(item.getTextContent(), "one");
	}

	@Test
	public void findsFileUnderMetaInf() throws Exception {
		ConfigurationHelper helper = new ConfigurationHelper();
		helper.createDomFromFile("meta-only.xml");

		Element root = parseRoot(helper.getDomAsString());
		assertEquals(root.getTagName(), "metaOnly");
		assertEquals(root.getAttribute("id"), "m-1");
	}

	@Test(expectedExceptions = FileNotFoundException.class)
	public void missingFileIsFileNotFound() throws Exception {
		new ConfigurationHelper().createDomFromFile("no-such-file.xml");
	}

	@Test(expectedExceptions = XMLAccessException.class)
	public void malformedXmlIsXmlAccessError() throws Exception {
		new ConfigurationHelper().createDomFromFile("malformed.xml");
	}

	@Test
	public void useDatabaseReturnsTheRowsXml() throws Exception {
		ConfigurationHelper helper = databaseHelper();
		// a file of the same name is on the classpath: the row must win
		helper.createDomFromFile("sample.xml");

		Element root = parseRoot(helper.getDomAsString());
		assertEquals(root.getTagName(), "fromDatabase");
		assertEquals(root.getAttribute("id"), "d-1");
	}

	@Test
	public void useDatabaseWithoutARowIsXmlAccessError() throws Exception {
		try {
			databaseHelper().createDomFromFile("meta-only.xml");
			fail("expected XMLAccessException");
		} catch (XMLAccessException e) {
			assertTrue(e.getMessage().contains("app"), e.getMessage());
			assertTrue(e.getMessage().contains("server"), e.getMessage());
			assertTrue(e.getMessage().contains("meta-only.xml"), e.getMessage());
		}
	}

	private ConfigurationHelper databaseHelper() {
		ConfigurationHelper helper = new ConfigurationHelper();
		helper.setApplicationName("app");
		helper.setDefaultServerId("server");
		helper.setDataSource(dataSource);
		helper.setUseDatabase(true);
		return helper;
	}

	private static Element parseRoot(String xml) throws Exception {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(new InputSource(new StringReader(xml))).getDocumentElement();
	}
}
