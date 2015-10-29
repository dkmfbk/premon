package eu.fbk.dkm.premon.premonitor;

import java.io.File;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class Test {

    public static void main(final String[] args) throws Throwable {
        for (final String line : Files.readLines(new File("/tmp/test_src.txt"), Charsets.UTF_8)) {
            final int index1 = line.indexOf("name=\"") + 6;
            final int index2 = line.indexOf("\"", index1);
            final String name = line.substring(index1, index2);
            final int index3 = line.indexOf("src=\"") + 5;
            final int index4 = line.indexOf("\"", index3);
            final String type = line.substring(index3, index4);
            if (!type.equals("") && !type.equals("-") && !type.equals(name)) {
                System.out.println(name + " --> " + type);
            }

        }
    }
}
