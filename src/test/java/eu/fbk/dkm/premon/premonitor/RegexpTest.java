package eu.fbk.dkm.premon.premonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 26/02/16.
 */

public class RegexpTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegexpTest.class);

    public static void main(String[] args) {
        String text = "The Wardak police chief , Gen. Abdul Qayuum Baqizoi , said the American strike was aimed at a clandestine meeting of insurgent figures in the village Jaw-e-Mekh Zareen , which is considered a perilous locale .";
        String regex = "[^\\s]+";
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(text);

        while (matcher.find()) {
            System.out.println(matcher.group() + ":" + "start =" + matcher.start() + " end = " + matcher.end());
        }

    }
}
