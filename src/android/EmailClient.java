package cordova.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import java.io.File;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import cordova.plugin.MailSender;

/**
 * This class echoes a string called from JavaScript.
 */
public class EmailClient extends CordovaPlugin {
    public static final String ACTION_SEND = "send", ACTION_CONFIG = "config", ACTION_GET_CONFIG = "getConfig",
      ACTION_ADD_ATTACHMENT = "addAttachment";
    public static final String  CONFIG_HOST = "host", CONFIG_PORT = "port", CONFIG_USER = "user",
                                CONFIG_PASS = "pass", CONFIG_HTML = "html", CONFIG_SSL = "ssl";

    private MailSender mail = new MailSender();

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        try {
            if (EmailClient.ACTION_CONFIG.equals(action)) {
                config(args, callbackContext);
            } else if (EmailClient.ACTION_GET_CONFIG.equals(action)) {
                getConfig(args, callbackContext);
            } else if (EmailClient.ACTION_SEND.equals(action)) {
                sendEmail(args, callbackContext);
            } else if (EmailClient.ACTION_SEND.equals(action)) {
                addAttachment(args, callbackContext);
            } else {
                return false;
            }
        } catch (Exception e) {
            return true;
        }

        return true;
    }

    private void config(JSONArray args, CallbackContext callbackContext) throws Exception {

        mail.setHost(args.getString(0));

        mail.setPort(args.getString(1));
        mail.setUser(args.getString(2));
        mail.setPass(args.getString(3));
        //mail.setHTML(args.getBoolean(4));
        mail.setSSL(args.getBoolean(4));

        callbackContext.success();
    }

    private void getConfig(JSONArray args, CallbackContext callbackContext) throws Exception {
        String configToGet = args.getString(0);

        if (EmailClient.CONFIG_HOST.equals(configToGet)) {
            callbackContext.success(mail.getHost());
        } else if (EmailClient.CONFIG_PORT.equals(configToGet)) {
            callbackContext.success(mail.getPort());
        } else if (EmailClient.CONFIG_USER.equals(configToGet)) {
            callbackContext.success(mail.getUser());
        } else if (EmailClient.CONFIG_PASS.equals(configToGet)) {
            callbackContext.success(mail.getUser());
        } else if (EmailClient.CONFIG_HTML.equals(configToGet)) {
            int html = (mail.isHTML()) ? 1 : 0;
            callbackContext.success(html);
        } else if (EmailClient.CONFIG_SSL.equals(configToGet)) {
            int ssl = (mail.isSSL()) ? 1 : 0;
            callbackContext.success(ssl);
        } else {
            callbackContext.error("Email config '" + configToGet + "' does not exists.");
        }
    }

    private void addAttachment(final JSONArray args, final CallbackContext callbackContext){
      mail.addAttachment(new File(args.getString(0)));
      callbackContext.success();
    }

    private void sendEmail(final JSONArray args, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                Log.d("EMAIL", "Tentando enviar email");
                try {
                    Log.d("EMAIL", "Entrou no try");
                    String message = args.getString(0);
                    String subject = args.getString(1);
                    String from = args.getString(2);
                    String[] to = { args.getString(3) };
                    String sender = args.getString(4);
                    boolean html = args.getBoolean(5);
                    String replyTo = args.getString(6);

                    mail.setFrom(from);
                    mail.setTo(to);
                    mail.setSubject(subject);
                    mail.setBody(message);
                    mail.setSender(sender);
                    if(!"".equals(replyTo)){
                        mail.setReplyTo(replyTo);
                    }

                    if (mail.send()) {
                        Log.d("EMAIL", "Enviou email");
                        callbackContext.success(); // Thread-safe.
                    } else {
                        Log.d("EMAIL", "Não enviou email");
                        callbackContext.error(mail.getError().getMessage());
                    }
                } catch (Exception e) {
                    Log.d("EMAIL", "ERRO ");
                    callbackContext.error(e.getMessage());
                }

            }
        });
    }
}
