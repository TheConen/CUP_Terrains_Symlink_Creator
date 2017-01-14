package cup.terrains;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CUP_Terrains_SymlinkCreator {

    public static void main(String[] args) throws IOException {

        String workingDirectory = System.getProperty("user.dir");
        Path rootDirectory = Paths.get(workingDirectory + "\\CUP_Terrains");
        ArrayList<Path> subDirs = new ArrayList<>();


        //get a list of all subDirs
        DirectoryStream.Filter<Path> folderFilter = new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path file) throws IOException {
                return Files.isDirectory(file);
            }
        };
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDirectory, folderFilter)) {
            for (Path pathItr : stream) {
                subDirs.add(pathItr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //associate directories with their pboPrefixs
        HashMap<Path, String> dirPboprefixRelation = new HashMap();
        for (Path pathItr : subDirs) {
            Path pboprefixPath = Paths.get(pathItr.toString() + "\\$PBOPREFIX$");
            BufferedReader pboprefixReader = Files.newBufferedReader(pboprefixPath);
            String pboprefix =  pboprefixReader.readLine();
            dirPboprefixRelation.put(pathItr, pboprefix);
        }

        //create parent folders if they don't exist
        for (Map.Entry<Path, String> itr : dirPboprefixRelation.entrySet()) {
            Path link = Paths.get("P:\\" + itr.getValue());
            String temp[] = link.toString().split("\\\\");
            if (!temp[1].equals("CUP")) {
                String parentFolderStr = "";
                for (int i = 0; i < temp.length - 1; i++) {
                    parentFolderStr += temp[i] + "\\";
                }
                Path parentFolder = Paths.get(parentFolderStr);
                if (Files.notExists(parentFolder)) {
                    Files.createDirectory(parentFolder);
                }
            }
        }


        //create symbolic links
        for (Map.Entry<Path, String> itr : dirPboprefixRelation.entrySet()) {
            Path link = Paths.get("P:\\" + itr.getValue());
            Path target = itr.getKey();

            String[] temp = itr.getValue().split("\\\\");
            if (!temp[0].equals("CUP")) {

                Files.deleteIfExists(link);

                String linkPath = "P:\\";
                String targetPath = target.toString();
                String linkName = temp[temp.length-1];
                for (int i = 0; i < temp.length-1; i++) {
                    linkPath += temp[i] + "\\";
                }

                String command = "cd /D" + linkPath + " && mklink /J " + linkName + " \"" + targetPath +"\"";
                Process p = new ProcessBuilder("cmd.exe", "/c", command).start();
            }
        }


        //create symbolic link for the root dir
        if (Files.notExists(Paths.get("P:\\CUP"))) {
            Files.createDirectory(Paths.get("P:\\CUP"));
        }
        Files.deleteIfExists(Paths.get("P:\\CUP\\Terrains"));
        String command = "cd /D P:\\CUP" + " && mklink /J Terrains"  + " \"" + rootDirectory +"\"";
        Process p = new ProcessBuilder("cmd.exe", "/c", command).start();


        System.out.println("Symbolic links successfully created!");

    }
}
