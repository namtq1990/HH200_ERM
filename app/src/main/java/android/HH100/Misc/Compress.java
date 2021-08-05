package android.HH100.Misc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.HH100.Structure.NcLibrary;
import android.util.Log;

public class Compress {

private static final int BUFFER = 2048;
private String[] _files;
private String _zipFile;

public Compress(String[] files, String zipFile) {

            _files = files;
            _zipFile = zipFile;
}

public void zip() {


    try {
        BufferedInputStream origin = null;
        FileOutputStream dest = new FileOutputStream(_zipFile);

        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                dest));

        byte data[] = new byte[BUFFER];

        for (int i = 0; i < _files.length; i++) {
            Log.v("Compress", "Adding: " + _files[i]);
            String name =  _files[i];
            File f = new File( _files[i]);
            if (f.isDirectory()) {
                name = name.endsWith("/") ? name : name + "/";

                for (String file : f.list()) {
                    System.out.println(" checking " + file);
                    System.out
                            .println("The folder name is: " + f.getName());
                    out.putNextEntry(new ZipEntry(f.getName() + "/" + file));
                    FileInputStream fi = new FileInputStream( _files[i]
                            + "/" + file);
                    origin = new BufferedInputStream(fi, BUFFER);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
                System.out.println(" checking folder" + name);
            } else {
                FileInputStream fi = new FileInputStream( _files[i]);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry( _files[i].substring( _files[i]
                        .lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }

        out.close();
    } catch (Exception e) {
    	NcLibrary.Write_ExceptionLog(e);
    }
}

}