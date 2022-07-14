package botservice;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

public class ModuleUtils {
	
	private ModuleUtils() {
	}

	public static Blob base64ToBlob(String imgB64) throws SerialException, SQLException {
		byte[] decodedByte = Base64.getDecoder().decode(imgB64);

		Blob b = new SerialBlob(decodedByte);

		return b;

	}

	public static String blobToBase64(Blob img) throws SQLException {
		byte[] bdata = img.getBytes(1, (int) img.length());

		String imageB64 = Base64.getEncoder().encodeToString(bdata);

		return imageB64;
	}

	public static String getHttpGet(String url, String endpoint) {
        String result = null;
        try {
        	
        	String request = url + endpoint;
        	
            URL myurl = new URL(request);
            HttpURLConnection urlconnection = (HttpURLConnection) myurl.openConnection();
            urlconnection.setRequestMethod("HEAD");
            urlconnection.setRequestMethod("GET");
            urlconnection.setRequestProperty("X-Redmine-API-Key", "Chave_Redmine");
            urlconnection.setDoInput(true);
            urlconnection.connect();
            InputStream is = urlconnection.getInputStream();
            if (is != null) {
                StringBuffer sb = new StringBuffer();
                String line;
                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(is));
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();
                } finally {
                    is.close();
                }
                result = sb.toString();
            }
        } catch (Exception e) {
            result = null;
        }
        return result;
    }


}
