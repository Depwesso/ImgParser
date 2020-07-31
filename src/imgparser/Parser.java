package imgparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList; 
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Parser implements Runnable {
    public Thread t;
    public javax.swing.JTextArea textArea;
    public boolean started;
    public Parser(javax.swing.JTextArea textArea) {
        this.textArea = textArea;
        this.started = false;
    }

    @Override
    public void run() {
        this.textArea.append("[+] Transformice image parser\n");
        this.textArea.append("[+] Tool by Depwesso\n");
        this.textArea.append("\n");
        List<String> urls = new ArrayList();
        for (String html : new String[] {"images", "ar", "godspaw", "share", "woot", "wp-admin", "wp-content", "wp-includes"}) {
            try {
                URL url = new URL("http://derpolino.alwaysdata.net/imagetfm/getFiles.php?n=" +  html + "%2F&mode=tfm");
                URLConnection conn = url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String data;
                while ((data = in.readLine()) != null) {
                    urls.addAll(Arrays.asList(Arrays.stream(data.split("\"")).filter(x -> x.contains("/")).map(x -> "https://www.transformice.com/" + x.replace("\\", "")).toArray(String[]::new)));
                }
                in.close();
            } catch (MalformedURLException ex) {
                Logger.getLogger(ImgParser.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ImgParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        for (String url : urls) {
            this.download(url);
        }
    }

    public void download(String url) {
        try {
            URL Url = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection) Url.openConnection();
            int code = httpCon.getResponseCode();
            if (code != 200) {
                this.textArea.append("Invalid response code, skipping.\n");
                return;
            }
            URLConnection conn = Url.openConnection();
            Matcher matcher = Pattern.compile("(http[s]?:\\/\\/)?([^\\/\\s]+\\/)(.*)").matcher(url);
            String path;
            path = matcher.matches() ? "TFM_IMAGES/" + matcher.group(3) : null;
            String[] elems = path.split("/");
            String dir = String.join("/", Arrays.copyOfRange(elems, 0, (elems.length - 1)));
            String fileName = elems[elems.length - 1];
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }

            File file = new File(dir);
            if (!file.exists()) {
                this.textArea.append("[+] Creating '" + dir + "' directory.\n");
                file.mkdirs();
            }
            file = new File(dir + fileName);
            InputStream is;
            if (file.exists()) {
                this.textArea.append("[!] File '" + dir + fileName + "' is already exists, skipping.\n");
                return;
            }
            this.textArea.append("[+] Saving '" + fileName + "' to '" + dir + "' directory.\n");
            is = Url.openStream();
            OutputStream os = new FileOutputStream(dir + fileName);
            byte[] b = new byte[2048];
            int length;
            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }
            is.close();
            os.close();
        } catch (MalformedURLException ex) {
            Logger.getLogger(ImgParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImgParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start() {
        if (this.t == null) {
            this.started = true;
            this.t = new Thread(this, "x");
            this.t.start();
        }
    }

}
