package com.hoodox.commons.configurable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.commons.exceptions.ErrorCode;
import com.hoodox.commons.exceptions.XMLAccessException;

/**
 * Loads an XML configuration file, either from files or from the database.
 * <p>
 * With <code>useDatabase</code> false (the default) the first of these that
 * exists is used: the file under <code>configRootDir</code> (if set), the name
 * on the classpath, <code>META-INF/&lt;name&gt;</code> on the classpath, the name
 * as a plain file path.
 * <p>
 * With <code>useDatabase</code> true the file is the <code>CONTENT</code> of the
 * <code>config_repository</code> row for <code>applicationName</code>,
 * <code>defaultServerId</code> and the file name. There is no fall-back to files.
 */
public class ConfigurationHelper {

	private static final String SELECT_CONTENT = "select CONTENT from config_repository "
			+ "where APPLICATION_NAME = ? and SERVER_ID = ? and FILE_NAME = ?";

	private static final String ERROR_CODE_xml_access_error = "xml_access_error";

	private String applicationName;
	private DataSource dataSource;
	private String defaultServerId;
	private boolean useDatabase;
	private String configRootDir;

	private Document dom;

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setDefaultServerId(String defaultServerId) {
		this.defaultServerId = defaultServerId;
	}

	public void setUseDatabase(boolean useDatabase) {
		this.useDatabase = useDatabase;
	}

	public void setConfigRootDir(String configRootDir) {
		this.configRootDir = configRootDir;
	}

	/**
	 * Loads and parses an XML file.
	 *
	 * @throws FileNotFoundException if <code>useDatabase</code> is false and the
	 *             file is nowhere to be found
	 * @throws IOException if the file cannot be read
	 * @throws XMLAccessException if the XML cannot be parsed, or if
	 *             <code>useDatabase</code> is true and there is no such row or
	 *             the database cannot be read
	 */
	public void createDomFromFile(String fileName) throws IOException, XMLAccessException {
		String xml = useDatabase ? readFromDatabase(fileName) : readFromFile(fileName);
		dom = parse(fileName, xml);
	}

	/**
	 * @return the XML of the last file loaded, or null if none has been loaded
	 */
	public String getDomAsString() {
		if (dom == null) {
			return null;
		}
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(dom), new StreamResult(writer));
			return writer.toString();
		} catch (TransformerException e) {
			throw new BaseSystemException(new ErrorCode(ERROR_CODE_xml_access_error),
					"Cannot write the XML document as a string", e);
		}
	}

	private String readFromFile(String fileName) throws IOException {
		InputStream in = open(fileName);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int length;
			while ((length = in.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}
			return new String(out.toByteArray(), StandardCharsets.UTF_8);
		} finally {
			in.close();
		}
	}

	private InputStream open(String fileName) throws IOException {
		if (configRootDir != null && configRootDir.length() > 0) {
			File file = new File(configRootDir, fileName);
			if (file.isFile()) {
				return new FileInputStream(file);
			}
		}

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader == null) {
			classLoader = ConfigurationHelper.class.getClassLoader();
		}
		InputStream in = classLoader.getResourceAsStream(fileName);
		if (in == null) {
			in = classLoader.getResourceAsStream("META-INF/" + fileName);
		}
		if (in != null) {
			return in;
		}

		File file = new File(fileName);
		if (file.isFile()) {
			return new FileInputStream(file);
		}
		throw new FileNotFoundException("Cannot find configuration file " + fileName);
	}

	private String readFromDatabase(String fileName) throws XMLAccessException {
		String key = "APPLICATION_NAME=" + applicationName + ", SERVER_ID=" + defaultServerId
				+ ", FILE_NAME=" + fileName;
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(SELECT_CONTENT)) {
			statement.setString(1, applicationName);
			statement.setString(2, defaultServerId);
			statement.setString(3, fileName);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					throw new XMLAccessException(new ErrorCode(ERROR_CODE_xml_access_error),
							"No row in config_repository for " + key);
				}
				return resultSet.getString(1);
			}
		} catch (SQLException e) {
			throw new XMLAccessException(new ErrorCode(ERROR_CODE_xml_access_error),
					"Cannot read config_repository for " + key + ": " + e.getMessage(), e);
		}
	}

	private static Document parse(String fileName, String xml) throws XMLAccessException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
		} catch (ParserConfigurationException e) {
			throw parseError(fileName, e);
		} catch (SAXException e) {
			throw parseError(fileName, e);
		} catch (IOException e) {
			throw parseError(fileName, e);
		}
	}

	private static XMLAccessException parseError(String fileName, Exception cause) {
		return new XMLAccessException(new ErrorCode(ERROR_CODE_xml_access_error),
				"Cannot parse XML file " + fileName + ": " + cause.getMessage(), cause);
	}
}
