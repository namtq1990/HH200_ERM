package android.HH100.Structure;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.HH100.R.string;

public class Mail extends javax.mail.Authenticator {
	private String _user;
	private String _pass;

	private String[] _to;
	private String _from;

	private String _port;
	private String _sport;
	private String protocol = "smtp";
	private String _host;

	private String _subject;
	private String _body;

	private boolean _auth;

	private boolean _debuggable;

	private Multipart _multipart;

	public Mail() {
		_host = "mail.nucaremed.com"; // default smtp server
		_port = "465"; // default smtp port
		_sport = "465"; // default socketfactory port

		_user = ""; // username
		_pass = ""; // password
		_from = ""; // email sent from
		_subject = ""; // email subject
		_body = ""; // email body

		_debuggable = false; // debug mode on or off - default off
		_auth = true; // smtp authentication - default on

		_multipart = new MimeMultipart();

		// There is something wrong with MailCap, javamail can not find a
		// handler for the multipart/mixed part, so this bit needs to be added.
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);
	}

	public Mail(String user, String pass, String host, String port) {
		this();

		_user = user;
		_pass = pass;
		_host = host;
		_port = port;

	}

	public boolean send() throws Exception {
		Properties props = _setProperties();

		if (!_user.equals("") && !_pass.equals("") && _to.length > 0 && !_from.equals("") && !_subject.equals("")
				&& !_body.equals("")) {

			javax.mail.Session session = javax.mail.Session.getInstance(props, new GMailAuthenticator(_user, _pass));

			session.setDebug(_debuggable);

			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(_from));

			InternetAddress[] addressTo = new InternetAddress[_to.length];
			for (int i = 0; i < _to.length; i++) {
				addressTo[i] = new InternetAddress(_to[i]);
			}
			msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);

			msg.setSubject(_subject);
			msg.setSentDate(new Date());

			// setup message body
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(_body);
			_multipart.addBodyPart(messageBodyPart);

			// Put parts in message
			msg.setContent(_multipart);

			Transport.send(msg);
			return true;
		} else {
			return false;
		}
	}

	public void addAttachment(String filePath, String fileName) throws Exception {

		BodyPart messageBodyPart = new MimeBodyPart();

		javax.activation.DataSource source = new FileDataSource(filePath);

		messageBodyPart.setDataHandler(new DataHandler((javax.activation.DataSource) source));

		messageBodyPart.setFileName(fileName);

		_multipart.addBodyPart(messageBodyPart);

	}

	@Override
	public javax.mail.PasswordAuthentication getPasswordAuthentication() {
		return new javax.mail.PasswordAuthentication(_user, _pass);
	}

	private Properties _setProperties() {
		Properties props = new Properties();
		props.put("mail.transport.protocol", protocol);
		props.put("mail.smtp.user", _user);
		props.put("mail.smtp.host", _host);
		props.put("mail.smtp.port", _port);

		if (_debuggable) {
			props.put("mail.debug", "true");
		}

		if (_auth) {
			props.put("mail.smtp.auth", "true");
		}
		if (_user.matches(".*@gmail.com")) {
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.socketFactory.port", _port);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.socketFactory.fallback", "false");
			props.setProperty("mail.smtp.quitwait", "false");
		}

		// props.put("mail.smtp.socketFactory.port", _sport);
		// props.put("mail.smtp.socketFactory.class",
		// "javax.net.ssl.SSLSocketFactory");
		// props.put("mail.smtp.socketFactory.fallback", "false");

		return props;
	}

	// the getters and setters
	public String getBody() {
		return _body;
	}

	public void setBody(String _body) {
		this._body = _body;
	}

	public String[] getTo() {
		return _to;
	}

	public void setTo(String[] _to) {
		this._to = _to;
	}

	public String getFrom() {
		return _from;
	}

	public void setHost(String host) {
		this._host = host;
	}

	public void setFrom(String _from) {
		this._from = _from;
	}

	public String getSubject() {
		return _subject;
	}

	public void setSubject(String _subject) {
		this._subject = _subject;
	}

	class GMailAuthenticator extends Authenticator {
		String user;
		String pw;

		public GMailAuthenticator(String username, String password) {
			super();
			this.user = username;
			this.pw = password;
		}

		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user, pw);
		}
	}

}