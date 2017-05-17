package cordova.plugin;

import android.util.Log;
import java.util.Date;
import java.util.Properties;
import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Created by bmsoft-efw on 22/03/2017.
 */
public class MailSender extends javax.mail.Authenticator {
    private String _user;
    private String _pass;

    private String[] _to;
    private String _from;
    private String _sender;

    private String _replyTo;

    private String _port;
    private String _sport;

    private String _host;

    private String _subject;
    private String _body;

    private boolean _ssl;
    private boolean _html;
    private boolean _auth;

    private boolean _debuggable;

    private Multipart _multipart;

    private Exception error;

    public MailSender() {
        _host = "smtp.bmsoft.com.br"; // default smtp server
        _port = "465"; // default smtp port
        _sport = "465"; // default socketfactory port

        _user = ""; // username
        _pass = ""; // password
        _from = ""; // email sent from
        _sender = "";
        _replyTo = null;
        _subject = ""; // email subject
        _body = ""; // email body

        _ssl = true;
        _html = true;
        _debuggable = false; // debug mode on or off - default off
        _auth = true; // smtp authentication - default on

        _multipart = new MimeMultipart();

        error = null;

        // There is something wrong with MailCap, javamail can not find a handler for the multipart/mixed part, so this bit needs to be added.
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }

    public MailSender(String host, String port, String user, String pass) {
        this();

        _host = host;
        _port = port;
        _sport = port;
        _user = user;
        _pass = pass;
        _from = user;
    }

    public boolean send() throws Exception {
        Properties props = _setProperties();
        String to_ = "[ ";
        for(int i = 0; i < this._to.length; i++){
            if(i == 0){
                to_ += this._to[i];
            }else{
                to_ += " , " + this._to[i];
            }
        }
        to_ += " ]";
        Log.d("EMAIL", "Sending email from " + this._from + " to " + to_);
        if (!_user.equals("") && !_pass.equals("") && _to.length > 0 && !_from.equals("") && !_subject.equals("")
                && !_body.equals("")) {
            try {
                Session session = Session.getInstance(props, this);

                MimeMessage msg = new MimeMessage(session);

                if(this._sender.equals("")){
                    msg.setFrom(new InternetAddress(_from));
                }else{
                    msg.setFrom(new InternetAddress(_from, _sender));
                }
                
                

                InternetAddress[] addressTo = new InternetAddress[_to.length];
                for (int i = 0; i < _to.length; i++) {
                    addressTo[i] = new InternetAddress(_to[i]);
                }
                msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);

                msg.setSubject(_subject);
                msg.setSentDate(new Date());

                // setup message body
                /*BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(_body);
                _multipart.addBodyPart(messageBodyPart);
                */

                // Put parts in message
                if (_html) {
                    msg.setContent(_body, "text/html; charset=utf-8");
                } else {
                    msg.setContent(_body, "text/plain; charset=utf-8");
                    BodyPart messageBodyPart = new MimeBodyPart();
                    messageBodyPart.setText(_body);
                    _multipart.addBodyPart(messageBodyPart);
                    msg.setContent(_multipart);
                }
                // send email
                
                Transport.send(msg);
                return true;
            } catch (Exception e) {
                Log.e("EMAIL", e.getMessage());
                this.error = e;
                return false;
            }
        } else {
            return false;
        }
    }

    private Properties _setProperties() {
        Properties props = new Properties();

        props.put("mail.smtp.host", _host);

        if (_debuggable) {
            props.put("mail.debug", "true");
        }

        if (_auth) {
            props.put("mail.smtp.auth", "true");
        }

        props.put("mail.smtp.port", _port);
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        if (this._ssl) {
            props.put("mail.smtp.socketFactory.port", _sport);
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        return props;
    }

    public void addAttachment(String filename) throws Exception {
        BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);

        _multipart.addBodyPart(messageBodyPart);
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(_user, _pass);
    }

    // the getters and setters

    public String getUser() {
        return _user;
    }

    public void setUser(String _user) {
        this._user = _user;
    }

    public String getPass() {
        return _pass;
    }

    public void setPass(String _pass) {
        this._pass = _pass;
    }

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

    public void setFrom(String _from) {
        this._from = _from;
    }

    public String getSubject() {
        return _subject;
    }

    public void setSubject(String _subject) {
        this._subject = _subject;
    }

    public String getPort() {
        return _port;
    }

    public void setPort(String _port) {
        this._port = _port;
        this._sport = _port;
    }

    public String getHost() {
        return _host;
    }

    public void setHost(String _host) {
        this._host = _host;
    }

    public boolean isHTML() {
        return this._html;
    }

    public void setHTML(boolean _html) {
        this._html = _html;
    }

    public boolean isSSL() {
        return this._ssl;
    }

    public void setSSL(boolean _ssl) {
        this._ssl = _ssl;
    }

    public Exception getError(){
        return this.error;
    }

    public void setSender(String sender){
        this._sender = sender;
    }

    public String getSender(){
        return this._sender;
    }

    public String getReplyTo(){
        return this._replyTo;
    }

    public void setReplyTo(String replyTo){
        this._replyTo = replyTo;
    }
}
