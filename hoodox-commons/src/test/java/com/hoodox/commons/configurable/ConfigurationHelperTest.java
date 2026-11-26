package com.hoodox.commons.configurable;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
			// rows for other applications and servers go in before the matching one, so a
			// query that ignores either column returns a wrong row
			insertRow(insert, "other-app", "server", "shared.xml", "<otherApp/>");
			insertRow(insert, "app", "other-server", "shared.xml", "<otherServer/>");
			insertRow(insert, "app", "server", "shared.xml", "<thisAppAndServer/>");
			insertRow(insert, "other-app", "other-server", "elsewhere-only.xml", "<elsewhere/>");
			insert.close();
		} finally {
			connection.close();
		}
	}

	private static void insertRow(PreparedStatement insert, String application, String server, String fileName,
			String content) throws SQLException {
		insert.setString(1, application);
		insert.setString(2, server);
		insert.setString(3, fileName);
		insert.setString(4, content);
		insert.executeUpdate();
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

	@Test
	public void useDatabaseMatchesApplicationAndServer() throws Exception {
		assertEquals(rootOfSharedXml("app", "server"), "thisAppAndServer");
		assertEquals(rootOfSharedXml("other-app", "server"), "otherApp");
		assertEquals(rootOfSharedXml("app", "other-server"), "otherServer");
	}

	@Test(expectedExceptions = XMLAccessException.class)
	public void useDatabaseIgnoresOtherApplicationsRows() throws Exception {
		databaseHelper("app", "server").createDomFromFile("elsewhere-only.xml");
	}

	@Test
	public void configRootDirComesBeforeTheClasspath() throws Exception {
		Path dir = tempDir();
		tempFile(dir.resolve("sample.xml"), "<fromRootDir id=\"r-1\"/>".getBytes(StandardCharsets.UTF_8));
		ConfigurationHelper helper = new ConfigurationHelper();
		helper.setConfigRootDir(dir.toString());
		helper.createDomFromFile("sample.xml");

		Element root = parseRoot(helper.getDomAsString());
		assertEquals(root.getTagName(), "fromRootDir");
		assertEquals(root.getAttribute("id"), "r-1");
	}

	@Test
	public void configRootDirWithoutTheFileFallsBackToTheClasspath() throws Exception {
		ConfigurationHelper helper = new ConfigurationHelper();
		helper.setConfigRootDir(tempDir().toString());
		helper.createDomFromFile("sample.xml");

		assertEquals(parseRoot(helper.getDomAsString()).getTagName(), "sample");
	}

	@Test
	public void plainFilePathIsTheLastLookup() throws Exception {
		Path file = tempFile(tempDir().resolve("plain.xml"), "<fromPlainPath/>".getBytes(StandardCharsets.UTF_8));
		ConfigurationHelper helper = new ConfigurationHelper();
		helper.createDomFromFile(file.toAbsolutePath().toString());

		assertEquals(parseRoot(helper.getDomAsString()).getTagName(), "fromPlainPath");
	}

	@Test
	public void utf8FileWithByteOrderMarkLoads() throws Exception {
		byte[] xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bom name=\"caf\u00e9\"/>".getBytes(StandardCharsets.UTF_8);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		bytes.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
		bytes.write(xml);
		Path file = tempFile(tempDir().resolve("bom.xml"), bytes.toByteArray());
		ConfigurationHelper helper = new ConfigurationHelper();
		helper.createDomFromFile(file.toString());

		assertEquals(parseRoot(helper.getDomAsString()).getAttribute("name"), "caf\u00e9");
	}

	@Test
	public void fileInTheEncodingItDeclaresLoads() throws Exception {
		Path file = tempFile(tempDir().resolve("latin1.xml"),
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><latin1 name=\"caf\u00e9\"/>"
						.getBytes(StandardCharsets.ISO_8859_1));
		ConfigurationHelper helper = new ConfigurationHelper();
		helper.createDomFromFile(file.toString());

		String xml = helper.getDomAsString();
		assertTrue(xml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><latin1 "), xml);
		assertEquals(parseRoot(xml).getAttribute("name"), "caf\u00e9");
	}

	private String rootOfSharedXml(String applicationName, String serverId) throws Exception {
		ConfigurationHelper helper = databaseHelper(applicationName, serverId);
		helper.createDomFromFile("shared.xml");
		return parseRoot(helper.getDomAsString()).getTagName();
	}

	private static Path tempDir() throws IOException {
		Path dir = Files.createTempDirectory("configurationHelperTest");
		dir.toFile().deleteOnExit();
		return dir;
	}

	/** Registered after its folder, so it is deleted before it. */
	private static Path tempFile(Path file, byte[] content) throws IOException {
		Files.write(file, content);
		file.toFile().deleteOnExit();
		return file;
	}

	private ConfigurationHelper databaseHelper() {
		return databaseHelper("app", "server");
	}

	private ConfigurationHelper databaseHelper(String applicationName, String serverId) {
		ConfigurationHelper helper = new ConfigurationHelper();
		helper.setApplicationName(applicationName);
		helper.setDefaultServerId(serverId);
		helper.setDataSource(dataSource);
		helper.setUseDatabase(true);
		return helper;
	}

	private static Element parseRoot(String xml) throws Exception {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(new InputSource(new StringReader(xml))).getDocumentElement();
	}
}
