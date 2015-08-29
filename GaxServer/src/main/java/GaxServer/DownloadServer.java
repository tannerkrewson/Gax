package GaxServer;

import java.io.*;
import java.net.Socket;
import java.sql.ResultSet;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.security.EncryptionUtil;
import org.jets3t.service.utils.ServiceUtils;
import org.json.JSONObject;

public class DownloadServer {

    Socket socket;
    ServerThread st;

    public DownloadServer(Socket s, ServerThread st) {
        socket = s;
        this.st = st;
    }

    public void sendGame(int gid) {
        /*  Current assumed format is GaxGames/gid/gid.zip  */

        //tell the client what it's getting itself into
        JSONObject njo = new JSONObject();
        if (gameExists(gid)) {
            //if the game exists, the client will get ready to receive
            njo.put("responseToCommand", "download " + gid);
            njo.put("success", true);
            njo.put("url", getDownloadURL(gid));
            st.clientDataSender(njo);
        } else {
            njo.put("responseToCommand", "download " + gid);
            njo.put("success", false);
            njo.put("reason", 2);
            st.clientDataSender(njo);
            return;
        }

        System.out.println("Sent game url " + gid + " to client.");
        //sendFile("GaxGames/" + gid + "/" + gid + ".zip");
    }

    public String getDownloadURL(int gid) {

        try {
            String distributionDomain = "d23b1r6v0i4wgk.cloudfront.net";
            String privateKeyFilePath = "aws/pk-APKAJLNRKDR4IXAIAOMA.pem";
            String s3ObjectKey = gid + "/" + gid + ".zip";
            String policyResourcePath = distributionDomain + "/" + s3ObjectKey;
            String keyPairId = "APKAI4YTJQTJ37BYYRUQ";

            byte[] derPrivateKey = EncryptionUtil.convertRsaPemToDer(
                    new FileInputStream(privateKeyFilePath));

            String signedUrlCanned = CloudFrontService.signUrlCanned(
                    "https://" + distributionDomain + "/" + s3ObjectKey, // Resource URL or Path
                    keyPairId, // Certificate identifier, an active trusted signer for the distribution
                    derPrivateKey, // DER Private key data
                    ServiceUtils.parseIso8601Date("2009-11-14T22:20:00.000Z") // DateLessThan
            );

            System.out.println(signedUrlCanned);

            String policy = CloudFrontService.buildPolicyForSignedUrl(
                    policyResourcePath, // Resource path (optional, may include '*' and '?' wildcards)
                    ServiceUtils.parseIso8601Date("2009-11-14T22:20:00.000Z"), // DateLessThan
                    "0.0.0.0/0", // CIDR IP address restriction (optional, 0.0.0.0/0 means everyone)
                    null // DateGreaterThan (optional)
            );

            String signedUrl = CloudFrontService.signUrl(
                    "https://" + distributionDomain + "/" + s3ObjectKey, // Resource URL or Path
                    keyPairId, // Certificate identifier, an active trusted signer for the distribution
                    derPrivateKey, // DER Private key data
                    policy // Access control policy
            );

            System.out.println(signedUrl);
            return signedUrl;
        } catch (Exception ex) {
            System.out.println("Get download url from Amazon failed");
            ex.printStackTrace();
            return null;
        }
    }

    private boolean gameExists(int gid) {
        try {
            ResultSet rs = Server.dbc.query("SELECT * FROM GAMES.INFO "
                    + "WHERE GID = " + gid + ";");
            if (rs.next()) {
                return true;
            }
            System.out.println("The gid " + gid + "does not exist");
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Failed to check gid");
            return false;
        }
    }

    private void sendFile(String path) {
        System.out.println("Sending " + path);
        BufferedOutputStream outToClient = null;
        try {
            outToClient = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (outToClient != null) {
            File myFile = new File(path);
            byte[] mybytearray = new byte[(int) myFile.length()];

            FileInputStream fis = null;

            try {
                fis = new FileInputStream(myFile);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            BufferedInputStream bis = new BufferedInputStream(fis);

            try {
                bis.read(mybytearray, 0, mybytearray.length);
                outToClient.write(mybytearray, 0, mybytearray.length);
                outToClient.flush();
                outToClient.close();
                socket.close();

                // File sent, exit the main method
                System.out.println("File sent successfully!");
                return;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
