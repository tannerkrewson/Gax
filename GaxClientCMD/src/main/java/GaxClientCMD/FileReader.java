package GaxClientCMD;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import org.json.JSONObject;

public class FileReader {

    /**
     * Reads a .json file into a JSONObject.
     *
     * @param path The location of the JSON file.
     * @return The resulting JSONObject
     * @throws java.io.FileNotFoundException if the file does not exist
     */
    public JSONObject readJSONFile(String path) throws IOException {

        //trys to read config
        String temp = readTextFile(path);

        //converts read string to JSONObject
        return new JSONObject(temp);
    }

    /**
     * Reads a text file into a String.
     *
     * @param path The location of the text file.
     * @return The resulting String
     * @throws java.io.FileNotFoundException if the file does not exist
     */
    public String readTextFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), Charset.defaultCharset());
    }
}
